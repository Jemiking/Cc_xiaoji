package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.CategoryStat
import com.ccxiaoji.feature.ledger.api.TransactionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics(TimePeriod.THIS_MONTH)
    }
    
    fun selectTimePeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStatistics(period)
    }
    
    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _uiState.value = _uiState.value.copy(
            customStartDate = startDate,
            customEndDate = endDate,
            selectedPeriod = TimePeriod.CUSTOM
        )
        loadStatistics(TimePeriod.CUSTOM)
    }
    
    private fun loadStatistics(period: TimePeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val (startDate, endDate) = getDateRange(period)
            
            try {
                // Get transaction statistics for the period
                val stats = ledgerApi.getTransactionStatsByDateRange(startDate, endDate)
                
                // Get transactions for the period
                val yearMonth = YearMonth.of(startDate.year, startDate.monthNumber)
                val transactions = ledgerApi.getTransactionsByMonth(yearMonth.year, yearMonth.monthValue)
                
                // Filter expense and income categories from stats
                val expenseCategories = stats.categoryStats.filter { it.totalAmount < 0 }
                val incomeCategories = stats.categoryStats.filter { it.totalAmount > 0 }
                
                // Get top transactions (sorted by amount)
                val topExpenses = transactions
                    .filter { it.amount < 0 }
                    .sortedBy { it.amount }
                    .take(10)
                
                val topIncomes = transactions
                    .filter { it.amount > 0 }
                    .sortedByDescending { it.amount }
                    .take(10)
                
                // Calculate savings rate
                val totalIncome = stats.totalIncome
                val totalExpense = stats.totalExpense
                val savingsRate = if (totalIncome > 0) {
                    ((totalIncome - totalExpense).toFloat() / totalIncome.toFloat()) * 100f
                } else {
                    0f
                }
                
                // Create daily totals map (simplified for now)
                val dailyTotals = mutableMapOf<LocalDate, Pair<Int, Int>>()
                transactions.groupBy { it.date }.forEach { (date, dayTransactions) ->
                    val dayIncome = dayTransactions.filter { it.amount > 0 }.sumOf { (it.amount * 100).toInt() }
                    val dayExpense = dayTransactions.filter { it.amount < 0 }.sumOf { (-it.amount * 100).toInt() }
                    dailyTotals[date] = Pair(dayIncome, dayExpense)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyTotals = dailyTotals,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    topExpenses = topExpenses,
                    topIncomes = topIncomes,
                    savingsRate = savingsRate,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = stats.balance
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun getDateRange(period: TimePeriod): Pair<LocalDate, LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return when (period) {
            TimePeriod.THIS_MONTH -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                startOfMonth to today
            }
            TimePeriod.THIS_YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                startOfYear to today
            }
            TimePeriod.CUSTOM -> {
                val state = _uiState.value
                (state.customStartDate ?: today.minus(30, DateTimeUnit.DAY)) to 
                (state.customEndDate ?: today)
            }
        }
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: TimePeriod = TimePeriod.THIS_MONTH,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val dailyTotals: Map<LocalDate, Pair<Int, Int>> = emptyMap(),
    val expenseCategories: List<CategoryStat> = emptyList(),
    val incomeCategories: List<CategoryStat> = emptyList(),
    val topExpenses: List<TransactionItem> = emptyList(),
    val topIncomes: List<TransactionItem> = emptyList(),
    val savingsRate: Float = 0f,
    val totalIncome: Int = 0,
    val totalExpense: Int = 0,
    val balance: Int = 0
)

enum class TimePeriod {
    THIS_MONTH,
    THIS_YEAR,
    CUSTOM
}