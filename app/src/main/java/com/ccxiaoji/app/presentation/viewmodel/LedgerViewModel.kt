package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.repository.AccountRepository
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.app.data.repository.BudgetRepository
import com.ccxiaoji.app.data.repository.UserRepository
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.Category
import com.ccxiaoji.app.domain.model.Transaction
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
import kotlinx.datetime.LocalDate

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val ledgerApi: LedgerApi,
    private val budgetRepository: BudgetRepository,
    private val userRepository: UserRepository
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
            try {
                val selectedMonth = _selectedMonth.value
                val transactions = ledgerApi.getTransactionsByMonth(
                    selectedMonth.year,
                    selectedMonth.monthValue
                )
                
                // Convert TransactionItem to Transaction for UI
                val domainTransactions = transactions.map { item ->
                    Transaction(
                        id = item.id,
                        amountCents = (item.amount * 100).toInt(),
                        amountYuan = item.amount,
                        categoryId = item.categoryName, // Using categoryName as temp ID
                        accountId = "", // Not provided by API
                        note = item.note,
                        createdAt = kotlinx.datetime.Clock.System.now(), // Using current time as placeholder
                        updatedAt = kotlinx.datetime.Clock.System.now(),
                        categoryDetails = Category(
                            id = "",
                            name = item.categoryName,
                            type = Category.Type.EXPENSE, // Default, should be determined from category
                            icon = item.categoryIcon ?: "💰",
                            color = item.categoryColor,
                            parentId = null,
                            displayOrder = 0,
                            isSystem = false,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now()
                        ),
                        accountDetails = Account(
                            id = "",
                            name = item.accountName,
                            type = Account.Type.CASH,
                            balance = 0.0,
                            icon = "💰",
                            color = "#000000",
                            displayOrder = 0,
                            includeInTotalAssets = true,
                            isDefault = false,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now()
                        )
                    )
                }
                
                // Apply additional filters if needed
                val accountId = _uiState.value.activeFilter.accountId
                val filteredTransactions = if (accountId != null) {
                    domainTransactions.filter { it.accountDetails?.name == accountId }
                } else {
                    domainTransactions
                }
                
                _uiState.update { it.copy(transactions = filteredTransactions) }
                updateGroupedTransactions(filteredTransactions)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun loadMonthlySummary() {
        viewModelScope.launch {
            val selectedMonth = _selectedMonth.value
            
            try {
                // Get statistics for the selected month
                val startDate = LocalDate(selectedMonth.year, selectedMonth.monthValue, 1)
                val endDate = LocalDate(selectedMonth.year, selectedMonth.monthValue, 
                    selectedMonth.lengthOfMonth())
                
                val statistics = ledgerApi.getStatisticsByDateRange(startDate, endDate)
                
                _uiState.update { 
                    it.copy(
                        monthlyIncome = statistics.totalIncome,
                        monthlyExpense = statistics.totalExpense
                    )
                }
            } catch (e: Exception) {
                // Handle error
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
            try {
                // 添加交易
                ledgerApi.addTransaction(
                    amountCents = amountCents,
                    categoryId = categoryId,
                    note = note,
                    accountId = accountId
                )
                
                // 检查预算
                checkBudgetAfterTransaction(categoryId)
                
                // Reload data
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private suspend fun checkBudgetAfterTransaction(categoryId: String) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val userId = userRepository.getCurrentUserId()
        
        // 检查分类预算
        val categoryBudgetAlert = budgetRepository.checkBudgetAlert(userId, now.year, now.monthNumber, categoryId)
        val categoryBudgetExceeded = budgetRepository.checkBudgetExceeded(userId, now.year, now.monthNumber, categoryId)
        
        // 检查总预算
        val totalBudgetAlert = budgetRepository.checkBudgetAlert(userId, now.year, now.monthNumber, null)
        val totalBudgetExceeded = budgetRepository.checkBudgetExceeded(userId, now.year, now.monthNumber, null)
        
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
            try {
                val categories = ledgerApi.getAllCategories()
                // 将CategoryItem转换为Category
                val domainCategories = categories.map { categoryItem ->
                    Category(
                        id = categoryItem.id,
                        name = categoryItem.name,
                        type = Category.Type.valueOf(categoryItem.type),
                        icon = categoryItem.icon,
                        color = categoryItem.color,
                        parentId = categoryItem.parentId,
                        displayOrder = 0, // LedgerApi中没有提供displayOrder
                        isSystem = categoryItem.isSystem,
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        updatedAt = kotlinx.datetime.Clock.System.now()
                    )
                }
                _uiState.update { it.copy(categories = domainCategories) }
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }
    
    fun setSelectedAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                ledgerApi.deleteTransaction(transactionId)
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                ledgerApi.updateTransaction(
                    transactionId = transaction.id,
                    amountCents = transaction.amountCents,
                    categoryId = transaction.categoryId,
                    note = transaction.note
                )
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                // Handle error
            }
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
            try {
                val selectedIds = _uiState.value.selectedTransactionIds
                ledgerApi.deleteTransactions(selectedIds.toList())
                _uiState.update { 
                    it.copy(
                        isSelectionMode = false,
                        selectedTransactionIds = emptySet()
                    )
                }
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun copyTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                // Create a new transaction with the same details but current time
                ledgerApi.addTransaction(
                    amountCents = transaction.amountCents,
                    categoryId = transaction.categoryId,
                    note = transaction.note,
                    accountId = transaction.accountId
                )
                loadTransactions()
                loadMonthlySummary()
            } catch (e: Exception) {
                // Handle error
            }
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
                try {
                    val results = ledgerApi.searchTransactions(query)
                    // Convert TransactionItem to Transaction
                    val domainTransactions = results.map { item ->
                        Transaction(
                            id = item.id,
                            amountCents = (item.amount * 100).toInt(),
                            amountYuan = item.amount,
                            categoryId = item.categoryName,
                            accountId = "",
                            note = item.note,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now(),
                            categoryDetails = Category(
                                id = "",
                                name = item.categoryName,
                                type = Category.Type.EXPENSE,
                                icon = item.categoryIcon ?: "💰",
                                color = item.categoryColor,
                                parentId = null,
                                displayOrder = 0,
                                isSystem = false,
                                createdAt = kotlinx.datetime.Clock.System.now(),
                                updatedAt = kotlinx.datetime.Clock.System.now()
                            ),
                            accountDetails = Account(
                                id = "",
                                name = item.accountName,
                                type = Account.Type.CASH,
                                balance = 0.0,
                                icon = "💰",
                                color = "#000000",
                                displayOrder = 0,
                                includeInTotalAssets = true,
                                isDefault = false,
                                createdAt = kotlinx.datetime.Clock.System.now(),
                                updatedAt = kotlinx.datetime.Clock.System.now()
                            )
                        )
                    }
                    _uiState.update { it.copy(filteredTransactions = domainTransactions) }
                } catch (e: Exception) {
                    // Handle error
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
            try {
                val filter = _uiState.value.activeFilter
                val selectedMonth = _selectedMonth.value
                
                // Get transactions for selected month
                val monthTransactions = ledgerApi.getTransactionsByMonth(
                    selectedMonth.year,
                    selectedMonth.monthValue
                )
                
                // Convert to domain model
                val allTransactions = monthTransactions.map { item ->
                    Transaction(
                        id = item.id,
                        amountCents = (item.amount * 100).toInt(),
                        amountYuan = item.amount,
                        categoryId = item.categoryName,
                        accountId = "",
                        note = item.note,
                        createdAt = kotlinx.datetime.Clock.System.now(),
                        updatedAt = kotlinx.datetime.Clock.System.now(),
                        categoryDetails = Category(
                            id = "",
                            name = item.categoryName,
                            type = if (item.amount > 0) Category.Type.INCOME else Category.Type.EXPENSE,
                            icon = item.categoryIcon ?: "💰",
                            color = item.categoryColor,
                            parentId = null,
                            displayOrder = 0,
                            isSystem = false,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now()
                        ),
                        accountDetails = Account(
                            id = "",
                            name = item.accountName,
                            type = Account.Type.CASH,
                            balance = 0.0,
                            icon = "💰",
                            color = "#000000",
                            displayOrder = 0,
                            includeInTotalAssets = true,
                            isDefault = false,
                            createdAt = kotlinx.datetime.Clock.System.now(),
                            updatedAt = kotlinx.datetime.Clock.System.now()
                        )
                    )
                }
                
                val filtered = allTransactions.filter { transaction ->
                    // Filter by transaction type
                    val typeMatch = when (filter.transactionType) {
                        TransactionType.ALL -> true
                        TransactionType.INCOME -> transaction.categoryDetails?.type == Category.Type.INCOME
                        TransactionType.EXPENSE -> transaction.categoryDetails?.type == Category.Type.EXPENSE
                    }
                    
                    // Filter by categories
                    val categoryMatch = if (filter.categoryIds.isEmpty()) {
                        true
                    } else {
                        filter.categoryIds.contains(transaction.categoryDetails?.name)
                    }
                    
                    // Filter by amount range
                    val amountMatch = (filter.minAmount == null || transaction.amountYuan >= filter.minAmount) &&
                            (filter.maxAmount == null || transaction.amountYuan <= filter.maxAmount)
                    
                    // Filter by account
                    val accountMatch = filter.accountId == null || transaction.accountDetails?.name == filter.accountId
                    
                    typeMatch && categoryMatch && amountMatch && accountMatch
                }
                
                _uiState.update { it.copy(transactions = filtered) }
                
                // Update monthly summary based on filtered data
                updateFilteredSummary(filtered)
                
                // Update grouped transactions
                updateGroupedTransactions(filtered)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun updateFilteredSummary(filteredTransactions: List<Transaction>) {
        val income = filteredTransactions
            .filter { it.categoryDetails?.type == Category.Type.INCOME }
            .sumOf { it.amountCents }
        val expense = filteredTransactions
            .filter { it.categoryDetails?.type == Category.Type.EXPENSE }
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
                        totalIncome = transactions.filter { it.categoryDetails?.type == Category.Type.INCOME }.sumOf { it.amountCents },
                        totalExpense = transactions.filter { it.categoryDetails?.type == Category.Type.EXPENSE }.sumOf { it.amountCents }
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
                    totalIncome = dayTransactions.filter { it.categoryDetails?.type == Category.Type.INCOME }.sumOf { it.amountCents },
                    totalExpense = dayTransactions.filter { it.categoryDetails?.type == Category.Type.EXPENSE }.sumOf { it.amountCents }
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
                    totalIncome = weekTransactions.filter { it.categoryDetails?.type == Category.Type.INCOME }.sumOf { it.amountCents },
                    totalExpense = weekTransactions.filter { it.categoryDetails?.type == Category.Type.EXPENSE }.sumOf { it.amountCents }
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
                    totalIncome = monthTransactions.filter { it.categoryDetails?.type == Category.Type.INCOME }.sumOf { it.amountCents },
                    totalExpense = monthTransactions.filter { it.categoryDetails?.type == Category.Type.EXPENSE }.sumOf { it.amountCents }
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
                    totalIncome = yearTransactions.filter { it.categoryDetails?.type == Category.Type.INCOME }.sumOf { it.amountCents },
                    totalExpense = yearTransactions.filter { it.categoryDetails?.type == Category.Type.EXPENSE }.sumOf { it.amountCents }
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