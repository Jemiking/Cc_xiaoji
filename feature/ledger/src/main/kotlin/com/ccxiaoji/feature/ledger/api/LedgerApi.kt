package com.ccxiaoji.feature.ledger.api

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.ccxiaoji.feature.ledger.domain.model.*
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
        accountId: String? = null,
        createdAt: Long = System.currentTimeMillis()
    ): Transaction
    
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transactionId: String)
    
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
    fun getBudgetScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getStatisticsScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getAssetOverviewScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getLedgerSettingsScreen(
        onNavigateBack: () -> Unit,
        onNavigateToCategory: () -> Unit,
        onNavigateToAccount: () -> Unit,
        onNavigateToBudget: () -> Unit,
        onNavigateToDataExport: () -> Unit,
        onNavigateToRecurring: () -> Unit
    )
    
    @Composable
    fun getRecurringTransactionScreen(onNavigateBack: () -> Unit)
    
    @Composable
    fun getSavingsGoalScreen(onNavigateBack: () -> Unit, onNavigateToDetail: (Long) -> Unit)
    
    @Composable
    fun getSavingsGoalDetailScreen(goalId: Long, onNavigateBack: () -> Unit)
    
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
}