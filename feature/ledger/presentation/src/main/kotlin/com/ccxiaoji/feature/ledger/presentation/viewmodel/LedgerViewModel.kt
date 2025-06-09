package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.TransactionDetail
import com.ccxiaoji.feature.ledger.api.TransactionStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()
    
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()
    
    init {
        loadTransactions()
        loadCategories()
        loadMonthlyStats()
    }
    
    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
        loadTransactions()
        loadMonthlyStats()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val month = _selectedMonth.value
                val transactions = ledgerApi.getTransactionsByMonth(month.year, month.monthValue)
                
                _uiState.update { 
                    it.copy(
                        transactions = transactions,
                        isLoading = false
                    )
                }
                updateGroupedTransactions(transactions)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = ledgerApi.getAllCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            try {
                val month = _selectedMonth.value
                val startDate = LocalDate(month.year, month.monthValue, 1)
                val endDate = if (month.monthValue == 12) {
                    LocalDate(month.year + 1, 1, 1).minus(1, DateTimeUnit.DAY)
                } else {
                    LocalDate(month.year, month.monthValue + 1, 1).minus(1, DateTimeUnit.DAY)
                }
                
                val stats = ledgerApi.getTransactionStatsByDateRange(startDate, endDate)
                
                _uiState.update { 
                    it.copy(
                        monthlyIncome = stats.totalIncomeYuan,
                        monthlyExpense = stats.totalExpenseYuan
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun addTransaction(amountCents: Int, categoryId: String, note: String?, accountId: String? = null) {
        viewModelScope.launch {
            try {
                ledgerApi.addTransaction(amountCents, categoryId, note, accountId)
                loadTransactions() // Refresh
                loadMonthlyStats() // Refresh stats
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            try {
                ledgerApi.deleteTransaction(transactionId)
                loadTransactions() // Refresh
                loadMonthlyStats() // Refresh stats
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun deleteSelectedTransactions() {
        viewModelScope.launch {
            try {
                val selectedIds = _uiState.value.selectedTransactionIds.toList()
                ledgerApi.deleteTransactions(selectedIds)
                
                _uiState.update { 
                    it.copy(
                        isSelectionMode = false,
                        selectedTransactionIds = emptySet()
                    )
                }
                
                loadTransactions() // Refresh
                loadMonthlyStats() // Refresh stats
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun updateTransaction(transactionId: String, amountCents: Int, categoryId: String, note: String?) {
        viewModelScope.launch {
            try {
                ledgerApi.updateTransaction(transactionId, amountCents, categoryId, note)
                loadTransactions() // Refresh
                loadMonthlyStats() // Refresh stats
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message)
                }
            }
        }
    }
    
    fun searchTransactions(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            
            if (query.isEmpty()) {
                loadTransactions()
            } else {
                try {
                    val results = ledgerApi.searchTransactions(query)
                    _uiState.update { 
                        it.copy(transactions = results)
                    }
                    updateGroupedTransactions(results)
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(error = e.message)
                    }
                }
            }
        }
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
    
    fun setEditingTransaction(transactionId: String?) {
        viewModelScope.launch {
            if (transactionId != null) {
                try {
                    val detail = ledgerApi.getTransactionDetail(transactionId)
                    _uiState.update { 
                        it.copy(editingTransactionDetail = detail)
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(error = e.message)
                    }
                }
            } else {
                _uiState.update { 
                    it.copy(editingTransactionDetail = null)
                }
            }
        }
    }
    
    fun toggleSearchMode() {
        _uiState.update { 
            it.copy(
                isSearchMode = !it.isSearchMode,
                searchQuery = if (it.isSearchMode) "" else it.searchQuery
            )
        }
        
        if (_uiState.value.isSearchMode) {
            loadTransactions() // Reset to all transactions
        }
    }
    
    fun setGroupingMode(mode: GroupingMode) {
        _uiState.update { it.copy(groupingMode = mode) }
        updateGroupedTransactions(_uiState.value.transactions)
    }
    
    private fun updateGroupedTransactions(transactions: List<TransactionItem>) {
        viewModelScope.launch {
            val groups = when (_uiState.value.groupingMode) {
                GroupingMode.NONE -> listOf(
                    TransactionGroup(
                        id = "all",
                        title = "所有交易",
                        transactions = transactions,
                        totalIncome = transactions.filter { isCategoryIncome(it.categoryName) }.sumOf { (it.amount * 100).toInt() },
                        totalExpense = transactions.filter { !isCategoryIncome(it.categoryName) }.sumOf { (it.amount * 100).toInt() }
                    )
                )
                GroupingMode.DAY -> groupTransactionsByDay(transactions)
                GroupingMode.WEEK -> groupTransactionsByWeek(transactions)
                GroupingMode.MONTH -> groupTransactionsByMonth(transactions)
            }
            
            _uiState.update { 
                it.copy(groupedTransactions = groups)
            }
        }
    }
    
    private fun groupTransactionsByDay(transactions: List<TransactionItem>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        
        return transactions
            .groupBy { it.date }
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
                    transactions = dayTransactions,
                    totalIncome = dayTransactions.filter { isCategoryIncome(it.categoryName) }.sumOf { (it.amount * 100).toInt() },
                    totalExpense = dayTransactions.filter { !isCategoryIncome(it.categoryName) }.sumOf { (it.amount * 100).toInt() }
                )
            }
            .sortedByDescending { it.id }
    }
    
    private fun groupTransactionsByWeek(transactions: List<TransactionItem>): List<TransactionGroup> {
        // 简化实现，实际应该使用周分组逻辑
        return groupTransactionsByDay(transactions)
    }
    
    private fun groupTransactionsByMonth(transactions: List<TransactionItem>): List<TransactionGroup> {
        // 简化实现，实际应该使用月分组逻辑
        return groupTransactionsByDay(transactions)
    }
    
    // 临时辅助方法，判断分类是否为收入类型
    // 实际应该从分类信息中获取
    private fun isCategoryIncome(categoryName: String): Boolean {
        return categoryName.contains("工资") || categoryName.contains("收入") || 
               categoryName.contains("奖金") || categoryName.contains("红包")
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LedgerUiState(
    val transactions: List<TransactionItem> = emptyList(),
    val groupedTransactions: List<TransactionGroup> = emptyList(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingTransactionDetail: TransactionDetail? = null,
    val isSelectionMode: Boolean = false,
    val selectedTransactionIds: Set<String> = emptySet(),
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val groupingMode: GroupingMode = GroupingMode.DAY
)

enum class GroupingMode {
    NONE, DAY, WEEK, MONTH
}

data class TransactionGroup(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val transactions: List<TransactionItem>,
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