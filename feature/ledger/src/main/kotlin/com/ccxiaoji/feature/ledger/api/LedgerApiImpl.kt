package com.ccxiaoji.feature.ledger.api

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.presentation.screen.account.AccountScreen
import com.ccxiaoji.feature.ledger.presentation.screen.budget.BudgetScreen
import com.ccxiaoji.feature.ledger.presentation.screen.category.CategoryManagementScreen
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardBillsScreen
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardScreen
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.LedgerScreen
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.TransactionDetailScreen
import com.ccxiaoji.feature.ledger.presentation.screen.recurring.RecurringTransactionScreen
import com.ccxiaoji.feature.ledger.presentation.screen.savings.SavingsGoalDetailScreen
import com.ccxiaoji.feature.ledger.presentation.screen.savings.SavingsGoalScreen
import com.ccxiaoji.feature.ledger.presentation.screen.statistics.StatisticsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class LedgerApiImpl @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository
) : LedgerApi {
    
    // Transaction methods
    override fun getTransactions(): Flow<List<Transaction>> {
        return transactionRepository.getTransactions()
    }
    
    override fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByDateRange(startDate, endDate)
    }
    
    override fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByAccount(accountId)
    }
    
    override fun getTransactionsByAccountAndDateRange(
        accountId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByAccountAndDateRange(accountId, startDate, endDate)
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> {
        return transactionRepository.getRecentTransactions(limit)
    }
    
    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionRepository.searchTransactions(query)
    }
    
    override suspend fun getMonthlyTotal(year: Int, month: Int): Int {
        return transactionRepository.getMonthlyTotal(year, month).getOrThrow()
    }
    
    override suspend fun getMonthlyIncomesAndExpenses(year: Int, month: Int): Pair<Int, Int> {
        return transactionRepository.getMonthlyIncomesAndExpenses(year, month).getOrThrow()
    }
    
    override suspend fun getTodayExpense(): Double {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val (_, expense) = transactionRepository.getMonthlyIncomesAndExpenses(today.year, today.monthNumber).getOrThrow()
        return expense / 100.0
    }
    
    override suspend fun getTotalBalance(): Double {
        return accountRepository.getTotalBalance()
    }
    
    override suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String?,
        createdAt: Long
    ): Transaction {
        val transactionId = transactionRepository.addTransaction(amountCents, categoryId, note, accountId ?: "")
        // TODO: 返回完整的Transaction对象而不是仅仅ID
        return Transaction(
            id = transactionId.toString(),
            accountId = accountId ?: "",
            amountCents = amountCents,
            categoryId = categoryId,
            categoryDetails = null,
            note = note,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(createdAt)
        )
    }
    
    override suspend fun updateTransaction(transaction: Transaction) {
        transactionRepository.updateTransaction(transaction)
    }
    
    override suspend fun deleteTransaction(transactionId: String) {
        transactionRepository.deleteTransaction(transactionId)
    }
    
    // Account methods
    override fun getAccounts(): Flow<List<Account>> {
        return accountRepository.getAccounts()
    }
    
    override suspend fun getAccountById(accountId: String): Account? {
        return accountRepository.getAccountById(accountId)
    }
    
    override suspend fun createAccount(
        name: String,
        type: String,
        balanceCents: Long,
        currency: String,
        isDefault: Boolean
    ): Account {
        val accountId = accountRepository.createAccount(
            name = name,
            type = AccountType.valueOf(type),
            initialBalanceCents = balanceCents,
            creditLimitCents = null,
            billingDay = null,
            paymentDueDay = null,
            gracePeriodDays = null
        )
        // TODO: 返回完整的Account对象而不是仅仅ID
        return Account(
            id = accountId.toString(),
            name = name,
            type = AccountType.valueOf(type),
            balanceCents = balanceCents,
            currency = currency,
            isDefault = isDefault,
            icon = null,
            color = "#3A7AFE",
            createdAt = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            updatedAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
    }
    
    override suspend fun updateAccount(account: Account) {
        accountRepository.updateAccount(account)
    }
    
    override suspend fun deleteAccount(accountId: String) {
        accountRepository.deleteAccount(accountId)
    }
    
    override suspend fun updateAccountBalance(accountId: String, newBalanceCents: Long) {
        val account = accountRepository.getAccountById(accountId)
        if (account != null) {
            accountRepository.updateAccount(account.copy(balanceCents = newBalanceCents))
        }
    }
    
    override suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Int,
        note: String?
    ) {
        accountRepository.transferBetweenAccounts(fromAccountId, toAccountId, amountCents.toLong(), null)
    }
    
    // Category methods
    override fun getCategories(): Flow<List<Category>> {
        return categoryRepository.getCategories()
    }
    
    override fun getCategoriesByType(type: String): Flow<List<Category>> {
        return categoryRepository.getCategoriesByType(Category.Type.valueOf(type))
    }
    
    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }
    
    override suspend fun createCategory(
        name: String,
        type: String,
        icon: String?,
        color: String,
        parentId: String?
    ): Category {
        val categoryId = categoryRepository.createCategory(
            name = name,
            type = type,
            icon = icon ?: "📝",
            color = color,
            parentId = parentId
        )
        return categoryRepository.getCategoryById(categoryId.toString())!!
    }
    
    override suspend fun updateCategory(category: Category) {
        categoryRepository.updateCategory(category)
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        categoryRepository.deleteCategory(categoryId)
    }
    
    // Statistics methods
    override suspend fun getDailyTotals(
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Pair<Int, Int>> {
        return transactionRepository.getDailyTotals(startDate, endDate).getOrThrow()
    }
    
    override suspend fun getCategoryStatistics(
        startDate: LocalDate,
        endDate: LocalDate,
        type: String
    ): List<CategoryStatistic> {
        return transactionRepository.getCategoryStatistics(
            categoryType = type,
            startDate = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            endDate = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        ).getOrThrow()
    }
    
    override suspend fun getTopTransactions(
        startDate: LocalDate,
        endDate: LocalDate,
        type: String,
        limit: Int
    ): List<Transaction> {
        return transactionRepository.getTopTransactions(startDate, endDate, type, limit).getOrThrow()
    }
    
    override suspend fun calculateSavingsRate(startDate: LocalDate, endDate: LocalDate): Float {
        return transactionRepository.calculateSavingsRate(startDate, endDate).getOrThrow()
    }
    
    // Recurring transaction methods
    override fun getRecurringTransactions(): Flow<List<RecurringTransaction>> {
        return recurringTransactionRepository.getAllRecurringTransactions().map { entities ->
            entities.map { entity ->
                RecurringTransaction(
                    id = entity.id,
                    name = entity.name,
                    accountId = entity.accountId,
                    amountCents = entity.amountCents,
                    categoryId = entity.categoryId,
                    note = entity.note,
                    frequency = entity.frequency,
                    dayOfWeek = entity.dayOfWeek,
                    dayOfMonth = entity.dayOfMonth,
                    monthOfYear = entity.monthOfYear,
                    startDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.startDate),
                    endDate = entity.endDate?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
                    isEnabled = entity.isEnabled,
                    lastExecutionDate = entity.lastExecutionDate?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
                    nextExecutionDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.nextExecutionDate),
                    createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.createdAt),
                    updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.updatedAt)
                )
            }
        }
    }
    
    override suspend fun createRecurringTransaction(
        amountCents: Int,
        categoryId: String,
        accountId: String,
        note: String?,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate?
    ): RecurringTransaction {
        val frequency = com.ccxiaoji.common.model.RecurringFrequency.valueOf(period)
        val startMillis = startDate.atStartOfDayIn(kotlinx.datetime.TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate?.atStartOfDayIn(kotlinx.datetime.TimeZone.currentSystemDefault())?.toEpochMilliseconds()
        
        recurringTransactionRepository.createRecurringTransaction(
            name = note ?: "定期交易",
            accountId = accountId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            frequency = frequency,
            startDate = startMillis,
            endDate = endMillis
        )
        
        // 获取创建的交易
        val entities = recurringTransactionRepository.getAllRecurringTransactions().first()
        val entity = entities.last() // 最新创建的
        
        return RecurringTransaction(
            id = entity.id,
            name = entity.name,
            accountId = entity.accountId,
            amountCents = entity.amountCents,
            categoryId = entity.categoryId,
            note = entity.note,
            frequency = entity.frequency,
            dayOfWeek = entity.dayOfWeek,
            dayOfMonth = entity.dayOfMonth,
            monthOfYear = entity.monthOfYear,
            startDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.startDate),
            endDate = entity.endDate?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
            isEnabled = entity.isEnabled,
            lastExecutionDate = entity.lastExecutionDate?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
            nextExecutionDate = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.nextExecutionDate),
            createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(entity.updatedAt)
        )
    }
    
    override suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactionRepository.updateRecurringTransaction(
            id = recurringTransaction.id,
            name = recurringTransaction.name,
            accountId = recurringTransaction.accountId,
            amountCents = recurringTransaction.amountCents,
            categoryId = recurringTransaction.categoryId,
            note = recurringTransaction.note,
            frequency = recurringTransaction.frequency,
            dayOfWeek = recurringTransaction.dayOfWeek,
            dayOfMonth = recurringTransaction.dayOfMonth,
            monthOfYear = recurringTransaction.monthOfYear,
            startDate = recurringTransaction.startDate.toEpochMilliseconds(),
            endDate = recurringTransaction.endDate?.toEpochMilliseconds()
        )
    }
    
    override suspend fun deleteRecurringTransaction(recurringTransactionId: String) {
        recurringTransactionRepository.deleteRecurringTransaction(recurringTransactionId)
    }
    
    override suspend fun pauseRecurringTransaction(recurringTransactionId: String) {
        recurringTransactionRepository.toggleEnabled(recurringTransactionId)
    }
    
    override suspend fun resumeRecurringTransaction(recurringTransactionId: String) {
        recurringTransactionRepository.toggleEnabled(recurringTransactionId)
    }
    
    // Budget methods
    override fun getBudgets(): Flow<List<Budget>> {
        return budgetRepository.getBudgets().map { entities ->
            entities.map { entity ->
                Budget(
                    id = entity.id,
                    userId = entity.userId,
                    year = entity.year,
                    month = entity.month,
                    categoryId = entity.categoryId,
                    budgetAmountCents = entity.budgetAmountCents,
                    alertThreshold = entity.alertThreshold,
                    note = entity.note,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }
    
    override fun getBudgetsByMonth(year: Int, month: Int): Flow<List<Budget>> {
        return budgetRepository.getBudgetsWithSpent(year, month).map { budgetsWithSpent ->
            budgetsWithSpent.map { budgetWithSpent ->
                Budget(
                    id = budgetWithSpent.id,
                    userId = budgetWithSpent.userId,
                    year = budgetWithSpent.year,
                    month = budgetWithSpent.month,
                    categoryId = budgetWithSpent.categoryId,
                    budgetAmountCents = budgetWithSpent.budgetAmountCents,
                    alertThreshold = budgetWithSpent.alertThreshold,
                    note = budgetWithSpent.note,
                    createdAt = budgetWithSpent.createdAt,
                    updatedAt = budgetWithSpent.updatedAt
                )
            }
        }
    }
    
    override suspend fun createBudget(
        categoryId: String,
        amountCents: Long,
        period: String,
        year: Int,
        month: Int?
    ): Budget {
        val budgetId = budgetRepository.createBudget(
            year = year,
            month = month ?: 0,
            categoryId = categoryId,
            amountCents = amountCents.toInt()
        )
        
        // TODO: 返回完整的Budget对象而不是仅仅ID
        return Budget(
            id = budgetId.toString(),
            userId = "",
            year = year,
            month = month ?: 0,
            categoryId = categoryId,
            budgetAmountCents = amountCents.toInt(),
            alertThreshold = 0.8f,
            note = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    override suspend fun updateBudget(budget: Budget) {
        budgetRepository.updateBudget(budget)
    }
    
    override suspend fun deleteBudget(budgetId: String) {
        budgetRepository.deleteBudget(budgetId)
    }
    
    override suspend fun getBudgetProgress(budgetId: String): Float {
        val budget = budgetRepository.getBudgets().first().find { it.id == budgetId }
        return if (budget != null) {
            budgetRepository.getBudgetUsagePercentage(budget.year, budget.month, budget.categoryId) ?: 0f
        } else {
            0f
        }
    }
    
    // Savings Goal methods
    override fun getSavingsGoals(): Flow<List<SavingsGoal>> {
        return savingsGoalRepository.getAllSavingsGoals()
    }
    
    override suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate,
        accountId: String?
    ): SavingsGoal {
        val goal = SavingsGoal(
            id = 0, // Will be generated by database
            name = name,
            targetAmount = targetAmountCents.toDouble() / 100,
            currentAmount = 0.0,
            targetDate = java.time.LocalDate.of(targetDate.year, targetDate.monthNumber, targetDate.dayOfMonth),
            description = null,
            color = "#3A7AFE",
            iconName = "savings",
            isActive = true,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
        val goalId = savingsGoalRepository.createSavingsGoal(goal)
        return goal.copy(id = goalId)
    }
    
    override suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalRepository.updateSavingsGoal(savingsGoal)
    }
    
    override suspend fun deleteSavingsGoal(savingsGoalId: String) {
        val goal = savingsGoalRepository.getSavingsGoalById(savingsGoalId.toLong())
        if (goal != null) {
            savingsGoalRepository.deleteSavingsGoal(goal)
        }
    }
    
    override suspend fun addSavingsContribution(
        savingsGoalId: String,
        amountCents: Long,
        note: String?
    ): SavingsContribution {
        val contribution = SavingsContribution(
            id = 0, // Will be generated by database
            goalId = savingsGoalId.toLong(),
            amount = amountCents.toDouble() / 100,
            note = note,
            createdAt = java.time.LocalDateTime.now()
        )
        savingsGoalRepository.addContribution(contribution)
        return contribution
    }
    
    override suspend fun getSavingsGoalProgress(savingsGoalId: String): Float {
        val goal = savingsGoalRepository.getSavingsGoalById(savingsGoalId.toLong())
        return if (goal != null && goal.targetAmount > 0) {
            (goal.currentAmount / goal.targetAmount * 100).toFloat()
        } else {
            0f
        }
    }
    
    // Credit Card methods
    override fun getCreditCardAccounts(): Flow<List<Account>> {
        return accountRepository.getAccounts().map { accounts ->
            accounts.filter { it.type == AccountType.CREDIT_CARD }
        }
    }
    
    override suspend fun getCreditCardBill(
        accountId: String,
        year: Int,
        month: Int
    ): CreditCardBill? {
        // 信用卡账单功能暂未实现，返回null
        return null
    }
    
    override fun getCreditCardBills(accountId: String): Flow<List<CreditCardBill>> {
        // 信用卡账单功能暂未实现，返回空列表
        return flowOf(emptyList())
    }
    
    override suspend fun generateCreditCardBill(accountId: String, year: Int, month: Int) {
        // 获取指定月份的所有交易
        val startDate = kotlinx.datetime.LocalDate(year, month, 1)
        val endDate = if (month == 12) {
            kotlinx.datetime.LocalDate(year + 1, 1, 1)
        } else {
            kotlinx.datetime.LocalDate(year, month + 1, 1)
        }
        val endDateExclusive = endDate.minus(kotlinx.datetime.DatePeriod(days = 1))
        
        val transactions = getTransactionsByAccountAndDateRange(accountId, startDate, endDateExclusive).first()
        val totalAmountCents = transactions.sumOf { it.amountCents.toLong() }
        
        // 创建账单记录（注：实际应该持久化到数据库）
        // 这里只是生成账单的基本逻辑，实际实现需要CreditCardBillRepository
        TODO("需要CreditCardBillRepository来持久化账单数据")
    }
    
    override suspend fun recordCreditCardPayment(
        billId: String,
        amountCents: Long,
        paymentDate: LocalDate,
        fromAccountId: String
    ): CreditCardPayment {
        // 信用卡还款功能暂未实现，返回临时对象
        return CreditCardPayment(
            id = UUID.randomUUID().toString(),
            userId = "", // 临时值
            accountId = fromAccountId,
            paymentAmountCents = amountCents,
            paymentType = com.ccxiaoji.feature.ledger.data.local.entity.PaymentType.FULL,
            paymentDate = paymentDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
            dueAmountCents = 0,
            isOnTime = true,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }
    
    override fun getCreditCardPayments(billId: String): Flow<List<CreditCardPayment>> {
        // 信用卡还款记录功能暂未实现，返回空列表
        return flowOf(emptyList())
    }
    
    override suspend fun updateCreditCardAccount(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        dueDay: Int
    ) {
        // 更新信用卡账户的额度和账单日信息
        val account = accountRepository.getAccountById(accountId)
        if (account != null && account.type == AccountType.CREDIT_CARD) {
            // 更新账户余额为负的信用额度（信用卡余额通常是负数表示已使用额度）
            // 注：实际实现需要扩展Account实体以包含billingDay和dueDay字段
            TODO("需要扩展Account实体以支持信用卡特有字段")
        } else {
            throw IllegalArgumentException("账户不存在或不是信用卡账户")
        }
    }
    
    // Additional statistics methods
    override suspend fun getMonthlyTrend(year: Int, categoryId: String?): Map<Int, Pair<Long, Long>> {
        val result = mutableMapOf<Int, Pair<Long, Long>>()
        
        for (month in 1..12) {
            val (income, expense) = transactionRepository.getMonthlyIncomesAndExpenses(year, month).getOrThrow()
            result[month] = income.toLong() to expense.toLong()
        }
        
        return result
    }
    
    override suspend fun getYearlyOverview(year: Int): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        var totalIncome = 0L
        var totalExpense = 0L
        
        for (month in 1..12) {
            val (income, expense) = transactionRepository.getMonthlyIncomesAndExpenses(year, month).getOrThrow()
            totalIncome += income
            totalExpense += expense
        }
        
        result["totalIncome"] = totalIncome
        result["totalExpense"] = totalExpense
        result["netSavings"] = totalIncome - totalExpense
        result["savingsRate"] = if (totalIncome > 0) {
            ((totalIncome - totalExpense) * 100 / totalIncome)
        } else {
            0L
        }
        
        return result
    }
    
    override suspend fun getExpenseStructure(year: Int, month: Int): Map<String, Float> {
        val startDate = kotlinx.datetime.LocalDate(year, month, 1)
        val endDate = if (month == 12) {
            kotlinx.datetime.LocalDate(year + 1, 1, 1).minus(kotlinx.datetime.DatePeriod(days = 1))
        } else {
            kotlinx.datetime.LocalDate(year, month + 1, 1).minus(kotlinx.datetime.DatePeriod(days = 1))
        }
        
        val categoryStats = transactionRepository.getCategoryStatistics(
            categoryType = "EXPENSE",
            startDate = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            endDate = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        ).getOrThrow()
        val totalExpense = categoryStats.sumOf { it.totalAmount }
        
        return if (totalExpense > 0) {
            categoryStats.associate { stat ->
                stat.categoryName to (stat.totalAmount.toFloat() / totalExpense * 100)
            }
        } else {
            emptyMap()
        }
    }
    
    override suspend fun getAccountBalanceTrend(accountId: String, days: Int): List<Pair<LocalDate, Long>> {
        val endDate = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        val startDate = endDate.minus(kotlinx.datetime.DatePeriod(days = days))
        
        val account = accountRepository.getAccountById(accountId) ?: return emptyList()
        val transactions = transactionRepository.getTransactionsByAccountAndDateRange(accountId, startDate, endDate).first()
        
        val dailyBalances = mutableMapOf<LocalDate, Long>()
        var currentBalance = account.balanceCents.toLong()
        
        // 从最新日期往前推算余额
        val sortedTransactions = transactions.sortedByDescending { it.createdAt }
        var currentDate = endDate
        
        while (currentDate >= startDate) {
            val dayTransactions = sortedTransactions.filter { transaction ->
                val transactionDate = transaction.createdAt
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                transactionDate == currentDate
            }
            
            // 减去当天的交易来计算当天开始时的余额
            dayTransactions.forEach { transaction ->
                val category = categoryRepository.getCategoryById(transaction.categoryId)
                if (category != null && category.type == Category.Type.EXPENSE) {
                    currentBalance += transaction.amountCents
                } else {
                    currentBalance -= transaction.amountCents
                }
            }
            
            dailyBalances[currentDate] = currentBalance
            currentDate = currentDate.minus(kotlinx.datetime.DatePeriod(days = 1))
        }
        
        return dailyBalances.toList().sortedBy { it.first }
    }
    
    // Navigation
    override fun navigateToAddTransaction() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToTransactionDetail(transactionId: String) {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToAccountManagement() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToCategoryManagement() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToRecurringTransactions() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToBudgetManagement() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToSavingsGoals() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToStatistics() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToCreditCardManagement() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    // Screen Providers
    @Composable
    override fun getLedgerScreen(navController: NavHostController, accountId: String?) {
        if (accountId != null) {
            LedgerScreen(navController = navController, accountId = accountId)
        } else {
            LedgerScreen(navController = navController)
        }
    }
    
    @Composable
    override fun getTransactionDetailScreen(transactionId: String, navController: NavHostController) {
        TransactionDetailScreen(
            transactionId = transactionId,
            navController = navController,
            viewModel = hiltViewModel()
        )
    }
    
    @Composable
    override fun getAccountScreen(navController: NavHostController) {
        AccountScreen(navController = navController)
    }
    
    @Composable
    override fun getCategoryManagementScreen(navController: NavHostController) {
        CategoryManagementScreen(navController = navController)
    }
    
    @Composable
    override fun getBudgetScreen(onNavigateBack: () -> Unit) {
        BudgetScreen(onNavigateBack = onNavigateBack)
    }
    
    @Composable
    override fun getStatisticsScreen(onNavigateBack: () -> Unit) {
        StatisticsScreen(onNavigateBack = onNavigateBack)
    }
    
    @Composable
    override fun getRecurringTransactionScreen(onNavigateBack: () -> Unit) {
        RecurringTransactionScreen(onNavigateBack = onNavigateBack)
    }
    
    @Composable
    override fun getSavingsGoalScreen(onNavigateBack: () -> Unit, onNavigateToDetail: (Long) -> Unit) {
        SavingsGoalScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }
    
    @Composable
    override fun getSavingsGoalDetailScreen(goalId: Long, onNavigateBack: () -> Unit) {
        SavingsGoalDetailScreen(
            goalId = goalId,
            onNavigateBack = onNavigateBack
        )
    }
    
    @Composable
    override fun getCreditCardScreen(
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToAccount: (String) -> Unit
    ) {
        CreditCardScreen(
            navController = navController,
            onNavigateBack = onNavigateBack,
            onNavigateToAccount = onNavigateToAccount
        )
    }
    
    @Composable
    override fun getCreditCardBillsScreen(accountId: String, navController: NavHostController) {
        CreditCardBillsScreen(
            accountId = accountId,
            navController = navController
        )
    }
}