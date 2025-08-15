package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.AccountDao
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardPaymentDao
import com.ccxiaoji.feature.ledger.data.local.dao.CreditCardBillDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionDao
import com.ccxiaoji.feature.ledger.data.local.entity.AccountEntity
import com.ccxiaoji.shared.sync.data.local.entity.ChangeLogEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardBillEntity
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.model.AccountTypeMapping
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.common.utils.CreditCardDateUtils
import com.ccxiaoji.shared.user.api.UserApi
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val changeLogDao: ChangeLogDao,
    private val creditCardPaymentDao: CreditCardPaymentDao,
    private val creditCardBillDao: CreditCardBillDao,
    private val transactionDao: TransactionDao,
    private val userApi: UserApi,
    private val gson: Gson
) : AccountRepository {
    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAccountsByUser(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override suspend fun getAccountById(accountId: String): Account? {
        return accountDao.getAccountById(accountId)?.toDomainModel()
    }
    
    override suspend fun getDefaultAccount(): Account? {
        return accountDao.getDefaultAccount(userApi.getCurrentUserId())?.toDomainModel()
    }
    
    override suspend fun getTotalBalance(): Double {
        val totalCents = accountDao.getTotalBalance(userApi.getCurrentUserId()) ?: 0L
        return totalCents / 100.0
    }
    
    // 新的接口方法实现
    override suspend fun createAccount(
        name: String,
        type: AccountType,
        initialBalanceCents: Long,
        creditLimitCents: Long?,
        billingDay: Int?,
        paymentDueDay: Int?,
        gracePeriodDays: Int?
    ): Long {
        val account = createAccountDetailed(
            name = name,
            type = type,
            initialBalanceCents = initialBalanceCents,
            currency = "CNY",
            icon = null,
            color = "#3A7AFE",
            creditLimitCents = creditLimitCents,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays
        )
        
        return account.id.hashCode().toLong()
    }
    
    // 原有的详细创建方法
    suspend fun createAccountDetailed(
        name: String,
        type: AccountType,
        initialBalanceCents: Long = 0L,
        currency: String = "CNY",
        icon: String? = null,
        color: String? = null,
        creditLimitCents: Long? = null,
        billingDay: Int? = null,
        paymentDueDay: Int? = null,
        gracePeriodDays: Int? = null,
        annualFeeAmountCents: Long? = null,
        annualFeeWaiverThresholdCents: Long? = null,
        cashAdvanceLimitCents: Long? = null,
        interestRate: Double? = null
    ): Account {
        val accountId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = AccountEntity(
            id = accountId,
            userId = userApi.getCurrentUserId(),
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
            annualFeeAmountCents = annualFeeAmountCents,
            annualFeeWaiverThresholdCents = annualFeeWaiverThresholdCents,
            cashAdvanceLimitCents = cashAdvanceLimitCents,
            interestRate = interestRate,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        accountDao.insertAccount(entity)
        
        // Log the change for sync
        logChange("accounts", accountId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    override suspend fun updateAccount(account: Account) {
        val now = System.currentTimeMillis()
        val entity = account.toEntity(userApi.getCurrentUserId(), now)
        
        accountDao.updateAccount(entity)
        
        // Log the change for sync
        logChange("accounts", account.id, "UPDATE", entity)
    }
    
    override suspend fun setDefaultAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        // Clear other defaults
        accountDao.clearDefaultStatus(userApi.getCurrentUserId())
        
        // Set new default
        accountDao.setDefaultAccount(accountId, userApi.getCurrentUserId(), now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("isDefault" to true))
    }
    
    override suspend fun updateBalance(accountId: String, changeAmount: Long) {
        val now = System.currentTimeMillis()
        
        accountDao.updateBalance(accountId, changeAmount, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "UPDATE", mapOf("balanceChange" to changeAmount))
    }
    
    override suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long,
        note: String?
    ) {
        transferBetweenAccountsDetailed(fromAccountId, toAccountId, amountCents)
    }
    
    suspend fun transferBetweenAccountsDetailed(
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
    
    override suspend fun deleteAccount(accountId: String) {
        val now = System.currentTimeMillis()
        
        accountDao.softDeleteAccount(accountId, now)
        
        // Log the change for sync
        logChange("accounts", accountId, "DELETE", mapOf("id" to accountId))
    }
    
    // Credit card specific methods
    fun getCreditCardAccounts(): Flow<List<Account>> {
        return accountDao.getCreditCardAccounts(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    suspend fun getCreditCardsWithBillingDay(dayOfMonth: Int): List<Account> {
        return accountDao.getCreditCardsWithBillingDay(userApi.getCurrentUserId(), dayOfMonth)
            .map { it.toDomainModel() }
    }
    
    override suspend fun getCreditCardsWithPaymentDueDay(dayOfMonth: Int): List<Account> {
        return accountDao.getCreditCardsWithPaymentDueDay(userApi.getCurrentUserId(), dayOfMonth)
            .map { it.toDomainModel() }
    }
    
    override suspend fun getCreditCardsWithDebt(): List<Account> {
        return accountDao.getCreditCardsWithDebt(userApi.getCurrentUserId())
            .map { it.toDomainModel() }
    }
    
    override suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int
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
    
    override suspend fun recordCreditCardPayment(accountId: String, paymentAmountCents: Long) {
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
    override suspend fun recordCreditCardPaymentWithHistory(
        accountId: String,
        paymentAmountCents: Long,
        paymentType: PaymentType,
        dueAmountCents: Long,
        note: String?
    ) {
        val userId = userApi.getCurrentUserId()
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
    
    override fun getCreditCardPayments(accountId: String): Flow<List<CreditCardPaymentEntity>> {
        return creditCardPaymentDao.getPaymentsByAccount(userApi.getCurrentUserId(), accountId)
    }
    
    override suspend fun getPaymentStats(accountId: String): com.ccxiaoji.feature.ledger.domain.repository.PaymentStats {
        val userId = userApi.getCurrentUserId()
        val onTimeCount = creditCardPaymentDao.getOnTimePaymentCount(userId, accountId)
        val totalCount = creditCardPaymentDao.getTotalPaymentCount(userId, accountId)
        val totalAmountCents = creditCardPaymentDao.getTotalPaymentAmount(userId, accountId) ?: 0L
        
        // Get payment type counts - simplified implementation
        // TODO: Add getPaymentCountByType method to CreditCardPaymentDao
        val fullPaymentCount = 0
        val minPaymentCount = 0
        val customPaymentCount = totalCount
        
        return com.ccxiaoji.feature.ledger.domain.repository.PaymentStats(
            totalPayments = totalAmountCents,
            paymentCount = totalCount,
            averagePayment = if (totalCount > 0) totalAmountCents / totalCount else 0L,
            fullPaymentCount = fullPaymentCount,
            minPaymentCount = minPaymentCount,
            customPaymentCount = customPaymentCount,
            onTimePaymentRate = if (totalCount > 0) (onTimeCount.toFloat() / totalCount) * 100 else 0f
        )
    }
    
    override suspend fun deletePaymentRecord(paymentId: String) {
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
    
    // Credit Card Bill Management Methods
    
    /**
     * 生成信用卡账单
     */
    suspend fun generateCreditCardBill(accountId: String) {
        val account = accountDao.getAccountById(accountId) ?: return
        if (account.type != "CREDIT_CARD" || account.billingDay == null) return
        
        val currentDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (cycleStart, cycleEnd) = CreditCardDateUtils.calculateCurrentBillingCycle(
            billingDay = account.billingDay,
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
            userId = userApi.getCurrentUserId(),
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
        val paymentDueDate = if (account.paymentDueDay != null) {
            CreditCardDateUtils.calculateNextPaymentDate(
                paymentDueDay = account.paymentDueDay,
                billingDay = account.billingDay,
                currentDate = cycleEnd
            ).toEpochDays().toLong() * 24 * 60 * 60 * 1000
        } else {
            cycleEndMillis + (20 * 24 * 60 * 60 * 1000) // 默认账单日后20天
        }
        
        // 创建账单
        val bill = CreditCardBillEntity(
            id = UUID.randomUUID().toString(),
            userId = userApi.getCurrentUserId(),
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

private fun AccountEntity.toDomainModel(): Account {
    return Account(
        id = id,
        name = name,
        type = AccountTypeMapping.safeValueOf(type),
        balanceCents = balanceCents,
        currency = currency,
        icon = icon,
        color = color,
        isDefault = isDefault,
        creditLimitCents = creditLimitCents,
        billingDay = billingDay,
        paymentDueDay = paymentDueDay,
        gracePeriodDays = gracePeriodDays,
        annualFeeAmountCents = annualFeeAmountCents,
        annualFeeWaiverThresholdCents = annualFeeWaiverThresholdCents,
        cashAdvanceLimitCents = cashAdvanceLimitCents,
        interestRate = interestRate,
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
        annualFeeAmountCents = annualFeeAmountCents,
        annualFeeWaiverThresholdCents = annualFeeWaiverThresholdCents,
        cashAdvanceLimitCents = cashAdvanceLimitCents,
        interestRate = interestRate,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt,
        syncStatus = SyncStatus.PENDING_SYNC
    )
}