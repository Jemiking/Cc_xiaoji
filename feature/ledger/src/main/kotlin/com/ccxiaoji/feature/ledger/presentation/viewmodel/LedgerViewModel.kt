package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.AccountRepository
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.data.repository.BudgetRepository
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.datetime.toJavaLocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import java.time.YearMonth

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()
    
    private val _groupedTransactions = MutableStateFlow<List<TransactionGroup>>(emptyList())
    val groupedTransactions: StateFlow<List<TransactionGroup>> = _groupedTransactions.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()
    
    init {
        loadTransactions()
        loadMonthlySummary()
        loadAccounts()
        loadCategories()
    }
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            val accountId = _uiState.value.activeFilter.accountId
            val transactionsFlow = if (accountId != null) {
                transactionRepository.getTransactionsByAccount(accountId)
            } else {
                transactionRepository.getTransactions()
            }
            
            transactionsFlow.collect { allTransactions ->
                // Filter transactions by selected month
                val filteredByMonth = allTransactions.filter { transaction ->
                    val transactionDate = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val transactionMonth = YearMonth.of(transactionDate.year, transactionDate.monthNumber)
                    transactionMonth == _selectedMonth.value
                }
                _uiState.update { it.copy(transactions = filteredByMonth) }
                updateGroupedTransactions(filteredByMonth)
            }
        }
    }
    
    private fun loadMonthlySummary() {
        viewModelScope.launch {
            val selectedMonth = _selectedMonth.value
            
            // Use new method that queries by category type instead of old enum
            val (income, expense) = transactionRepository.getMonthlyIncomesAndExpenses(
                selectedMonth.year, 
                selectedMonth.monthValue
            )
            
            _uiState.update { 
                it.copy(
                    monthlyIncome = income / 100.0,
                    monthlyExpense = expense / 100.0
                )
            }
        }
    }
    
    private fun loadAccounts() {
        viewModelScope.launch {
            accountRepository.getAccounts().collect { accounts ->
                _uiState.update { 
                    it.copy(
                        accounts = accounts,
                        selectedAccount = accounts.find { acc -> acc.isDefault } ?: accounts.firstOrNull()
                    )
                }
            }
        }
    }
    
    fun addTransaction(amountCents: Int, categoryId: String, note: String?, accountId: String? = null) {
        viewModelScope.launch {
            val finalAccountId = accountId ?: _uiState.value.selectedAccount?.id ?: return@launch
            
            // 添加交易
            transactionRepository.addTransaction(
                amountCents = amountCents,
                categoryId = categoryId,
                note = note,
                accountId = finalAccountId
            )
            
            // 检查预算
            checkBudgetAfterTransaction(categoryId)
            
            loadMonthlySummary() // Refresh summary
        }
    }
    
    private suspend fun checkBudgetAfterTransaction(categoryId: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        
        // 检查分类预算
        val categoryBudgetAlert = budgetRepository.checkBudgetAlert(now.year, now.monthNumber, categoryId)
        val categoryBudgetExceeded = budgetRepository.checkBudgetExceeded(now.year, now.monthNumber, categoryId)
        
        // 检查总预算
        val totalBudgetAlert = budgetRepository.checkBudgetAlert(now.year, now.monthNumber, null)
        val totalBudgetExceeded = budgetRepository.checkBudgetExceeded(now.year, now.monthNumber, null)
        
        // 更新UI状态以显示预算提醒
        if (categoryBudgetExceeded || totalBudgetExceeded) {
            _uiState.update { 
                it.copy(
                    budgetAlert = BudgetAlertInfo(
                        message = when {
                            categoryBudgetExceeded && totalBudgetExceeded -> "分类预算和总预算都已超支！"
                            categoryBudgetExceeded -> "该分类预算已超支！"
                            else -> "总预算已超支！"
                        },
                        isExceeded = true
                    )
                )
            }
        } else if (categoryBudgetAlert || totalBudgetAlert) {
            _uiState.update { 
                it.copy(
                    budgetAlert = BudgetAlertInfo(
                        message = when {
                            categoryBudgetAlert && totalBudgetAlert -> "分类预算和总预算即将用完！"
                            categoryBudgetAlert -> "该分类预算即将用完！"
                            else -> "总预算即将用完！"
                        },
                        isExceeded = false
                    )
                )
            }
        }
    }
    
    fun dismissBudgetAlert() {
        _uiState.update { it.copy(budgetAlert = null) }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }
    
    fun setSelectedAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transactionId)
            loadMonthlySummary() // Refresh summary
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
            loadMonthlySummary() // Refresh summary
        }
    }
    
    fun setEditingTransaction(transaction: Transaction?) {
        _uiState.update { it.copy(editingTransaction = transaction) }
    }
    
    fun toggleSelectionMode() {
        _uiState.update { 
            it.copy(
                isSelectionMode = !it.isSelectionMode,
                selectedTransactionIds = if (it.isSelectionMode) emptySet() else it.selectedTransactionIds
            )
        }
    }
    
    fun toggleTransactionSelection(transactionId: String) {
        _uiState.update { state ->
            val newSelection = if (state.selectedTransactionIds.contains(transactionId)) {
                state.selectedTransactionIds - transactionId
            } else {
                state.selectedTransactionIds + transactionId
            }
            state.copy(selectedTransactionIds = newSelection)
        }
    }
    
    fun selectAllTransactions() {
        _uiState.update { state ->
            state.copy(selectedTransactionIds = state.transactions.map { it.id }.toSet())
        }
    }
    
    fun clearSelection() {
        _uiState.update { 
            it.copy(selectedTransactionIds = emptySet())
        }
    }
    
    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedTransactionIds
            selectedIds.forEach { id ->
                transactionRepository.deleteTransaction(id)
            }
            _uiState.update { 
                it.copy(
                    isSelectionMode = false,
                    selectedTransactionIds = emptySet()
                )
            }
            loadMonthlySummary() // Refresh summary
        }
    }
    
    fun copyTransaction(transaction: Transaction) {
        viewModelScope.launch {
            // Create a new transaction with the same details but current time
            transactionRepository.addTransaction(
                amountCents = transaction.amountCents,
                categoryId = transaction.categoryId,
                note = transaction.note,
                accountId = transaction.accountId
            )
            loadMonthlySummary() // Refresh summary
        }
    }
    
    fun toggleSearchMode() {
        _uiState.update { 
            it.copy(
                isSearchMode = !it.isSearchMode,
                searchQuery = if (it.isSearchMode) "" else it.searchQuery,
                filteredTransactions = if (it.isSearchMode) emptyList() else it.filteredTransactions
            )
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isEmpty()) {
            _uiState.update { it.copy(filteredTransactions = emptyList()) }
        } else {
            viewModelScope.launch {
                transactionRepository.searchTransactions(query).collect { results ->
                    _uiState.update { it.copy(filteredTransactions = results) }
                }
            }
        }
    }
    
    fun clearSearch() {
        _uiState.update { 
            it.copy(
                searchQuery = "",
                filteredTransactions = emptyList()
            )
        }
    }
    
    fun toggleFilterDialog() {
        _uiState.update { it.copy(showFilterDialog = !it.showFilterDialog) }
    }
    
    fun updateFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
        applyFilter()
    }
    
    fun clearFilter() {
        _uiState.update { it.copy(activeFilter = TransactionFilter()) }
        loadTransactions()
        loadMonthlySummary()
    }
    
    private fun applyFilter() {
        viewModelScope.launch {
            val filter = _uiState.value.activeFilter
            transactionRepository.getTransactions().collect { allTransactions ->
                // First filter by selected month
                val monthFiltered = allTransactions.filter { transaction ->
                    val transactionDate = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val transactionMonth = YearMonth.of(transactionDate.year, transactionDate.monthNumber)
                    transactionMonth == _selectedMonth.value
                }
                
                val filtered = monthFiltered.filter { transaction ->
                    // Filter by transaction type
                    val typeMatch = when (filter.transactionType) {
                        TransactionType.ALL -> true
                        TransactionType.INCOME -> transaction.categoryDetails?.type == "INCOME"
                        TransactionType.EXPENSE -> transaction.categoryDetails?.type == "EXPENSE"
                    }
                    
                    // Filter by categories
                    val categoryMatch = if (filter.categoryIds.isEmpty()) {
                        true
                    } else {
                        filter.categoryIds.contains(transaction.categoryId)
                    }
                    
                    // Filter by amount range
                    val amountMatch = (filter.minAmount == null || transaction.amountYuan >= filter.minAmount) &&
                            (filter.maxAmount == null || transaction.amountYuan <= filter.maxAmount)
                    
                    // Filter by date range
                    val dateMatch = if (filter.dateRange == null) {
                        true
                    } else {
                        val transactionDate = Instant.fromEpochMilliseconds(transaction.createdAt.toEpochMilliseconds())
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val kotlinDate = kotlinx.datetime.LocalDate(
                            transactionDate.year,
                            transactionDate.monthNumber,
                            transactionDate.dayOfMonth
                        )
                        val startDate = kotlinx.datetime.LocalDate(
                            filter.dateRange.start.year,
                            filter.dateRange.start.monthValue,
                            filter.dateRange.start.dayOfMonth
                        )
                        val endDate = kotlinx.datetime.LocalDate(
                            filter.dateRange.end.year,
                            filter.dateRange.end.monthValue,
                            filter.dateRange.end.dayOfMonth
                        )
                        kotlinDate >= startDate && kotlinDate <= endDate
                    }
                    
                    // Filter by account (already handled in query)
                    val accountMatch = filter.accountId == null || transaction.accountId == filter.accountId
                    
                    typeMatch && categoryMatch && amountMatch && dateMatch && accountMatch
                }
                _uiState.update { it.copy(transactions = filtered) }
                
                // Update monthly summary based on filtered data
                updateFilteredSummary(filtered)
                
                // Update grouped transactions
                updateGroupedTransactions(filtered)
            }
        }
    }
    
    private fun updateFilteredSummary(filteredTransactions: List<Transaction>) {
        val income = filteredTransactions
            .filter { it.categoryDetails?.type == "INCOME" }
            .sumOf { it.amountCents }
        val expense = filteredTransactions
            .filter { it.categoryDetails?.type == "EXPENSE" }
            .sumOf { it.amountCents }
            
        _uiState.update { 
            it.copy(
                monthlyIncome = income / 100.0,
                monthlyExpense = expense / 100.0
            )
        }
    }
    
    fun setGroupingMode(mode: GroupingMode) {
        _uiState.update { it.copy(groupingMode = mode) }
        updateGroupedTransactions(_uiState.value.transactions)
    }
    
    fun filterByAccount(accountId: String?) {
        val currentFilter = _uiState.value.activeFilter
        _uiState.update { 
            it.copy(activeFilter = currentFilter.copy(accountId = accountId))
        }
        if (accountId != null) {
            applyFilter()
        } else {
            clearFilter()
        }
    }
    
    private fun updateGroupedTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            val groups = when (_uiState.value.groupingMode) {
                GroupingMode.NONE -> listOf(
                    TransactionGroup(
                        id = "all",
                        title = "所有交易",
                        transactions = transactions,
                        totalIncome = transactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                        totalExpense = transactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                    )
                )
                GroupingMode.DAY -> groupTransactionsByDay(transactions)
                GroupingMode.WEEK -> groupTransactionsByWeek(transactions)
                GroupingMode.MONTH -> groupTransactionsByMonth(transactions)
                GroupingMode.YEAR -> groupTransactionsByYear(transactions)
            }
            _groupedTransactions.value = groups
        }
    }
    
    private fun groupTransactionsByDay(transactions: List<Transaction>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        
        return transactions
            .groupBy { transaction ->
                transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            .map { (date, dayTransactions) ->
                val title = when (date) {
                    today -> "今天"
                    yesterday -> "昨天"
                    else -> {
                        val javaDate = date.toJavaLocalDate()
                        "${javaDate.monthValue}月${javaDate.dayOfMonth}日"
                    }
                }
                
                TransactionGroup(
                    id = date.toString(),
                    title = title,
                    subtitle = if (date != today && date != yesterday) {
                        val javaDate = date.toJavaLocalDate()
                        val dayOfWeek = when (javaDate.dayOfWeek.value) {
                            1 -> "周一"
                            2 -> "周二"
                            3 -> "周三"
                            4 -> "周四"
                            5 -> "周五"
                            6 -> "周六"
                            7 -> "周日"
                            else -> ""
                        }
                        dayOfWeek
                    } else null,
                    transactions = dayTransactions.sortedByDescending { it.createdAt },
                    totalIncome = dayTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = dayTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    private fun groupTransactionsByWeek(transactions: List<Transaction>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentWeek = now.date.toJavaLocalDate().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
        val currentYear = now.year
        
        return transactions
            .groupBy { transaction ->
                val date = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val javaDate = date.toJavaLocalDate()
                val week = javaDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                val year = date.year
                "$year-$week"
            }
            .map { (weekKey, weekTransactions) ->
                val parts = weekKey.split("-")
                val year = parts[0].toInt()
                val week = parts[1].toInt()
                
                val title = when {
                    year == currentYear && week == currentWeek -> "本周"
                    year == currentYear && week == currentWeek - 1 -> "上周"
                    else -> {
                        // Calculate week date range
                        val firstDayOfWeek = java.time.LocalDate.of(year, 1, 1)
                            .with(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(), week.toLong())
                            .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                        val lastDayOfWeek = firstDayOfWeek.plusDays(6)
                        "${firstDayOfWeek.monthValue}月${firstDayOfWeek.dayOfMonth}日 - ${lastDayOfWeek.monthValue}月${lastDayOfWeek.dayOfMonth}日"
                    }
                }
                
                TransactionGroup(
                    id = weekKey,
                    title = title,
                    subtitle = if (year != currentYear) "${year}年" else null,
                    transactions = weekTransactions.sortedByDescending { it.createdAt },
                    totalIncome = weekTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = weekTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    private fun groupTransactionsByMonth(transactions: List<Transaction>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentMonth = now.monthNumber
        val currentYear = now.year
        
        return transactions
            .groupBy { transaction ->
                val date = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                "${date.year}-${date.monthNumber}"
            }
            .map { (monthKey, monthTransactions) ->
                val parts = monthKey.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                
                val title = when {
                    year == currentYear && month == currentMonth -> "本月"
                    year == currentYear && month == currentMonth - 1 -> "上月"
                    year == currentYear -> "${month}月"
                    else -> "${year}年${month}月"
                }
                
                TransactionGroup(
                    id = monthKey,
                    title = title,
                    transactions = monthTransactions.sortedByDescending { it.createdAt },
                    totalIncome = monthTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = monthTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    private fun groupTransactionsByYear(transactions: List<Transaction>): List<TransactionGroup> {
        return transactions
            .groupBy { transaction ->
                transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).year
            }
            .map { (year, yearTransactions) ->
                TransactionGroup(
                    id = year.toString(),
                    title = "${year}年",
                    transactions = yearTransactions.sortedByDescending { it.createdAt },
                    totalIncome = yearTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = yearTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
}

data class LedgerUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val editingTransaction: Transaction? = null,
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet(),
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val filteredTransactions: List<Transaction> = emptyList(),
    val showFilterDialog: Boolean = false,
    val activeFilter: TransactionFilter = TransactionFilter(),
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val categories: List<Category> = emptyList(),
    val groupingMode: GroupingMode = GroupingMode.NONE,
    val budgetAlert: BudgetAlertInfo? = null
)

data class BudgetAlertInfo(
    val message: String,
    val isExceeded: Boolean
)

data class TransactionFilter(
    val categoryIds: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val transactionType: TransactionType = TransactionType.ALL,
    val accountId: String? = null
)

enum class TransactionType {
    ALL, INCOME, EXPENSE
}

data class DateRange(
    val start: LocalDate,
    val end: LocalDate
)

enum class GroupingMode {
    NONE, DAY, WEEK, MONTH, YEAR
}

data class TransactionGroup(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val transactions: List<Transaction>,
    val totalIncome: Int,
    val totalExpense: Int
) {
    val totalIncomeYuan: Double
        get() = totalIncome / 100.0
    
    val totalExpenseYuan: Double
        get() = totalExpense / 100.0
    
    val balance: Int
        get() = totalIncome - totalExpense
    
    val balanceYuan: Double
        get() = balance / 100.0
}