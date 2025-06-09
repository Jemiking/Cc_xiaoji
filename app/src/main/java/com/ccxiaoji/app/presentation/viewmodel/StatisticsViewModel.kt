package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.core.database.dao.CategoryStatistic
import com.ccxiaoji.app.data.repository.TransactionRepository
import com.ccxiaoji.app.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
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
                // Load daily totals for trend chart
                val dailyTotals = transactionRepository.getDailyTotals(startDate, endDate)
                
                // Load category statistics
                val expenseCategories = transactionRepository.getCategoryStatistics(startDate, endDate, "EXPENSE")
                val incomeCategories = transactionRepository.getCategoryStatistics(startDate, endDate, "INCOME")
                
                // Load top transactions
                val topExpenses = transactionRepository.getTopTransactions(startDate, endDate, "EXPENSE", 10)
                val topIncomes = transactionRepository.getTopTransactions(startDate, endDate, "INCOME", 10)
                
                // Calculate savings rate
                val savingsRate = transactionRepository.calculateSavingsRate(startDate, endDate)
                
                // Calculate totals
                val totalIncome = incomeCategories.sumOf { it.totalAmount }
                val totalExpense = expenseCategories.sumOf { it.totalAmount }
                
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
                    balance = totalIncome - totalExpense
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
    val expenseCategories: List<CategoryStatistic> = emptyList(),
    val incomeCategories: List<CategoryStatistic> = emptyList(),
    val topExpenses: List<Transaction> = emptyList(),
    val topIncomes: List<Transaction> = emptyList(),
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