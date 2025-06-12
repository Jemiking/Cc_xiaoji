package com.ccxiaoji.feature.ledger.data

import com.ccxiaoji.core.database.entity.PaymentType
import com.ccxiaoji.feature.ledger.api.AccountItem
import com.ccxiaoji.feature.ledger.api.BudgetAlert
import com.ccxiaoji.feature.ledger.api.BudgetItem
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.CreditCardBill
import com.ccxiaoji.feature.ledger.api.DailyStatistics
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.api.PaymentRecord
import com.ccxiaoji.feature.ledger.api.PaymentStats
import com.ccxiaoji.feature.ledger.api.PeriodStatistics
import com.ccxiaoji.feature.ledger.api.RecurringTransactionItem
import com.ccxiaoji.feature.ledger.api.SavingsContributionItem
import com.ccxiaoji.feature.ledger.api.SavingsGoalItem
import com.ccxiaoji.feature.ledger.api.SavingsGoalsSummary
import com.ccxiaoji.feature.ledger.api.TransactionDetail
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.ledger.api.TransactionStats
import com.ccxiaoji.feature.ledger.data.repository.AccountRepository
import com.ccxiaoji.feature.ledger.data.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.data.repository.StatisticsRepository
import com.ccxiaoji.feature.ledger.data.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LedgerApi的实现类
 * 委托给各个Repository处理具体业务逻辑
 */
@Singleton
class LedgerApiImpl @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val budgetRepository: BudgetRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val ledgerNavigator: LedgerNavigator
) : LedgerApi {
    
    // ========== 统计功能 ==========
    
    override suspend fun getTodayStatistics(): DailyStatistics {
        return statisticsRepository.getTodayStatistics()
    }
    
    override suspend fun getMonthlyExpense(year: Int, month: Int): Double {
        return statisticsRepository.getMonthlyExpense(year, month)
    }
    
    override suspend fun getStatisticsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): PeriodStatistics {
        return statisticsRepository.getStatisticsByDateRange(startDate, endDate)
    }
    
    override suspend fun getTotalBalance(): Double {
        return statisticsRepository.getTotalBalance()
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<TransactionItem>> {
        return statisticsRepository.getRecentTransactions(limit)
    }
    
    // ========== 导航功能 ==========
    
    override fun navigateToLedger() {
        ledgerNavigator.navigateToLedger()
    }
    
    override fun navigateToQuickAdd() {
        ledgerNavigator.navigateToQuickAdd()
    }
    
    override fun navigateToStatistics() {
        ledgerNavigator.navigateToStatistics()
    }
    
    override fun navigateToAccounts() {
        ledgerNavigator.navigateToAccounts()
    }
    
    override fun navigateToCategories() {
        ledgerNavigator.navigateToCategories()
    }
    
    // ========== 分类管理功能 ==========
    
    override suspend fun getAllCategories(): List<CategoryItem> {
        return categoryRepository.getAllCategories()
    }
    
    override suspend fun getCategoriesByType(type: String): List<CategoryItem> {
        return categoryRepository.getCategoriesByType(type)
    }
    
    override suspend fun addCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String?
    ) {
        categoryRepository.createCategory(name, type, icon, color, parentId)
    }
    
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        icon: String,
        color: String
    ) {
        categoryRepository.updateCategory(categoryId, name, icon, color)
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        categoryRepository.deleteCategory(categoryId)
    }
    
    override suspend fun getCategoryUsageCount(categoryId: String): Int {
        return categoryRepository.getCategoryUsageCount(categoryId)
    }
    
    // ========== 交易记录功能 ==========
    
    override suspend fun getTransactionsByMonth(year: Int, month: Int): List<TransactionItem> {
        return transactionRepository.getTransactionsByMonth(getCurrentUserId(), year, month)
    }
    
    override suspend fun getRecentTransactionsList(limit: Int): List<TransactionItem> {
        return transactionRepository.getRecentTransactions(getCurrentUserId(), limit)
    }
    
    override suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String?
    ): String {
        return transactionRepository.addTransaction(
            userId = getCurrentUserId(),
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            accountId = accountId
        )
    }
    
    override suspend fun updateTransaction(
        transactionId: String,
        amountCents: Int,
        categoryId: String,
        note: String?
    ) {
        transactionRepository.updateTransaction(
            userId = getCurrentUserId(),
            transactionId = transactionId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note
        )
    }
    
    override suspend fun deleteTransaction(transactionId: String) {
        transactionRepository.deleteTransaction(getCurrentUserId(), transactionId)
    }
    
    override suspend fun deleteTransactions(transactionIds: List<String>) {
        transactionRepository.deleteTransactions(getCurrentUserId(), transactionIds)
    }
    
    override suspend fun searchTransactions(query: String): List<TransactionItem> {
        return transactionRepository.searchTransactions(getCurrentUserId(), query)
    }
    
    override suspend fun getTransactionsByAccount(accountId: String): List<TransactionItem> {
        return transactionRepository.getTransactionsByAccount(getCurrentUserId(), accountId)
    }
    
    override suspend fun getTransactionDetail(transactionId: String): TransactionDetail? {
        return transactionRepository.getTransactionDetail(transactionId)
    }
    
    override suspend fun getTransactionStatsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionStats {
        return transactionRepository.getTransactionStatsByDateRange(
            userId = getCurrentUserId(),
            startDate = startDate,
            endDate = endDate
        )
    }
    
    override fun navigateToTransactionDetail(transactionId: String) {
        ledgerNavigator.navigateToTransactionDetail(transactionId)
    }
    
    // ========== 账户管理功能 ==========
    
    override fun getAccountsFlow(): Flow<List<AccountItem>> {
        return accountRepository.getAccounts()
            .map { accounts -> accounts.map { it.toAccountItem() } }
    }
    
    override suspend fun getAccounts(): List<AccountItem> {
        return accountRepository.getAccounts()
            .map { accounts -> accounts.map { it.toAccountItem() } }
            .first()
    }
    
    override suspend fun getAccountById(accountId: String): AccountItem? {
        return accountRepository.getAccountById(accountId)?.toAccountItem()
    }
    
    override suspend fun getDefaultAccount(): AccountItem? {
        return accountRepository.getDefaultAccount()?.toAccountItem()
    }
    
    override suspend fun createAccount(
        name: String,
        type: String,
        initialBalanceCents: Long,
        currency: String,
        icon: String?,
        color: String?,
        creditLimitCents: Long?,
        billingDay: Int?,
        paymentDueDay: Int?,
        gracePeriodDays: Int?
    ): AccountItem {
        val account = accountRepository.createAccount(
            name = name,
            type = AccountType.valueOf(type),
            initialBalanceCents = initialBalanceCents,
            currency = currency,
            icon = icon,
            color = color,
            creditLimitCents = creditLimitCents,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays
        )
        return account.toAccountItem()
    }
    
    override suspend fun updateAccount(account: AccountItem) {
        val domainAccount = Account(
            id = account.id,
            name = account.name,
            type = AccountType.valueOf(account.type),
            balanceCents = account.balanceCents,
            currency = account.currency,
            icon = account.icon,
            color = account.color,
            isDefault = account.isDefault,
            creditLimitCents = account.creditLimitCents,
            billingDay = account.billingDay,
            paymentDueDay = account.paymentDueDay,
            gracePeriodDays = account.gracePeriodDays,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
        accountRepository.updateAccount(domainAccount)
    }
    
    override suspend fun setDefaultAccount(accountId: String) {
        accountRepository.setDefaultAccount(accountId)
    }
    
    override suspend fun deleteAccount(accountId: String) {
        accountRepository.deleteAccount(accountId)
    }
    
    override suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Long
    ) {
        accountRepository.transferBetweenAccounts(fromAccountId, toAccountId, amountCents)
    }
    
    override fun getCreditCardAccountsFlow(): Flow<List<AccountItem>> {
        return accountRepository.getCreditCardAccounts()
            .map { accounts -> accounts.map { it.toAccountItem() } }
    }
    
    override suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        paymentDueDay: Int,
        gracePeriodDays: Int
    ) {
        accountRepository.updateCreditCardInfo(
            accountId = accountId,
            creditLimitCents = creditLimitCents,
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = gracePeriodDays
        )
    }
    
    override suspend fun recordCreditCardPayment(
        accountId: String,
        paymentAmountCents: Long,
        paymentType: String,
        dueAmountCents: Long,
        note: String?
    ) {
        accountRepository.recordCreditCardPaymentWithHistory(
            accountId = accountId,
            paymentAmountCents = paymentAmountCents,
            paymentType = PaymentType.valueOf(paymentType),
            dueAmountCents = dueAmountCents,
            note = note
        )
    }
    
    override fun getCreditCardPaymentsFlow(accountId: String): Flow<List<PaymentRecord>> {
        return accountRepository.getCreditCardPayments(accountId)
            .map { payments ->
                payments.map { payment ->
                    PaymentRecord(
                        id = payment.id,
                        accountId = payment.accountId,
                        paymentAmountCents = payment.paymentAmountCents,
                        paymentType = payment.paymentType.name,
                        paymentDate = payment.paymentDate,
                        dueAmountCents = payment.dueAmountCents,
                        isOnTime = payment.isOnTime,
                        note = payment.note
                    )
                }
            }
    }
    
    override suspend fun generateCreditCardBill(accountId: String) {
        accountRepository.generateCreditCardBill(accountId)
    }
    
    override fun getCreditCardBillsFlow(accountId: String): Flow<List<CreditCardBill>> {
        return accountRepository.getCreditCardBills(accountId)
            .map { bills ->
                bills.map { bill ->
                    CreditCardBill(
                        id = bill.id,
                        accountId = bill.accountId,
                        billStartDate = bill.billStartDate,
                        billEndDate = bill.billEndDate,
                        paymentDueDate = bill.paymentDueDate,
                        totalAmountCents = bill.totalAmountCents,
                        newChargesCents = bill.newChargesCents,
                        previousBalanceCents = bill.previousBalanceCents,
                        paymentsCents = bill.paymentsCents,
                        adjustmentsCents = bill.adjustmentsCents,
                        minimumPaymentCents = bill.minimumPaymentCents,
                        isPaid = bill.isPaid,
                        paidAmountCents = bill.paidAmountCents,
                        isOverdue = bill.isOverdue,
                        createdAt = bill.createdAt,
                        updatedAt = bill.updatedAt
                    )
                }
            }
    }
    
    override suspend fun getCurrentCreditCardBill(accountId: String): CreditCardBill? {
        val bill = accountRepository.getCurrentCreditCardBill(accountId) ?: return null
        return CreditCardBill(
            id = bill.id,
            accountId = bill.accountId,
            billStartDate = bill.billStartDate,
            billEndDate = bill.billEndDate,
            paymentDueDate = bill.paymentDueDate,
            totalAmountCents = bill.totalAmountCents,
            newChargesCents = bill.newChargesCents,
            previousBalanceCents = bill.previousBalanceCents,
            paymentsCents = bill.paymentsCents,
            adjustmentsCents = bill.adjustmentsCents,
            minimumPaymentCents = bill.minimumPaymentCents,
            isPaid = bill.isPaid,
            paidAmountCents = bill.paidAmountCents,
            isOverdue = bill.isOverdue,
            createdAt = bill.createdAt,
            updatedAt = bill.updatedAt
        )
    }
    
    override suspend fun getTransactionsForBill(billId: String): List<TransactionItem> {
        return accountRepository.getTransactionsForBill(billId)
    }
    
    override suspend fun updateBillPaymentStatus(billId: String, paymentAmountCents: Long) {
        accountRepository.updateBillPaymentStatus(billId, paymentAmountCents)
    }
    
    // ========== 预算管理功能 ==========
    
    override fun getBudgetsWithSpent(year: Int, month: Int): Flow<List<BudgetItem>> {
        return budgetRepository.getBudgetsWithSpent(getCurrentUserId(), year, month)
            .map { budgets ->
                budgets.map { it.toBudgetItem() }
            }
    }
    
    override suspend fun getTotalBudget(year: Int, month: Int): BudgetItem? {
        return budgetRepository.getTotalBudgetWithSpent(getCurrentUserId(), year, month)?.toBudgetItem()
    }
    
    override suspend fun getCategoryBudget(year: Int, month: Int, categoryId: String): BudgetItem? {
        return budgetRepository.getCategoryBudgetWithSpent(getCurrentUserId(), year, month, categoryId)?.toBudgetItem()
    }
    
    override suspend fun upsertBudget(
        year: Int,
        month: Int,
        budgetAmountCents: Int,
        categoryId: String?,
        alertThreshold: Float,
        note: String?
    ): BudgetItem {
        val budgetEntity = budgetRepository.upsertBudget(
            userId = getCurrentUserId(),
            year = year,
            month = month,
            budgetAmountCents = budgetAmountCents,
            categoryId = categoryId,
            alertThreshold = alertThreshold,
            note = note
        )
        
        // 获取带花费信息的预算
        val budgetWithSpent = if (categoryId == null) {
            budgetRepository.getTotalBudgetWithSpent(getCurrentUserId(), year, month)
        } else {
            budgetRepository.getCategoryBudgetWithSpent(getCurrentUserId(), year, month, categoryId)
        }
        
        return budgetWithSpent?.toBudgetItem() ?: budgetEntity.toBudgetItem()
    }
    
    override suspend fun updateBudget(
        budgetId: String,
        budgetAmountCents: Int?,
        alertThreshold: Float?,
        note: String?
    ): BudgetItem? {
        val updatedBudget = budgetRepository.updateBudget(budgetId, budgetAmountCents, alertThreshold, note)
            ?: return null
        
        // 获取带花费信息的预算
        val budgetWithSpent = if (updatedBudget.categoryId == null) {
            budgetRepository.getTotalBudgetWithSpent(getCurrentUserId(), updatedBudget.year, updatedBudget.month)
        } else {
            budgetRepository.getCategoryBudgetWithSpent(getCurrentUserId(), updatedBudget.year, updatedBudget.month, updatedBudget.categoryId!!)
        }
        
        return budgetWithSpent?.toBudgetItem() ?: updatedBudget.toBudgetItem()
    }
    
    override suspend fun deleteBudget(budgetId: String) {
        budgetRepository.deleteBudget(budgetId)
    }
    
    override suspend fun checkBudgetExceeded(year: Int, month: Int, categoryId: String?): Boolean {
        return budgetRepository.checkBudgetExceeded(getCurrentUserId(), year, month, categoryId)
    }
    
    override suspend fun checkBudgetAlert(year: Int, month: Int, categoryId: String?): Boolean {
        return budgetRepository.checkBudgetAlert(getCurrentUserId(), year, month, categoryId)
    }
    
    override suspend fun getBudgetUsagePercentage(year: Int, month: Int, categoryId: String?): Float? {
        return budgetRepository.getBudgetUsagePercentage(getCurrentUserId(), year, month, categoryId)
    }
    
    override suspend fun getBudgetAlerts(year: Int, month: Int): List<BudgetAlert> {
        val budgets = budgetRepository.getBudgetsWithSpent(getCurrentUserId(), year, month).first()
        return budgets.filter { it.spentAmountCents.toFloat() / it.budgetAmountCents.toFloat() >= it.alertThreshold }
            .map { budget ->
                // 获取分类名称（如果有分类ID）
                val categoryName = budget.categoryId?.let { categoryId ->
                    categoryRepository.getCategoryById(categoryId)?.name
                }
                
                BudgetAlert(
                    budgetId = budget.id,
                    categoryName = categoryName,
                    budgetAmountYuan = budget.budgetAmountCents / 100.0,
                    spentAmountYuan = budget.spentAmountCents / 100.0,
                    usagePercentage = (budget.spentAmountCents.toFloat() / budget.budgetAmountCents.toFloat()) * 100,
                    isExceeded = budget.spentAmountCents > budget.budgetAmountCents,
                    alertThreshold = budget.alertThreshold
                )
            }
    }
    
    // ========== 存钱目标管理功能 ==========
    
    override fun getSavingsGoalsFlow(): Flow<List<SavingsGoalItem>> {
        return savingsGoalRepository.getAllSavingsGoals()
            .map { goals -> goals.map { it.toSavingsGoalItem() } }
    }
    
    override suspend fun getSavingsGoals(): List<SavingsGoalItem> {
        return savingsGoalRepository.getAllSavingsGoals()
            .map { goals -> goals.map { it.toSavingsGoalItem() } }
            .first()
    }
    
    override suspend fun getActiveSavingsGoals(): List<SavingsGoalItem> {
        return savingsGoalRepository.getActiveSavingsGoals()
            .map { goals -> goals.map { it.toSavingsGoalItem() } }
            .first()
    }
    
    override suspend fun getSavingsGoalById(goalId: Long): SavingsGoalItem? {
        return savingsGoalRepository.getSavingsGoalById(goalId)?.toSavingsGoalItem()
    }
    
    override suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate?,
        description: String?,
        color: String,
        iconName: String
    ): SavingsGoalItem {
        val goal = SavingsGoal(
            name = name,
            targetAmountCents = targetAmountCents,
            targetDate = targetDate,
            description = description,
            color = color,
            iconName = iconName,
            createdAt = kotlinx.datetime.Clock.System.now(),
            updatedAt = kotlinx.datetime.Clock.System.now()
        )
        val goalId = savingsGoalRepository.createSavingsGoal(goal)
        return goal.copy(id = goalId).toSavingsGoalItem()
    }
    
    override suspend fun updateSavingsGoal(
        goalId: Long,
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate?,
        description: String?,
        color: String,
        iconName: String
    ): SavingsGoalItem? {
        val existingGoal = savingsGoalRepository.getSavingsGoalById(goalId) ?: return null
        val updatedGoal = existingGoal.copy(
            name = name,
            targetAmountCents = targetAmountCents,
            targetDate = targetDate,
            description = description,
            color = color,
            iconName = iconName,
            updatedAt = kotlinx.datetime.Clock.System.now()
        )
        savingsGoalRepository.updateSavingsGoal(updatedGoal)
        return updatedGoal.toSavingsGoalItem()
    }
    
    override suspend fun deleteSavingsGoal(goalId: Long) {
        savingsGoalRepository.deleteSavingsGoalById(goalId)
    }
    
    override suspend fun setSavingsGoalActive(goalId: Long, isActive: Boolean) {
        savingsGoalRepository.setSavingsGoalActive(goalId, isActive)
    }
    
    override suspend fun addSavingsContribution(
        goalId: Long,
        amountCents: Long,
        note: String?
    ): SavingsContributionItem {
        val contribution = SavingsContribution(
            goalId = goalId,
            amountCents = amountCents,
            note = note,
            createdAt = kotlinx.datetime.Clock.System.now()
        )
        val contributionId = savingsGoalRepository.addContribution(contribution)
        return contribution.copy(id = contributionId).toSavingsContributionItem()
    }
    
    override suspend fun getSavingsContributions(goalId: Long): List<SavingsContributionItem> {
        return savingsGoalRepository.getRecentContributions(goalId, Int.MAX_VALUE)
            .map { it.toSavingsContributionItem() }
    }
    
    override suspend fun deleteSavingsContribution(contributionId: Long) {
        savingsGoalRepository.deleteContributionById(contributionId)
    }
    
    override suspend fun getSavingsGoalsSummary(): SavingsGoalsSummary {
        val summary = savingsGoalRepository.getSavingsGoalsSummary()
        return SavingsGoalsSummary(
            activeGoalsCount = summary.activeGoalsCount,
            completedGoalsCount = summary.completedGoalsCount,
            totalTargetAmountCents = summary.totalTargetAmountCents,
            totalCurrentAmountCents = summary.totalCurrentAmountCents,
            totalProgress = summary.totalProgress
        )
    }
    
    override suspend fun getActiveSavingsGoalsCount(): Int {
        return savingsGoalRepository.getActiveSavingsGoalsCount()
    }
    
    override fun navigateToSavingsGoals() {
        ledgerNavigator.navigateToSavingsGoals()
    }
    
    override fun navigateToSavingsGoalDetail(goalId: Long) {
        ledgerNavigator.navigateToSavingsGoalDetail(goalId)
    }
    
    private fun getCurrentUserId(): String {
        // 在实际应用中，这应该从用户状态管理获取
        return "current_user_id"
    }
    
    // 扩展函数：将领域模型转换为API模型
    
    /**
     * 将BudgetWithSpent转换为BudgetItem
     */
    private suspend fun com.ccxiaoji.core.database.dao.BudgetWithSpent.toBudgetItem(): BudgetItem {
        // 获取分类信息
        val category = categoryId?.let { categoryRepository.getCategoryById(it) }
        
        return BudgetItem(
            id = id,
            year = year,
            month = month,
            categoryId = categoryId,
            categoryName = category?.name,
            categoryIcon = category?.icon,
            categoryColor = category?.color,
            budgetAmountCents = budgetAmountCents,
            spentAmountCents = spentAmountCents,
            alertThreshold = alertThreshold,
            note = note,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }
    
    /**
     * 将BudgetEntity转换为BudgetItem（无花费信息）
     */
    private suspend fun com.ccxiaoji.core.database.entity.BudgetEntity.toBudgetItem(): BudgetItem {
        // 获取分类信息
        val category = categoryId?.let { categoryRepository.getCategoryById(it) }
        
        return BudgetItem(
            id = id,
            year = year,
            month = month,
            categoryId = categoryId,
            categoryName = category?.name,
            categoryIcon = category?.icon,
            categoryColor = category?.color,
            budgetAmountCents = budgetAmountCents,
            spentAmountCents = 0, // BudgetEntity没有花费信息
            alertThreshold = alertThreshold,
            note = note,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt)
        )
    }
    
    private fun Account.toAccountItem(): AccountItem {
        return AccountItem(
            id = id,
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
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * 将SavingsGoal转换为SavingsGoalItem
     */
    private fun SavingsGoal.toSavingsGoalItem(): SavingsGoalItem {
        return SavingsGoalItem(
            id = id,
            name = name,
            targetAmountCents = targetAmountCents,
            currentAmountCents = currentAmountCents,
            targetDate = targetDate,
            description = description,
            color = color,
            iconName = iconName,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    /**
     * 将SavingsContribution转换为SavingsContributionItem
     */
    private fun SavingsContribution.toSavingsContributionItem(): SavingsContributionItem {
        return SavingsContributionItem(
            id = id,
            goalId = goalId,
            amountCents = amountCents,
            note = note,
            createdAt = createdAt
        )
    }
    
    // ==================== 信用卡管理相关实现 ====================
    
    override fun getCreditCards(): Flow<List<AccountItem>> {
        return accountRepository.getCreditCardAccounts()
            .map { accounts -> accounts.map { it.toAccountItem() } }
    }
    
    override suspend fun addCreditCard(
        name: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    ) {
        accountRepository.createAccount(
            name = name,
            type = AccountType.CREDIT_CARD,
            initialBalanceCents = -(usedAmountYuan * 100).toLong(), // 信用卡余额为负数表示欠款
            creditLimitCents = (creditLimitYuan * 100).toLong(),
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = 3 // 默认3天宽限期
        )
    }
    
    override suspend fun updateCreditCardInfo(
        accountId: String,
        creditLimitYuan: Double,
        usedAmountYuan: Double,
        billingDay: Int,
        paymentDueDay: Int
    ) {
        // 先获取当前账户信息
        val currentAccount = accountRepository.getAccountById(accountId)
        if (currentAccount != null) {
            // 计算余额变化（信用卡余额为负数表示欠款）
            val newBalanceCents = -(usedAmountYuan * 100).toLong()
            val balanceChangeCents = newBalanceCents - currentAccount.balanceCents
            
            // 更新余额
            if (balanceChangeCents != 0L) {
                accountRepository.updateBalance(accountId, balanceChangeCents)
            }
        }
        
        // 更新信用卡信息
        accountRepository.updateCreditCardInfo(
            accountId = accountId,
            creditLimitCents = (creditLimitYuan * 100).toLong(),
            billingDay = billingDay,
            paymentDueDay = paymentDueDay,
            gracePeriodDays = 3
        )
    }
    
    override suspend fun recordCreditCardPayment(
        accountId: String,
        paymentAmountYuan: Double,
        paymentType: String,
        note: String?
    ) {
        // 获取当前欠款金额
        val account = accountRepository.getAccountById(accountId)
        val dueAmountCents = if (account != null && account.balanceCents < 0) {
            -account.balanceCents
        } else {
            0L
        }
        
        // 将字符串转换为 PaymentType 枚举
        val type = when (paymentType) {
            "FULL" -> com.ccxiaoji.core.database.entity.PaymentType.FULL
            "MINIMUM" -> com.ccxiaoji.core.database.entity.PaymentType.MINIMUM
            else -> com.ccxiaoji.core.database.entity.PaymentType.CUSTOM
        }
        
        accountRepository.recordCreditCardPaymentWithHistory(
            accountId = accountId,
            paymentAmountCents = (paymentAmountYuan * 100).toLong(),
            paymentType = type,
            dueAmountCents = dueAmountCents,
            note = note
        )
    }
    
    override fun getCreditCardPayments(accountId: String): Flow<List<PaymentRecord>> {
        return accountRepository.getCreditCardPayments(accountId)
            .map { payments -> 
                payments.map { payment ->
                    PaymentRecord(
                        id = payment.id,
                        accountId = payment.accountId,
                        paymentAmountCents = payment.paymentAmountCents,
                        paymentType = payment.paymentType.name,
                        paymentDate = payment.paymentDate,
                        dueAmountCents = payment.dueAmountCents,
                        isOnTime = payment.isOnTime,
                        note = payment.note
                    )
                }
            }
    }
    
    override suspend fun getPaymentStats(accountId: String): PaymentStats {
        val stats = accountRepository.getPaymentStats(accountId)
        return PaymentStats(
            onTimeRate = stats.onTimeRate,
            totalPayments = stats.totalPayments,
            totalAmountYuan = stats.totalAmountYuan
        )
    }
    
    override suspend fun deletePaymentRecord(paymentId: String) {
        accountRepository.deletePaymentRecord(paymentId)
    }
    
    override suspend fun checkPaymentReminders(): List<AccountItem> {
        val currentDay = java.time.LocalDate.now().dayOfMonth
        val cardsWithPaymentDue = accountRepository.getCreditCardsWithPaymentDueDay(currentDay)
        val cardsWithDebt = accountRepository.getCreditCardsWithDebt()
        
        val cardsNeedingPayment = cardsWithPaymentDue.intersect(cardsWithDebt.toSet())
        return cardsNeedingPayment.map { it.toAccountItem() }
    }
    
    // ==================== 信用卡账单相关实现 ====================
    
    override fun getCreditCardBills(accountId: String): Flow<List<CreditCardBill>> {
        return accountRepository.getCreditCardBills(accountId)
            .map { bills ->
                bills.map { bill ->
                    CreditCardBill(
                        id = bill.id,
                        accountId = bill.accountId,
                        billStartDate = bill.billStartDate,
                        billEndDate = bill.billEndDate,
                        paymentDueDate = bill.paymentDueDate,
                        totalAmountCents = bill.totalAmountCents,
                        newChargesCents = bill.newChargesCents,
                        previousBalanceCents = bill.previousBalanceCents,
                        paymentsCents = bill.paymentsCents,
                        adjustmentsCents = bill.adjustmentsCents,
                        minimumPaymentCents = bill.minimumPaymentCents,
                        isPaid = bill.isPaid,
                        paidAmountCents = bill.paidAmountCents,
                        isOverdue = bill.isOverdue,
                        createdAt = bill.createdAt,
                        updatedAt = bill.updatedAt
                    )
                }
            }
    }
    
    override suspend fun getCreditCardBillDetail(billId: String): CreditCardBill? {
        return getCurrentCreditCardBill(billId) // 复用getCurrentCreditCardBill的逻辑
    }
    
    override suspend fun markOverdueBills(accountId: String) {
        accountRepository.markOverdueBills(accountId)
    }
    
    override suspend fun getCreditCardsWithPaymentDue(dayOfMonth: Int): List<AccountItem> {
        val cards = accountRepository.getCreditCardsWithPaymentDueDay(dayOfMonth)
        return cards.map { it.toAccountItem() }
    }
    
    override suspend fun getCreditCardsWithDebt(): List<AccountItem> {
        val cards = accountRepository.getCreditCardsWithDebt()
        return cards.map { it.toAccountItem() }
    }
    
    override fun navigateToCreditCards() {
        ledgerNavigator.navigateToCreditCards()
    }
    
    override fun navigateToCreditCardBills(accountId: String) {
        ledgerNavigator.navigateToCreditCardBills(accountId)
    }
    
    override fun navigateToTransactionsByAccount(accountId: String) {
        ledgerNavigator.navigateToTransactionsByAccount(accountId)
    }
    
    // ========== 定期交易管理功能 ==========
    
    override fun getAllRecurringTransactions(): Flow<List<RecurringTransactionItem>> {
        return recurringTransactionRepository.getAllRecurringTransactions(getCurrentUserId())
            .map { transactions ->
                transactions.map { transaction ->
                    val category = categoryRepository.getCategoryById(transaction.categoryId)
                    val account = accountRepository.getAccountById(transaction.accountId)
                    
                    RecurringTransactionItem(
                        id = transaction.id,
                        name = transaction.name,
                        accountId = transaction.accountId,
                        accountName = account?.name,
                        amountCents = transaction.amountCents,
                        categoryId = transaction.categoryId,
                        categoryName = category?.name,
                        categoryIcon = category?.icon,
                        categoryColor = category?.color,
                        note = transaction.note,
                        frequency = transaction.frequency.name,
                        dayOfWeek = transaction.dayOfWeek,
                        dayOfMonth = transaction.dayOfMonth,
                        monthOfYear = transaction.monthOfYear,
                        startDate = transaction.startDate,
                        endDate = transaction.endDate,
                        isEnabled = transaction.isEnabled,
                        lastExecutionDate = transaction.lastExecutionDate,
                        nextExecutionDate = transaction.nextExecutionDate,
                        createdAt = transaction.createdAt,
                        updatedAt = transaction.updatedAt
                    )
                }
            }
    }
    
    override fun getEnabledRecurringTransactions(): Flow<List<RecurringTransactionItem>> {
        return recurringTransactionRepository.getEnabledRecurringTransactions(getCurrentUserId())
            .map { transactions ->
                transactions.map { transaction ->
                    val category = categoryRepository.getCategoryById(transaction.categoryId)
                    val account = accountRepository.getAccountById(transaction.accountId)
                    
                    RecurringTransactionItem(
                        id = transaction.id,
                        name = transaction.name,
                        accountId = transaction.accountId,
                        accountName = account?.name,
                        amountCents = transaction.amountCents,
                        categoryId = transaction.categoryId,
                        categoryName = category?.name,
                        categoryIcon = category?.icon,
                        categoryColor = category?.color,
                        note = transaction.note,
                        frequency = transaction.frequency.name,
                        dayOfWeek = transaction.dayOfWeek,
                        dayOfMonth = transaction.dayOfMonth,
                        monthOfYear = transaction.monthOfYear,
                        startDate = transaction.startDate,
                        endDate = transaction.endDate,
                        isEnabled = transaction.isEnabled,
                        lastExecutionDate = transaction.lastExecutionDate,
                        nextExecutionDate = transaction.nextExecutionDate,
                        createdAt = transaction.createdAt,
                        updatedAt = transaction.updatedAt
                    )
                }
            }
    }
    
    override suspend fun getRecurringTransactionById(id: String): RecurringTransactionItem? {
        val transaction = recurringTransactionRepository.getRecurringTransactionById(id) ?: return null
        val category = categoryRepository.getCategoryById(transaction.categoryId)
        val account = accountRepository.getAccountById(transaction.accountId)
        
        return RecurringTransactionItem(
            id = transaction.id,
            name = transaction.name,
            accountId = transaction.accountId,
            accountName = account?.name,
            amountCents = transaction.amountCents,
            categoryId = transaction.categoryId,
            categoryName = category?.name,
            categoryIcon = category?.icon,
            categoryColor = category?.color,
            note = transaction.note,
            frequency = transaction.frequency.name,
            dayOfWeek = transaction.dayOfWeek,
            dayOfMonth = transaction.dayOfMonth,
            monthOfYear = transaction.monthOfYear,
            startDate = transaction.startDate,
            endDate = transaction.endDate,
            isEnabled = transaction.isEnabled,
            lastExecutionDate = transaction.lastExecutionDate,
            nextExecutionDate = transaction.nextExecutionDate,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )
    }
    
    override suspend fun createRecurringTransaction(
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String?,
        frequency: String,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        monthOfYear: Int?,
        startDate: Long,
        endDate: Long?
    ): RecurringTransactionItem {
        val id = recurringTransactionRepository.createRecurringTransaction(
            userId = getCurrentUserId(),
            name = name,
            accountId = accountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            frequency = com.ccxiaoji.core.database.model.RecurringFrequency.valueOf(frequency),
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            monthOfYear = monthOfYear,
            startDate = startDate,
            endDate = endDate
        )
        
        return getRecurringTransactionById(id)!!
    }
    
    override suspend fun updateRecurringTransaction(
        id: String,
        name: String,
        accountId: String,
        amountCents: Int,
        categoryId: String,
        note: String?,
        frequency: String,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        monthOfYear: Int?,
        startDate: Long,
        endDate: Long?
    ) {
        recurringTransactionRepository.updateRecurringTransaction(
            id = id,
            name = name,
            accountId = accountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            frequency = com.ccxiaoji.core.database.model.RecurringFrequency.valueOf(frequency),
            dayOfWeek = dayOfWeek,
            dayOfMonth = dayOfMonth,
            monthOfYear = monthOfYear,
            startDate = startDate,
            endDate = endDate
        )
    }
    
    override suspend fun toggleRecurringTransactionEnabled(id: String) {
        recurringTransactionRepository.toggleEnabled(id)
    }
    
    override suspend fun deleteRecurringTransaction(id: String) {
        recurringTransactionRepository.deleteRecurringTransaction(id)
    }
    
    override suspend fun executeDueRecurringTransactions(): Int {
        return recurringTransactionRepository.executeDueRecurringTransactions()
    }
    
    override fun navigateToRecurringTransactions() {
        ledgerNavigator.navigateToRecurringTransactions()
    }
}