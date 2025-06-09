package com.ccxiaoji.app.data.repository

import com.ccxiaoji.core.database.dao.AccountDao
import com.ccxiaoji.core.database.dao.ChangeLogDao
import com.ccxiaoji.core.database.dao.CreditCardPaymentDao
import com.ccxiaoji.core.database.dao.CreditCardBillDao
import com.ccxiaoji.core.database.dao.TransactionDao
import com.ccxiaoji.core.database.entity.AccountEntity
import com.ccxiaoji.core.database.entity.ChangeLogEntity
import com.ccxiaoji.core.database.entity.CreditCardPaymentEntity
import com.ccxiaoji.core.database.entity.CreditCardBillEntity
import com.ccxiaoji.core.database.entity.PaymentType
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.AccountType
import com.ccxiaoji.app.domain.model.Transaction
import com.ccxiaoji.app.utils.CreditCardDateUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val changeLogDao: ChangeLogDao,
    private val creditCardPaymentDao: CreditCardPaymentDao,
    private val creditCardBillDao: CreditCardBillDao,
    private val transactionDao: TransactionDao,
    private val gson: Gson
) {
    fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAccountsByUser(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    suspend fun getAccountById(accountId: String): Account? {
        return accountDao.getAccountById(accountId)?.toDomainModel()
    }
    
    suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount(getCurrentUserId())?.toDomainModel()
    }
    
    suspend fun getTotalBalance(): Double {
        val totalCents = accountDao.getTotalBalance(getCurrentUserId()) ?: 0L
        return totalCents / 100.0
    }
    
    suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceCents: Long = 0L,
        currency: String = "CNY",
        icon: String? = null,
        color: String? = null,
        creditLimitCents: Long? = null,
        billingDay: Int? = null,
        paymentDueDay: Int? = null,
        gracePeriodDays: Int? = null
    ): Account {
        val accountId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = AccountEntity(
            id = accountId,
            userId = getCurrentUserId(),
            name = name,
            type = type.name,
            balanceCents = initialBalanceCents,
            currency = currency,
            icon = icon,
            color = color,
            isDefault = false,
            creditLimitCents = creditLimitCents,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        accountDao.insertAccount(entity)
        
        // Log the change for sync
        logChange("accounts", accountId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    suspend fun updateAccount(account: Account) {
        val now = System.currentTimeMillis()
        val entity = account.toEntity(getCurrentUserId(), now)
        
        accountDao.updateAccount(entity)
        
        // Log the change for sync
        logChange("accounts", account.id, "UPDATE", entity)
    }
    
    suspend fun setDefaultAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        // Clear other defaults
        accountDao.clearDefaultStatus(getCurrentUserId())
        
        // Set new default
        accountDao.setDefaultAccount(accountId, getCurrentUserId(), now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("isDefault" to true))
    }
    
    suspend fun updateBalance(accountId: String, amountCents: Long) {
        val now = System.currentTimeMillis()
        
        accountDao.updateBalance(accountId, amountCents, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("balanceChange" to amountCents))
    }
    
    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long
    ) {
        val now = System.currentTimeMillis()
        
        // Deduct from source account
        accountDao.updateBalance(fromAccountId, -amountCents, now)
        
        // Add to destination account
        accountDao.updateBalance(toAccountId, amountCents, now)
        
        // Log the changes for sync
        logChange("accounts", fromAccountId, "TRANSFER", mapOf(
            "from" to fromAccountId,
            "to" to toAccountId,
            "amount" to amountCents
        ))
    }
    
    suspend fun deleteAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        accountDao.softDeleteAccount(accountId, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "DELETE", mapOf("id" to accountId))
    }
    
    // Credit card specific methods
    fun getCreditCardAccounts(): Flow<List<Account>> {
        return accountDao.getCreditCardAccounts(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    suspend fun getCreditCardsWithBillingDay(dayOfMonth: Int): List<Account> {
        return accountDao.getCreditCardsWithBillingDay(getCurrentUserId(), dayOfMonth)
            .map { it.toDomainModel() }
    }
    
    suspend fun getCreditCardsWithPaymentDueDay(dayOfMonth: Int): List<Account> {
        return accountDao.getCreditCardsWithPaymentDueDay(getCurrentUserId(), dayOfMonth)
            .map { it.toDomainModel() }
    }
    
    suspend fun getCreditCardsWithDebt(): List<Account> {
        return accountDao.getCreditCardsWithDebt(getCurrentUserId())
            .map { it.toDomainModel() }
    }
    
    suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int = 3
    ) {
        val now = System.currentTimeMillis()
        
        accountDao.updateCreditCardInfo(
            accountId = accountId,
            creditLimitCents = creditLimitCents,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays,
            timestamp = now
        )
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf(
            "creditLimitCents" to creditLimitCents,
            "billingDay" to billingDay,
            "paymentDueDay" to paymentDueDay,
            "gracePeriodDays" to gracePeriodDays
        ))
    }
    
    suspend fun recordCreditCardPayment(accountId: String, paymentAmountCents: Long) {
        // Get current debt amount for this credit card
        val account = accountDao.getAccountById(accountId)
        val dueAmountCents = if (account != null && account.balanceCents < 0) {
            -account.balanceCents
        } else {
            0L
        }
        
        // Determine payment type
        val paymentType = when {
            paymentAmountCents >= dueAmountCents -> PaymentType.FULL
            paymentAmountCents > 0 -> PaymentType.CUSTOM
            else -> PaymentType.CUSTOM
        }
        
        // Use the new method with payment history
        recordCreditCardPaymentWithHistory(
            accountId = accountId,
            paymentAmountCents = paymentAmountCents,
            paymentType = paymentType,
            dueAmountCents = dueAmountCents,
            note = null
        )
    }
    
    private suspend fun logChange(table: String, rowId: String, operation: String, payload: Any) {
        val changeLog = ChangeLogEntity(
            tableName = table,
            rowId = rowId,
            operation = operation,
            payload = gson.toJson(payload),
            timestamp = System.currentTimeMillis()
        )
        changeLogDao.insertChange(changeLog)
    }
    
    // Credit Card Payment History Methods
    suspend fun recordCreditCardPaymentWithHistory(
        accountId: String,
        paymentAmountCents: Long,
        paymentType: PaymentType,
        dueAmountCents: Long,
        note: String? = null
    ) {
        val userId = getCurrentUserId()
        val now = System.currentTimeMillis()
        
        // First update the account balance (payment increases balance for credit cards)
        accountDao.updateBalance(accountId, paymentAmountCents, now)
        
        // Get payment due day from account
        val account = accountDao.getAccountById(accountId)
        val paymentDueDay = account?.paymentDueDay ?: 0
        
        // Check if payment is on time (within grace period)
        val currentDate = java.time.LocalDate.now()
        val dueDate = if (currentDate.dayOfMonth > paymentDueDay) {
            // Due date is in current month
            currentDate.withDayOfMonth(paymentDueDay)
        } else {
            // Due date is in previous month
            currentDate.minusMonths(1).withDayOfMonth(paymentDueDay)
        }
        val gracePeriodDays = account?.gracePeriodDays ?: 3
        val latestPaymentDate = dueDate.plusDays(gracePeriodDays.toLong())
        val isOnTime = !currentDate.isAfter(latestPaymentDate)
        
        // Create payment record
        val paymentRecord = CreditCardPaymentEntity(
            userId = userId,
            accountId = accountId,
            paymentAmountCents = paymentAmountCents,
            paymentType = paymentType,
            paymentDate = now,
            dueAmountCents = dueAmountCents,
            isOnTime = isOnTime,
            note = note
        )
        
        creditCardPaymentDao.insert(paymentRecord)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf(
            "paymentAmountCents" to paymentAmountCents,
            "paymentType" to paymentType.name
        ))
    }
    
    fun getCreditCardPayments(accountId: String): Flow<List<CreditCardPaymentEntity>> {
        return creditCardPaymentDao.getPaymentsByAccount(getCurrentUserId(), accountId)
    }
    
    suspend fun getPaymentStats(accountId: String): PaymentStats {
        val userId = getCurrentUserId()
        val onTimeCount = creditCardPaymentDao.getOnTimePaymentCount(userId, accountId)
        val totalCount = creditCardPaymentDao.getTotalPaymentCount(userId, accountId)
        val totalAmountCents = creditCardPaymentDao.getTotalPaymentAmount(userId, accountId) ?: 0L
        
        return PaymentStats(
            onTimeRate = if (totalCount > 0) (onTimeCount.toDouble() / totalCount) * 100 else 0.0,
            totalPayments = totalCount,
            totalAmountYuan = totalAmountCents / 100.0
        )
    }
    
    suspend fun deletePaymentRecord(paymentId: String) {
        val payment = creditCardPaymentDao.getPaymentById(paymentId)
        if (payment != null) {
            // Reverse the balance update
            accountDao.updateBalance(payment.accountId, -payment.paymentAmountCents, System.currentTimeMillis())
            
            // Soft delete the payment record
            creditCardPaymentDao.softDelete(paymentId)
            
            // Log the change
            logChange("credit_card_payments", paymentId, "DELETE", payment)
        }
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get the actual current user ID
        return "current_user_id"
    }
    
    // Credit Card Bill Management Methods
    
    /**
     * 生成信用卡账单
     */
    suspend fun generateCreditCardBill(accountId: String) {
        val account = accountDao.getAccountById(accountId) ?: return
        if (account.type != "CREDIT_CARD" || account.billingDay == null) return
        
        // 创建本地变量避免 smart cast 问题，使用非空断言因为已经检查过非空
        val billingDay = account.billingDay!!
        val paymentDueDay = account.paymentDueDay
        
        val currentDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (cycleStart, cycleEnd) = CreditCardDateUtils.calculateCurrentBillingCycle(
            billingDay = billingDay,
            currentDate = currentDate
        )
        
        // 检查是否已经生成过该周期的账单
        val cycleStartMillis = cycleStart.toEpochDays().toLong() * 24 * 60 * 60 * 1000
        val cycleEndMillis = cycleEnd.toEpochDays().toLong() * 24 * 60 * 60 * 1000
        
        if (creditCardBillDao.hasBillForPeriod(accountId, cycleStartMillis, cycleEndMillis)) {
            return // 该周期账单已存在
        }
        
        // 获取上期账单（如果有）
        val previousBill = creditCardBillDao.getCurrentBill(accountId)
        val previousBalanceCents = previousBill?.remainingAmountCents ?: 0
        
        // 统计本期消费
        val newChargesCents = transactionDao.getTotalExpenseInBillingCycle(
            accountId = accountId,
            startDate = cycleStartMillis,
            endDate = cycleEndMillis
        ) ?: 0
        
        // 统计本期还款
        val paymentsCents = creditCardPaymentDao.getPaymentsByAccount(
            userId = getCurrentUserId(),
            accountId = accountId
        ).map { payments ->
            payments.filter { 
                it.paymentDate >= cycleStartMillis && 
                it.paymentDate <= cycleEndMillis 
            }.sumOf { it.paymentAmountCents }
        }.first()
        
        // 计算账单总额
        val totalAmountCents = previousBalanceCents + newChargesCents - paymentsCents
        val minimumPaymentCents = (totalAmountCents * 0.1).toLong() // 默认10%最低还款
        
        // 计算还款日
        val paymentDueDate = if (paymentDueDay != null) {
            // 在条件块内，paymentDueDay 已经被检查为非空
            val nonNullPaymentDueDay = paymentDueDay
            CreditCardDateUtils.calculateNextPaymentDate(
                paymentDueDay = nonNullPaymentDueDay,
                billingDay = billingDay,
                currentDate = cycleEnd
            ).toEpochDays().toLong() * 24 * 60 * 60 * 1000
        } else {
            cycleEndMillis + (20 * 24 * 60 * 60 * 1000) // 默认账单日后20天
        }
        
        // 创建账单
        val bill = CreditCardBillEntity(
            id = UUID.randomUUID().toString(),
            userId = getCurrentUserId(),
            accountId = accountId,
            billStartDate = cycleStartMillis,
            billEndDate = cycleEndMillis,
            paymentDueDate = paymentDueDate,
            totalAmountCents = totalAmountCents,
            newChargesCents = newChargesCents,
            previousBalanceCents = previousBalanceCents,
            paymentsCents = paymentsCents,
            adjustmentsCents = 0,
            minimumPaymentCents = minimumPaymentCents,
            isGenerated = true,
            isPaid = totalAmountCents <= 0,
            paidAmountCents = if (totalAmountCents <= 0) totalAmountCents else 0,
            isOverdue = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        creditCardBillDao.insert(bill)
        
        // 记录变更
        logChange("credit_card_bills", bill.id, "INSERT", bill)
    }
    
    /**
     * 获取信用卡当前账单
     */
    suspend fun getCurrentCreditCardBill(accountId: String): CreditCardBillEntity? {
        return creditCardBillDao.getCurrentBill(accountId)
    }
    
    /**
     * 获取信用卡账单列表
     */
    fun getCreditCardBills(accountId: String): Flow<List<CreditCardBillEntity>> {
        return creditCardBillDao.getBillsByAccount(accountId)
    }
    
    /**
     * 获取账单周期内的交易
     */
    suspend fun getTransactionsForBill(billId: String): List<Transaction> {
        val bill = creditCardBillDao.getBillById(billId) ?: return emptyList()
        
        val transactions = transactionDao.getTransactionsByBillingCycle(
            accountId = bill.accountId,
            startDate = bill.billStartDate,
            endDate = bill.billEndDate
        )
        
        return transactions.map { transactionEntity ->
            Transaction(
                id = transactionEntity.id,
                accountId = transactionEntity.accountId,
                amountCents = transactionEntity.amountCents,
                categoryId = transactionEntity.categoryId,
                categoryDetails = null,
                note = transactionEntity.note,
                createdAt = Instant.fromEpochMilliseconds(transactionEntity.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(transactionEntity.updatedAt)
            )
        }
    }
    
    /**
     * 更新账单支付状态
     */
    suspend fun updateBillPaymentStatus(billId: String, paymentAmountCents: Long) {
        creditCardBillDao.updatePaymentStatus(
            billId = billId,
            paymentAmountCents = paymentAmountCents,
            timestamp = System.currentTimeMillis()
        )
        
        val bill = creditCardBillDao.getBillById(billId)
        if (bill != null) {
            logChange("credit_card_bills", billId, "UPDATE", mapOf(
                "paidAmountCents" to bill.paidAmountCents,
                "isPaid" to bill.isPaid
            ))
        }
    }
    
    /**
     * 标记逾期账单
     */
    suspend fun markOverdueBills(accountId: String) {
        val currentDate = System.currentTimeMillis()
        creditCardBillDao.markOverdueBills(accountId, currentDate, currentDate)
    }
}

data class PaymentStats(
    val onTimeRate: Double,
    val totalPayments: Int,
    val totalAmountYuan: Double
)

private fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountType.valueOf(type),
        balanceCents = balanceCents,
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        creditLimitCents = creditLimitCents,
        billingDay = billingDay,
        paymentDueDay = paymentDueDay,
        gracePeriodDays = gracePeriodDays,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}

private fun Account.toEntity(userId: String, updatedAt: Long): AccountEntity {
    return AccountEntity(
        id = id,
        userId = userId,
        name = name,
        type = type.name,
        balanceCents = balanceCents,
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        creditLimitCents = creditLimitCents,
        billingDay = billingDay,
        paymentDueDay = paymentDueDay,
        gracePeriodDays = gracePeriodDays,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.PENDING_SYNC
    )
}