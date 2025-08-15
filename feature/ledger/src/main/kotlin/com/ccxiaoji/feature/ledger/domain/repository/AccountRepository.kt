package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * 账户仓库接口
 * 定义所有账户相关的数据操作
 */
interface AccountRepository {
    /**
     * 获取所有账户
     */
    fun getAccounts(): Flow<List<Account>>
    
    /**
     * 获取默认账户
     */
    suspend fun getDefaultAccount(): Account?
    
    /**
     * 创建账户
     */
    suspend fun createAccount(
        name: String,
        type: com.ccxiaoji.feature.ledger.domain.model.AccountType,
        initialBalanceCents: Long,
        creditLimitCents: Long? = null,
        billingDay: Int? = null,
        paymentDueDay: Int? = null,
        gracePeriodDays: Int? = null
    ): Long
    
    suspend fun updateBalance(accountId: String, changeAmount: Long)
    
    suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int
    )
    
    suspend fun recordCreditCardPayment(
        accountId: String,
        paymentAmountCents: Long
    )
    
    suspend fun recordCreditCardPaymentWithHistory(
        accountId: String,
        paymentAmountCents: Long,
        paymentType: com.ccxiaoji.feature.ledger.data.local.entity.PaymentType,
        dueAmountCents: Long,
        note: String? = null
    )
    
    suspend fun getCreditCardsWithPaymentDueDay(dayOfMonth: Int): List<Account>
    
    suspend fun getCreditCardsWithDebt(): List<Account>
    
    fun getCreditCardPayments(accountId: String): Flow<List<com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity>>
    
    suspend fun getPaymentStats(accountId: String): com.ccxiaoji.feature.ledger.domain.repository.PaymentStats
    
    suspend fun deletePaymentRecord(paymentId: String)
    
    /**
     * 更新账户
     */
    suspend fun updateAccount(account: Account)
    
    /**
     * 删除账户
     */
    suspend fun deleteAccount(accountId: String)
    
    /**
     * 设置默认账户
     */
    suspend fun setDefaultAccount(accountId: String)
    
    /**
     * 根据ID获取账户
     */
    suspend fun getAccountById(accountId: String): Account?
    
    /**
     * 获取总余额
     */
    suspend fun getTotalBalance(): Double
    
    /**
     * 在账户间转账
     */
    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long,
        note: String?
    )
    
}