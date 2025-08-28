package com.ccxiaoji.feature.ledger.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerFilter
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerComprehensiveStats
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Public API for ledger feature module
 */
interface LedgerApi {
    // Transaction methods
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    fun getTransactionsByAccountAndDateRange(accountId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    
    suspend fun getMonthlyTotal(year: Int, month: Int): Int
    suspend fun getMonthlyIncomesAndExpenses(year: Int, month: Int): Pair<Int, Int>
    suspend fun getTodayExpense(): Double
    suspend fun getTotalBalance(): Double
    
    suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String,
        ledgerId: String? = null,
        transactionDate: kotlinx.datetime.Instant? = null,
        location: LocationData? = null
    ): Transaction
    
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    
    // Batch operations
    suspend fun insertTransactionsBatch(
        transactions: List<TransactionBatchItem>
    ): BatchInsertResult
    
    suspend fun getTransactionCount(): Int
    
    // Account methods
    fun getAccounts(): Flow<List<Account>>
    suspend fun getAccountById(accountId: String): Account?
    suspend fun createAccount(
        name: String,
        type: String,
        balanceCents: Long = 0,
        currency: String = "CNY",
        isDefault: Boolean = false
    ): Account
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(accountId: String)
    suspend fun updateAccountBalance(accountId: String, newBalanceCents: Long)
    suspend fun transferBetweenAccounts(
        fromAccountId: String,
        toAccountId: String,
        amountCents: Int,
        note: String? = null
    )
    
    // Category methods
    fun getCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: String): Flow<List<Category>>
    suspend fun getCategoryById(categoryId: String): Category?
    suspend fun createCategory(
        name: String,
        type: String,
        icon: String? = null,
        color: String = "#3A7AFE",
        parentId: String? = null
    ): Category
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)
    
    // Ledger methods
    fun getLedgers(userId: String): Flow<List<Ledger>>
    fun getLedgersWithStats(userId: String): Flow<List<LedgerWithStats>>
    suspend fun getLedgerById(ledgerId: String): Ledger?
    suspend fun createLedger(
        userId: String,
        name: String,
        description: String? = null,
        icon: String = "📖",
        color: String = "#3A7AFE"
    ): Ledger
    suspend fun updateLedger(ledger: Ledger)
    suspend fun deleteLedger(ledgerId: String)
    
    // Ledger statistics methods
    suspend fun getLedgerStats(userId: String): List<LedgerWithStats>
    suspend fun getLedgerDetailStats(
        ledgerId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): com.ccxiaoji.feature.ledger.domain.usecase.LedgerDetailStats
    suspend fun getLedgerMonthlyStats(
        ledgerId: String,
        year: Int,
        month: Int
    ): com.ccxiaoji.feature.ledger.domain.usecase.MonthlyLedgerStats
    suspend fun compareLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): List<com.ccxiaoji.feature.ledger.domain.usecase.LedgerComparisonStats>
    
    // Ledger-filtered transaction methods
    fun getTransactionsByLedger(ledgerId: String): Flow<List<Transaction>>
    fun getTransactionsByLedgerAndDateRange(
        ledgerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>>
    fun getTransactionsByLedgers(ledgerIds: List<String>): Flow<List<Transaction>>
    suspend fun getMonthlyIncomesAndExpensesByLedger(
        ledgerId: String,
        year: Int,
        month: Int
    ): Pair<Int, Int>
    
    // Ledger-filtered statistics methods
    suspend fun getCategoryStatisticsByLedger(
        ledgerFilter: LedgerFilter,
        categoryType: String? = null,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategoryStatistic>
    suspend fun getDailyTotalsByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): Map<LocalDate, Pair<Int, Int>>
    suspend fun getTopTransactionsByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate,
        type: String,
        limit: Int = 10
    ): List<Transaction>
    suspend fun calculateSavingsRateByLedger(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): Float
    suspend fun getLedgerComprehensiveStats(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): LedgerComprehensiveStats
    fun getTransactionsPaginatedByLedger(
        ledgerFilter: LedgerFilter,
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<com.ccxiaoji.common.base.BaseResult<Pair<List<Transaction>, Int>>>
    
    // Statistics methods
    suspend fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Pair<Int, Int>>
    suspend fun getCategoryStatistics(startDate: LocalDate, endDate: LocalDate, type: String): List<CategoryStatistic>
    suspend fun getTopTransactions(startDate: LocalDate, endDate: LocalDate, type: String, limit: Int = 10): List<Transaction>
    suspend fun calculateSavingsRate(startDate: LocalDate, endDate: LocalDate): Float
    
    // Recurring transaction methods
    fun getRecurringTransactions(): Flow<List<RecurringTransaction>>
    suspend fun createRecurringTransaction(
        amountCents: Int,
        categoryId: String,
        accountId: String,
        note: String?,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate? = null
    ): RecurringTransaction
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)
    suspend fun deleteRecurringTransaction(recurringTransactionId: String)
    suspend fun pauseRecurringTransaction(recurringTransactionId: String)
    suspend fun resumeRecurringTransaction(recurringTransactionId: String)
    
    // Budget methods
    fun getBudgets(): Flow<List<Budget>>
    fun getBudgetsByMonth(year: Int, month: Int): Flow<List<Budget>>
    suspend fun createBudget(
        categoryId: String,
        amountCents: Long,
        period: String,
        year: Int,
        month: Int? = null
    ): Budget
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budgetId: String)
    suspend fun getBudgetProgress(budgetId: String): Float
    
    // Savings Goal methods
    fun getSavingsGoals(): Flow<List<SavingsGoal>>
    suspend fun createSavingsGoal(
        name: String,
        targetAmountCents: Long,
        targetDate: LocalDate,
        accountId: String? = null
    ): SavingsGoal
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)
    suspend fun deleteSavingsGoal(savingsGoalId: String)
    suspend fun addSavingsContribution(
        savingsGoalId: String,
        amountCents: Long,
        note: String? = null
    ): SavingsContribution
    suspend fun getSavingsGoalProgress(savingsGoalId: String): Float
    
    // Credit Card methods
    fun getCreditCardAccounts(): Flow<List<Account>>
    suspend fun getCreditCardBill(accountId: String, year: Int, month: Int): CreditCardBill?
    fun getCreditCardBills(accountId: String): Flow<List<CreditCardBill>>
    suspend fun generateCreditCardBill(accountId: String, year: Int, month: Int)
    suspend fun recordCreditCardPayment(
        billId: String,
        amountCents: Long,
        paymentDate: LocalDate,
        fromAccountId: String
    ): CreditCardPayment
    fun getCreditCardPayments(billId: String): Flow<List<CreditCardPayment>>
    suspend fun updateCreditCardAccount(
        accountId: String,
        creditLimitCents: Long,
        billingDay: Int,
        dueDay: Int
    )
    
    // Additional statistics methods
    suspend fun getMonthlyTrend(year: Int, categoryId: String? = null): Map<Int, Pair<Long, Long>>
    suspend fun getYearlyOverview(year: Int): Map<String, Long>
    suspend fun getExpenseStructure(year: Int, month: Int): Map<String, Float>
    suspend fun getAccountBalanceTrend(accountId: String, days: Int = 30): List<Pair<LocalDate, Long>>
    
    // Export methods
    suspend fun getExportStatistics(): LedgerExportStats
    suspend fun exportAllData(config: com.ccxiaoji.feature.ledger.domain.export.ExportConfig): java.io.File
    
    // Navigation
    fun navigateToAddTransaction()
    fun navigateToTransactionDetail(transactionId: String)
    fun navigateToAccountManagement()
    fun navigateToCategoryManagement()
    fun navigateToRecurringTransactions()
    fun navigateToBudgetManagement()
    fun navigateToSavingsGoals()
    fun navigateToStatistics()
    fun navigateToCreditCardManagement()
    fun navigateToLedgerManagement()
    fun navigateToLedgerDetail(ledgerId: String)
    
    // Screen Providers
    @Composable
    fun getLedgerScreen(navController: NavHostController, accountId: String?)
    
    @Composable
    fun getTransactionDetailScreen(transactionId: String, navController: NavHostController)
    
    @Composable
    fun getAccountScreen(navController: NavHostController)
    
    @Composable
    fun getCategoryManagementScreen(navController: NavHostController)
    
    @Composable
    fun getBudgetScreen(
        onNavigateBack: () -> Unit,
        onNavigateToAddEditBudget: (categoryId: String?) -> Unit
    )
    
    @Composable
    fun getStatisticsScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getAssetOverviewScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getUnifiedAccountAssetScreen(onNavigateBack: () -> Unit, navController: NavHostController?)
    
    @Composable
    fun getLedgerSettingsScreen(
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
    )
    
    @Composable
    fun getRecurringTransactionScreen(onNavigateBack: () -> Unit, onNavigateToAddEdit: (String?) -> Unit)
    
    @Composable
    fun getSavingsGoalScreen(onNavigateBack: () -> Unit, onNavigateToDetail: (Long) -> Unit, onNavigateToAddGoal: () -> Unit)
    
    @Composable
    fun getSavingsGoalDetailScreen(
        goalId: Long,
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToEditGoal: (Long) -> Unit,
        onNavigateToContribution: (Long) -> Unit
    )
    
    @Composable
    fun getCreditCardScreen(
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToAccount: (String) -> Unit
    )
    
    @Composable
    fun getCreditCardBillsScreen(accountId: String, navController: NavHostController)
    
    @Composable
    fun getCreditCardSettingsScreen(accountId: String, navController: NavHostController)
    
    @Composable
    fun getLedgerManagementScreen(
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToLedgerDetail: (String) -> Unit,
        onNavigateToAddLedger: () -> Unit
    )
    
    @Composable
    fun getLedgerDetailScreen(
        ledgerId: String,
        navController: NavHostController,
        onNavigateBack: () -> Unit,
        onNavigateToEditLedger: (String) -> Unit
    )
    
    @Composable
    fun getLedgerSelectorDialog(
        availableLedgers: List<Ledger>,
        selectedLedgerId: String?,
        onLedgerSelected: (Ledger) -> Unit,
        onDismiss: () -> Unit
    )
    
    @Composable
    fun getAutoLedgerDebugScreen(
        navController: NavHostController
    )
}

/**
 * 批量插入交易数据项
 */
data class TransactionBatchItem(
    val amountCents: Int,
    val categoryId: String,
    val accountId: String,
    val note: String,
    val createdAt: Long
)

/**
 * 批量插入结果
 */
data class BatchInsertResult(
    val successCount: Int,
    val failedCount: Int,
    val insertedIds: List<String>, // 改为String类型以支持新的transaction ID格式
    val errors: List<BatchInsertError>
)

/**
 * 批量插入错误信息
 */
data class BatchInsertError(
    val index: Int,
    val message: String
)

/**
 * 记账模块导出统计信息
 */
data class LedgerExportStats(
    val transactionCount: Int,
    val accountCount: Int,
    val categoryCount: Int,
    val budgetCount: Int,
    val recurringCount: Int,
    val savingsCount: Int,
    val ledgerCount: Int,
    val lastModified: Long
)
