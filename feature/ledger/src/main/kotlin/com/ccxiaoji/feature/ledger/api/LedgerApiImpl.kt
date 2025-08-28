package com.ccxiaoji.feature.ledger.api

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.domain.repository.CreditCardBillRepository
import com.ccxiaoji.feature.ledger.data.repository.RecurringTransactionRepository
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.GetLedgerStatsUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerFilteredStatisticsUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerFilter
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerComprehensiveStats
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.presentation.screen.account.AccountScreen
import com.ccxiaoji.feature.ledger.presentation.screen.budget.BudgetScreen
import com.ccxiaoji.feature.ledger.presentation.screen.category.CategoryManagementScreen
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardBillsScreen
// import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardScreen // 已删除
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.CreditCardSettingsScreen
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.LedgerScreen
import com.ccxiaoji.feature.ledger.presentation.screen.transaction.TransactionDetailScreen
import com.ccxiaoji.feature.ledger.presentation.screen.recurring.RecurringTransactionScreen
import com.ccxiaoji.feature.ledger.presentation.screen.savings.SavingsGoalDetailScreen
import com.ccxiaoji.feature.ledger.presentation.screen.savings.SavingsGoalScreen
import com.ccxiaoji.feature.ledger.presentation.screen.statistics.StatisticsScreen
import com.ccxiaoji.feature.ledger.presentation.screen.AssetOverviewScreen
import com.ccxiaoji.feature.ledger.presentation.screen.settings.LedgerSettingsScreen
import com.ccxiaoji.feature.ledger.presentation.screen.debug.AutoLedgerDebugScreen
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
    private val creditCardBillRepository: CreditCardBillRepository,
    private val recurringTransactionRepository: RecurringTransactionRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val ledgerExporter: com.ccxiaoji.feature.ledger.domain.export.LedgerExporter,
    private val manageLedgerUseCase: ManageLedgerUseCase,
    private val getLedgerStatsUseCase: GetLedgerStatsUseCase,
    private val ledgerFilteredStatisticsUseCase: LedgerFilteredStatisticsUseCase,
    private val userApi: UserApi
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
        accountId: String,
        ledgerId: String?,
        transactionDate: kotlinx.datetime.Instant?,
        location: LocationData?
    ): Transaction {
        // 如果没有提供记账簿ID，获取默认记账簿ID
        val actualLedgerId = ledgerId ?: run {
            val defaultLedger = manageLedgerUseCase.getDefaultLedger(userApi.getCurrentUserId()).getOrThrow()
            defaultLedger.id
        }
        
        val result = transactionRepository.addTransaction(
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            accountId = accountId,
            ledgerId = actualLedgerId,
            transactionDate = transactionDate,
            location = location
        )
        val transactionId = when (result) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
        // 获取完整的Transaction对象
        return transactionRepository.getTransactionById(transactionId.toString()) 
            ?: throw IllegalStateException("创建的交易未找到")
    }
    
    override suspend fun updateTransaction(transaction: Transaction) {
        transactionRepository.updateTransaction(transaction)
    }
    
    override suspend fun deleteTransaction(transactionId: String) {
        transactionRepository.deleteTransaction(transactionId)
    }
    
    // Batch operations
    override suspend fun insertTransactionsBatch(
        transactions: List<TransactionBatchItem>
    ): BatchInsertResult {
        val results = mutableListOf<String>()
        val errors = mutableListOf<BatchInsertError>()
        
        // 使用事务批量插入
        transactions.forEachIndexed { index, item ->
            try {
                // 获取默认记账簿ID
                val defaultLedger = manageLedgerUseCase.getDefaultLedger(userApi.getCurrentUserId()).getOrThrow()
                
                val result = transactionRepository.addTransaction(
                    amountCents = item.amountCents,
                    categoryId = item.categoryId,
                    note = item.note,
                    accountId = item.accountId,
                    ledgerId = defaultLedger.id,
                    transactionDate = null,
                    location = null
                )
                when (result) {
                    is BaseResult.Success -> results.add(result.data)
                    is BaseResult.Error -> errors.add(BatchInsertError(index, result.exception.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                errors.add(BatchInsertError(index, e.message ?: "Unknown error"))
            }
        }
        
        return BatchInsertResult(
            successCount = results.size,
            failedCount = errors.size,
            insertedIds = results,
            errors = errors
        )
    }
    
    override suspend fun getTransactionCount(): Int {
        return transactionRepository.getTransactions().first().size
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
            type = AccountTypeMapping.safeValueOf(type),
            initialBalanceCents = balanceCents,
            creditLimitCents = null,
            billingDay = null,
            paymentDueDay = null,
            gracePeriodDays = null
        )
        // 获取完整的Account对象
        return accountRepository.getAccountById(accountId.toString())
            ?: throw IllegalStateException("创建的账户未找到")
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
        budgetRepository.createBudget(
            year = year,
            month = month ?: 0,
            categoryId = categoryId,
            amountCents = amountCents.toInt()
        )
        
        // 获取刚创建的预算
        val budgets = budgetRepository.getBudgets().first()
        val createdBudget = budgets
            .filter { it.year == year && it.month == (month ?: 0) && it.categoryId == categoryId }
            .maxByOrNull { it.createdAt }
            
        return createdBudget ?: throw IllegalStateException("创建的预算未找到")
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
            userId = "default_user", // TODO: Get from current user context
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
        
        // 创建账单记录并持久化到数据库
        val result = creditCardBillRepository.generateBill(
            accountId = accountId,
            periodStart = startDate,
            periodEnd = endDateExclusive
        )
        
        when (result) {
            is com.ccxiaoji.common.base.BaseResult.Success -> {
                // 账单生成成功
            }
            is com.ccxiaoji.common.base.BaseResult.Error -> {
                // 如果生成失败，抛出异常
                throw result.exception
            }
        }
    }
    
    override suspend fun recordCreditCardPayment(
        billId: String,
        amountCents: Long,
        paymentDate: LocalDate,
        fromAccountId: String
    ): CreditCardPayment {
        // 记录信用卡还款到账单
        val result = creditCardBillRepository.recordPayment(billId, amountCents.toInt())
        when (result) {
            is BaseResult.Error -> throw result.exception
            is BaseResult.Success -> { /* 记录成功 */ }
        }
        
        // 获取账单信息以确定账户ID
        val billResult = creditCardBillRepository.getBillById(billId)
        val bill = when (billResult) {
            is BaseResult.Success -> billResult.data
            is BaseResult.Error -> throw billResult.exception
        }
        
        // 创建还款记录
        return CreditCardPayment(
            id = UUID.randomUUID().toString(),
            userId = bill.userId,
            accountId = fromAccountId,
            paymentAmountCents = amountCents,
            paymentType = com.ccxiaoji.feature.ledger.data.local.entity.PaymentType.FULL,
            paymentDate = paymentDate.atStartOfDayIn(TimeZone.currentSystemDefault()),
            dueAmountCents = bill.totalAmountCents,
            isOnTime = paymentDate <= bill.paymentDueDate.toLocalDateTime(TimeZone.currentSystemDefault()).date,
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
            // 创建更新后的账户对象
            val updatedAccount = account.copy(
                creditLimitCents = creditLimitCents,
                billingDay = billingDay,
                paymentDueDay = dueDay,
                updatedAt = Clock.System.now()
            )
            
            // 更新账户
            accountRepository.updateAccount(updatedAccount)
            // 更新成功
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
    
    override fun navigateToLedgerManagement() {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    override fun navigateToLedgerDetail(ledgerId: String) {
        throw NotImplementedError("导航功能应在app模块中实现，不应调用此方法")
    }
    
    // Ledger methods implementation
    override fun getLedgers(userId: String): Flow<List<Ledger>> {
        return manageLedgerUseCase.getUserLedgers(userId)
    }
    
    override fun getLedgersWithStats(userId: String): Flow<List<LedgerWithStats>> {
        return getLedgerStatsUseCase.getLedgerWithStats(userId)
    }
    
    override suspend fun getLedgerById(ledgerId: String): Ledger? {
        return when (val result = manageLedgerUseCase.getLedgerById(ledgerId)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> null
        }
    }
    
    override suspend fun createLedger(
        userId: String,
        name: String,
        description: String?,
        icon: String,
        color: String
    ): Ledger {
        return when (val result = manageLedgerUseCase.createLedger(userId, name, description, icon, color)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun updateLedger(ledger: Ledger) {
        when (val result = manageLedgerUseCase.updateLedger(ledger)) {
            is BaseResult.Success -> Unit
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun deleteLedger(ledgerId: String) {
        when (val result = manageLedgerUseCase.deleteLedger(ledgerId, userApi.getCurrentUserId())) {
            is BaseResult.Success -> Unit
            is BaseResult.Error -> throw result.exception
        }
    }
    
    // Ledger statistics methods implementation
    override suspend fun getLedgerStats(userId: String): List<LedgerWithStats> {
        return getLedgerStatsUseCase.getLedgerWithStats(userId).first()
    }
    
    override suspend fun getLedgerDetailStats(
        ledgerId: String,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): com.ccxiaoji.feature.ledger.domain.usecase.LedgerDetailStats {
        return when (val result = getLedgerStatsUseCase.getLedgerDetailStats(ledgerId, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun getLedgerMonthlyStats(
        ledgerId: String,
        year: Int,
        month: Int
    ): com.ccxiaoji.feature.ledger.domain.usecase.MonthlyLedgerStats {
        return when (val result = getLedgerStatsUseCase.getLedgerMonthlyStats(ledgerId, year, month)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun compareLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate?,
        endDate: LocalDate?
    ): List<com.ccxiaoji.feature.ledger.domain.usecase.LedgerComparisonStats> {
        return when (val result = getLedgerStatsUseCase.compareLedgers(ledgerIds, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    // Ledger-filtered transaction methods implementation
    override fun getTransactionsByLedger(ledgerId: String): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByLedger(ledgerId)
    }
    
    override fun getTransactionsByLedgerAndDateRange(
        ledgerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByLedgerAndDateRange(ledgerId, startDate, endDate)
    }
    
    override fun getTransactionsByLedgers(ledgerIds: List<String>): Flow<List<Transaction>> {
        return transactionRepository.getTransactionsByLedgers(ledgerIds)
    }
    
    override suspend fun getMonthlyIncomesAndExpensesByLedger(
        ledgerId: String,
        year: Int,
        month: Int
    ): Pair<Int, Int> {
        return when (val result = transactionRepository.getMonthlyIncomesAndExpensesByLedger(ledgerId, year, month)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    // Ledger-filtered statistics methods implementation
    override suspend fun getCategoryStatisticsByLedger(
        ledgerFilter: LedgerFilter,
        categoryType: String?,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategoryStatistic> {
        return when (val result = ledgerFilteredStatisticsUseCase.getCategoryStatistics(ledgerFilter, categoryType, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun getDailyTotalsByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Pair<Int, Int>> {
        return when (val result = ledgerFilteredStatisticsUseCase.getDailyTotals(ledgerFilter, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun getTopTransactionsByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate,
        type: String,
        limit: Int
    ): List<Transaction> {
        return when (val result = ledgerFilteredStatisticsUseCase.getTopTransactions(ledgerFilter, startDate, endDate, type, limit)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun calculateSavingsRateByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float {
        return when (val result = ledgerFilteredStatisticsUseCase.calculateSavingsRate(ledgerFilter, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override suspend fun getLedgerComprehensiveStats(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): LedgerComprehensiveStats {
        return when (val result = ledgerFilteredStatisticsUseCase.getComprehensiveStats(ledgerFilter, startDate, endDate)) {
            is BaseResult.Success -> result.data
            is BaseResult.Error -> throw result.exception
        }
    }
    
    override fun getTransactionsPaginatedByLedger(
        ledgerFilter: LedgerFilter,
        offset: Int,
        limit: Int,
        accountId: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<com.ccxiaoji.common.base.BaseResult<Pair<List<Transaction>, Int>>> {
        return ledgerFilteredStatisticsUseCase.getTransactionsPaginated(ledgerFilter, offset, limit, accountId, startDate, endDate)
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
    override fun getBudgetScreen(
        onNavigateBack: () -> Unit,
        onNavigateToAddEditBudget: (categoryId: String?) -> Unit
    ) {
        BudgetScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddEditBudget = onNavigateToAddEditBudget
        )
    }
    
    @Composable
    override fun getStatisticsScreen(onNavigateBack: () -> Unit) {
        StatisticsScreen(onNavigateBack = onNavigateBack)
    }
    
    @Composable
    override fun getAssetOverviewScreen(onNavigateBack: () -> Unit) {
        AssetOverviewScreen(onNavigateBack = onNavigateBack)
    }
    
    @Composable
    override fun getUnifiedAccountAssetScreen(onNavigateBack: () -> Unit, navController: NavHostController?) {
        com.ccxiaoji.feature.ledger.presentation.screen.account.UnifiedAccountAssetScreen(
            onNavigateBack = onNavigateBack,
            navController = navController
        )
    }
    
    @Composable
    override fun getLedgerSettingsScreen(
        onNavigateBack: () -> Unit,
        onNavigateToCategory: () -> Unit,
        onNavigateToAccount: () -> Unit,
        onNavigateToBudget: () -> Unit,
        onNavigateToRecurring: () -> Unit,
        onNavigateToCurrencySelection: () -> Unit,
        onNavigateToAccountSelection: () -> Unit,
        onNavigateToReminderSettings: () -> Unit,
        onNavigateToHomeDisplaySettings: () -> Unit,
        onNavigateToUIStyleSettings: () -> Unit,
        onNavigateToLedgerBookManagement: () -> Unit,
        onNavigateToPermissionGuide: () -> Unit,
        onNavigateToAutoLedgerDebug: () -> Unit,
        onNavigateToAutoLedgerSettings: () -> Unit,
        navController: NavHostController?
    ) {
        LedgerSettingsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToLedgerBookManagement = onNavigateToLedgerBookManagement,
            onNavigateToCategoryManagement = onNavigateToCategory,
            onNavigateToAccountManagement = onNavigateToAccount,
            onNavigateToBudgetManagement = onNavigateToBudget,
            onNavigateToRecurringTransactions = onNavigateToRecurring,
            onNavigateToCurrencySelection = onNavigateToCurrencySelection,
            onNavigateToAccountSelection = onNavigateToAccountSelection,
            onNavigateToReminderSettings = onNavigateToReminderSettings,
            onNavigateToHomeDisplaySettings = onNavigateToHomeDisplaySettings,
            onNavigateToUIStyleSettings = onNavigateToUIStyleSettings,
            onNavigateToPermissionGuide = onNavigateToPermissionGuide,
            onNavigateToAutoLedgerDebug = onNavigateToAutoLedgerDebug,
            onNavigateToAutoLedgerSettings = onNavigateToAutoLedgerSettings,
            navController = navController
        )
    }
    
    @Composable
    override fun getRecurringTransactionScreen(onNavigateBack: () -> Unit, onNavigateToAddEdit: (String?) -> Unit) {
        RecurringTransactionScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddEdit = onNavigateToAddEdit
        )
    }
    
    @Composable
    override fun getSavingsGoalScreen(onNavigateBack: () -> Unit, onNavigateToDetail: (Long) -> Unit, onNavigateToAddGoal: () -> Unit) {
        SavingsGoalScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAddGoal = onNavigateToAddGoal
        )
    }
    
    @Composable
    override fun getSavingsGoalDetailScreen(
        goalId: Long,
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToEditGoal: (Long) -> Unit,
        onNavigateToContribution: (Long) -> Unit
    ) {
        SavingsGoalDetailScreen(
            goalId = goalId,
            navController = navController,
            onNavigateBack = onNavigateBack,
            onNavigateToEditGoal = onNavigateToEditGoal,
            onNavigateToContribution = onNavigateToContribution
        )
    }
    
    @Composable
    override fun getCreditCardScreen(
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToAccount: (String) -> Unit
    ) {
        // TODO: 恢复CreditCardScreen实现或提供替代方案
        androidx.compose.foundation.layout.Column {
            androidx.compose.material3.Text("信用卡功能暂时不可用")
        }
    }
    
    @Composable
    override fun getCreditCardBillsScreen(accountId: String, navController: NavHostController) {
        CreditCardBillsScreen(
            accountId = accountId,
            navController = navController
        )
    }
    
    // Export methods implementation
    override suspend fun getExportStatistics(): LedgerExportStats {
        // 获取各种数据的统计信息
        // 由于TransactionRepository没有getTransactionCount方法，我们使用getTransactions并计数
        val transactionCount = transactionRepository.getTransactions().first().size
        val accountCount = accountRepository.getAccounts().first().size
        val categoryCount = categoryRepository.getCategories().first().size
        val budgetCount = budgetRepository.getBudgets().first().size
        // RecurringTransactionRepository的方法名是getAllRecurringTransactions
        val recurringCount = recurringTransactionRepository.getAllRecurringTransactions().first().size
        // SavingsGoalRepository的方法名是getAllSavingsGoals
        val savingsCount = savingsGoalRepository.getAllSavingsGoals().first().size
        // 获取记账簿数量
        val ledgerCount = manageLedgerUseCase.getUserLedgers(userApi.getCurrentUserId()).first().size
        
        // 获取最后修改时间（这里简单使用当前时间，实际应该查询数据库）
        val lastModified = System.currentTimeMillis()
        
        return LedgerExportStats(
            transactionCount = transactionCount,
            accountCount = accountCount,
            categoryCount = categoryCount,
            budgetCount = budgetCount,
            recurringCount = recurringCount,
            savingsCount = savingsCount,
            ledgerCount = ledgerCount,
            lastModified = lastModified
        )
    }
    
    override suspend fun exportAllData(config: com.ccxiaoji.feature.ledger.domain.export.ExportConfig): java.io.File {
        // 使用注入的LedgerExporter执行导出
        return ledgerExporter.exportAll(config)
    }
    
    @Composable
    override fun getCreditCardSettingsScreen(accountId: String, navController: NavHostController) {
        CreditCardSettingsScreen(
            accountId = accountId,
            navController = navController
        )
    }
    
    @Composable
    override fun getLedgerManagementScreen(
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToLedgerDetail: (String) -> Unit,
        onNavigateToAddLedger: () -> Unit
    ) {
        com.ccxiaoji.feature.ledger.presentation.screen.ledgerbook.LedgerBookManagementScreen(
            onNavigateBack = onNavigateBack
        )
    }
    
    @Composable
    override fun getLedgerDetailScreen(
        ledgerId: String,
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToEditLedger: (String) -> Unit
    ) {
        // TODO: 实现记账簿详情界面
        throw NotImplementedError("记账簿详情界面待实现")
    }
    
    @Composable
    override fun getLedgerSelectorDialog(
        availableLedgers: List<Ledger>,
        selectedLedgerId: String?,
        onLedgerSelected: (Ledger) -> Unit,
        onDismiss: () -> Unit
    ) {
        // TODO: 实现记账簿选择器对话框
        throw NotImplementedError("记账簿选择器待实现")
    }
    
    @Composable
    override fun getAutoLedgerDebugScreen(
        navController: NavHostController
    ) {
        AutoLedgerDebugScreen(navController = navController)
    }
}
