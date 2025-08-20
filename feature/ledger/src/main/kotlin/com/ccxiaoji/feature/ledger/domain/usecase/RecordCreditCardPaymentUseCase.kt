package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardPaymentDao
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject

/**
 * 记录信用卡还款用例
 * 
 * 功能：
 * 1. 创建还款记录
 * 2. 更新账单已还金额
 * 3. 在还款账户创建支出交易（如果指定了还款账户）
 */
class RecordCreditCardPaymentUseCase @Inject constructor(
    private val creditCardBillRepository: CreditCardBillRepository,
    private val creditCardPaymentDao: CreditCardPaymentDao,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val userApi: UserApi
) {
    
    suspend operator fun invoke(
        billId: String,
        amount: Int,
        fromAccountId: String? = null,
        paymentType: PaymentType = PaymentType.CUSTOM,
        note: String? = null
    ): BaseResult<Unit> {
        return try {
            val userId = userApi.getCurrentUserId()
            
            // 1. 获取账单信息
            val bill = when (val result = creditCardBillRepository.getBillById(billId)) {
                is BaseResult.Success -> result.data
                is BaseResult.Error -> return BaseResult.Error(result.exception)
            }
            
            // 2. 创建还款记录
            val paymentEntity = CreditCardPaymentEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = bill.accountId,
                paymentAmountCents = amount.toLong(),
                paymentType = paymentType,
                paymentDate = Clock.System.now().toEpochMilliseconds(),
                dueAmountCents = bill.totalAmountCents,
                isOnTime = !bill.isOverdue,
                note = note,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
            
            creditCardPaymentDao.insert(paymentEntity)
            
            // 3. 更新账单已还金额
            when (val result = creditCardBillRepository.recordPayment(billId, amount)) {
                is BaseResult.Success -> {
                    // 继续处理
                }
                is BaseResult.Error -> return BaseResult.Error(result.exception)
            }
            
            // 4. 如果指定了还款账户，创建支出交易
            if (fromAccountId != null) {
                // 获取或创建"信用卡还款"分类
                val categories = categoryRepository.getCategoriesByType(Category.Type.EXPENSE).first()
                val paymentCategory = categories.find { it.name == "信用卡还款" }
                    ?: createPaymentCategory(userId)
                
                // 获取信用卡账户信息
                val creditCardAccount = accountRepository.getAccountById(bill.accountId)
                
                // 创建支出交易
                val defaultLedger = manageLedgerUseCase.getDefaultLedger(userApi.getCurrentUserId()).getOrThrow()
                
                val transactionResult = transactionRepository.addTransaction(
                    amountCents = amount,
                    categoryId = paymentCategory.id,
                    note = note ?: "信用卡还款 - ${creditCardAccount?.name ?: ""}",
                    accountId = fromAccountId,
                    ledgerId = defaultLedger.id,
                    transactionDate = null,
                    location = null
                )
            }
            
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    /**
     * 创建信用卡还款分类
     */
    private suspend fun createPaymentCategory(userId: String): Category {
        // 创建分类并返回
        val categoryId = categoryRepository.createCategory(
            name = "信用卡还款",
            icon = "💳",
            color = "#2196F3",
            type = Category.Type.EXPENSE.name,
            parentId = null
        )
        
        // 创建一个临时的Category对象返回
        return Category(
            id = categoryId.toString(),
            name = "信用卡还款",
            icon = "💳",
            color = "#2196F3",
            type = Category.Type.EXPENSE,
            parentId = null,
            isSystem = false,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
}