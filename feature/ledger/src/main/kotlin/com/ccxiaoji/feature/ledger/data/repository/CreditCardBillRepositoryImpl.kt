package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardBillDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardBillEntity
import com.ccxiaoji.feature.ledger.domain.model.BillStatus
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 信用卡账单仓库实现
 */
@Singleton
class CreditCardBillRepositoryImpl @Inject constructor(
    private val creditCardBillDao: CreditCardBillDao,
    private val transactionDao: TransactionDao,
    private val userApi: UserApi
) : CreditCardBillRepository {
    
    override suspend fun generateBill(
        accountId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BaseResult<CreditCardBill> {
        return try {
            val userId = userApi.getCurrentUserId()
            
            // 检查是否已存在该周期的账单
            val startMillis = periodStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = periodEnd.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            if (creditCardBillDao.hasBillForPeriod(accountId, startMillis, endMillis)) {
                return BaseResult.Error(IllegalStateException("该周期的账单已存在"))
            }
            
            // 计算账单金额
            val transactions = transactionDao.getTransactionsByBillingCycle(
                accountId = accountId,
                startDate = startMillis,
                endDate = endMillis
            )
            
            var totalCharges = 0L
            var totalPayments = 0L
            
            transactions.forEach { transaction ->
                // 判断是否为支出（需要根据category类型判断）
                // TODO: 这里简化处理，实际应该通过categoryId查询category类型
                if (transaction.amountCents > 0) { // 假设正数为支出
                    totalCharges += transaction.amountCents
                } else {
                    totalPayments += Math.abs(transaction.amountCents)
                }
            }
            
            // 获取上期结余
            val previousBill = creditCardBillDao.getBillsByDateRange(
                accountId = accountId,
                startDate = 0,
                endDate = startMillis - 1
            ).maxByOrNull { it.billEndDate }
            
            val previousBalance = previousBill?.remainingAmountCents ?: 0
            val totalAmount = previousBalance + totalCharges - totalPayments
            val minimumPayment = (totalAmount * 0.1).toLong().coerceAtLeast(100) // 最低还款额为10%，至少1元
            
            // 计算还款日（假设为下个月的10号）
            val paymentDueDate = periodEnd.plus(1, DateTimeUnit.MONTH).let { nextMonth ->
                LocalDate(nextMonth.year, nextMonth.month, 10)
            }
            
            val now = Clock.System.now()
            val billEntity = CreditCardBillEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                accountId = accountId,
                billStartDate = startMillis,
                billEndDate = endMillis,
                paymentDueDate = paymentDueDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                totalAmountCents = totalAmount,
                newChargesCents = totalCharges,
                previousBalanceCents = previousBalance,
                paymentsCents = totalPayments,
                adjustmentsCents = 0,
                minimumPaymentCents = minimumPayment,
                isGenerated = true,
                isPaid = totalAmount <= 0,
                paidAmountCents = 0,
                isOverdue = false,
                createdAt = now.toEpochMilliseconds(),
                updatedAt = now.toEpochMilliseconds()
            )
            
            creditCardBillDao.insert(billEntity)
            
            BaseResult.Success(billEntity.toDomainModel())
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override fun getBillsByAccount(accountId: String): Flow<List<CreditCardBill>> {
        return creditCardBillDao.getBillsByAccount(accountId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getBillById(billId: String): BaseResult<CreditCardBill> {
        return try {
            val bill = creditCardBillDao.getBillById(billId)
            if (bill != null) {
                BaseResult.Success(bill.toDomainModel())
            } else {
                BaseResult.Error(NoSuchElementException("账单不存在"))
            }
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override suspend fun updateBillStatus(billId: String, status: BillStatus): BaseResult<Unit> {
        return try {
            val bill = creditCardBillDao.getBillById(billId)
                ?: return BaseResult.Error(NoSuchElementException("账单不存在"))
            
            val updatedBill = when (status) {
                BillStatus.PENDING -> bill.copy(isGenerated = false)
                BillStatus.GENERATED -> bill.copy(isGenerated = true)
                BillStatus.PAID -> bill.copy(isPaid = true, paidAmountCents = bill.totalAmountCents)
                BillStatus.PARTIAL_PAID -> bill.copy(isPaid = false)
                BillStatus.OVERDUE -> bill.copy(isOverdue = true)
            }.copy(updatedAt = Clock.System.now().toEpochMilliseconds())
            
            creditCardBillDao.update(updatedBill)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override suspend fun recordPayment(billId: String, amount: Int): BaseResult<Unit> {
        return try {
            creditCardBillDao.updatePaymentStatus(
                billId = billId,
                paymentAmountCents = amount.toLong(),
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override fun getPendingBills(): Flow<List<CreditCardBill>> {
        // TODO: 需要实现获取所有信用卡账户的未还清账单
        // 当前实现为返回空列表，后续需要优化
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    override suspend fun markOverdueBills(): BaseResult<Int> {
        return try {
            val currentDate = Clock.System.now().toEpochMilliseconds()
            val timestamp = currentDate
            
            // 这里需要遍历所有账户，暂时简化处理
            // TODO: 需要获取所有信用卡账户ID并逐个标记
            
            BaseResult.Success(0)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override suspend fun getCurrentBill(accountId: String): BaseResult<CreditCardBill?> {
        return try {
            val bill = creditCardBillDao.getCurrentBill(accountId)
            BaseResult.Success(bill?.toDomainModel())
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override suspend fun hasBillForPeriod(
        accountId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate
    ): BaseResult<Boolean> {
        return try {
            val startMillis = periodStart.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = periodEnd.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            val exists = creditCardBillDao.hasBillForPeriod(accountId, startMillis, endMillis)
            BaseResult.Success(exists)
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    /**
     * 将实体转换为领域模型
     */
    private fun CreditCardBillEntity.toDomainModel(): CreditCardBill {
        return CreditCardBill(
            id = id,
            userId = userId,
            accountId = accountId,
            billStartDate = Instant.fromEpochMilliseconds(billStartDate),
            billEndDate = Instant.fromEpochMilliseconds(billEndDate),
            paymentDueDate = Instant.fromEpochMilliseconds(paymentDueDate),
            totalAmountCents = totalAmountCents,
            newChargesCents = newChargesCents,
            previousBalanceCents = previousBalanceCents,
            paymentsCents = paymentsCents,
            adjustmentsCents = adjustmentsCents,
            minimumPaymentCents = minimumPaymentCents,
            isGenerated = isGenerated,
            isPaid = isPaid,
            paidAmountCents = paidAmountCents,
            isOverdue = isOverdue,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }
}