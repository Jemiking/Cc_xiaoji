package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.app.data.repository.CountdownRepository
import kotlinx.coroutines.flow.flow
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.todo.api.TodoTask
import com.ccxiaoji.app.domain.model.Countdown
import com.ccxiaoji.feature.ledger.api.SavingsGoalItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val countdownRepository: CountdownRepository,
    private val userApi: UserApi
) : ViewModel() {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "HomeViewModel init started")
        try {
            loadDashboardData()
            Log.d(TAG, "HomeViewModel init completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error in HomeViewModel init", e)
            throw e
        }
    }
    
    private fun loadDashboardData() {
        Log.d(TAG, "Loading dashboard data")
        viewModelScope.launch {
            // Load monthly expense and today's income/expense
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val monthlyExpense = ledgerApi.getMonthlyExpense(now.year, now.monthNumber)
            val todayStatistics = ledgerApi.getTodayStatistics()
            
            _uiState.value = _uiState.value.copy(
                monthlyExpense = monthlyExpense,
                todayIncome = todayStatistics.income,
                todayExpense = todayStatistics.expense
            )
        }
        
        viewModelScope.launch {
            // Load today's tasks count and completed count
            val todayTasks = todoApi.getTodayTasks()
            val statistics = todoApi.getTaskStatistics()
            _uiState.value = _uiState.value.copy(
                todayTasks = todayTasks.size,
                todayCompletedTasks = todayTasks.count { it.isCompleted },
                todayTasksList = todayTasks
            )
        }
        
        viewModelScope.launch {
            // Load habits data
            combine(
                flow { emit(habitApi.getTodayHabitStatistics()) },
                habitApi.getTodayCheckedCount()
            ) { statistics, todayCheckedCount ->
                Pair(statistics, todayCheckedCount)
            }.collect { (statistics, todayCheckedCount) ->
                _uiState.value = _uiState.value.copy(
                    activeHabits = statistics.totalHabits,
                    todayCheckedHabits = todayCheckedCount,
                    longestHabitStreak = statistics.longestStreak
                )
            }
        }
        
        viewModelScope.launch {
            // Load recent transactions
            ledgerApi.getRecentTransactions(5).collect { transactionItems ->
                _uiState.value = _uiState.value.copy(recentTransactions = transactionItems)
            }
        }
        
        
        viewModelScope.launch {
            // Load upcoming countdowns
            countdownRepository.getUpcomingCountdowns(3).collect { countdowns ->
                _uiState.value = _uiState.value.copy(upcomingCountdowns = countdowns)
            }
        }
        
        viewModelScope.launch {
            // Load budget overview
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val totalBudget = ledgerApi.getTotalBudget(now.year, now.monthNumber)
            
            if (totalBudget != null) {
                _uiState.value = _uiState.value.copy(
                    budgetAmount = totalBudget.budgetAmountYuan,
                    budgetSpent = totalBudget.spentAmountYuan,
                    budgetUsagePercentage = totalBudget.usagePercentage
                )
            }
        }
        
        viewModelScope.launch {
            // Load active savings goals
            val goals = ledgerApi.getActiveSavingsGoals()
            _uiState.value = _uiState.value.copy(savingsGoals = goals.take(3))
        }
        
        viewModelScope.launch {
            // Load account balance
            val totalBalance = ledgerApi.getTotalBalance()
            _uiState.value = _uiState.value.copy(totalAccountBalance = totalBalance)
        }
    }
}

data class HomeUiState(
    val monthlyExpense: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val todayTasks: Int = 0,
    val todayCompletedTasks: Int = 0,
    val activeHabits: Int = 0,
    val todayCheckedHabits: Int = 0,
    val longestHabitStreak: Int = 0,
    val recentTransactions: List<TransactionItem> = emptyList(),
    val todayTasksList: List<TodoTask> = emptyList(),
    val upcomingCountdowns: List<Countdown> = emptyList(),
    val budgetAmount: Double = 0.0,
    val budgetSpent: Double = 0.0,
    val budgetUsagePercentage: Float = 0f,
    val savingsGoals: List<SavingsGoalItem> = emptyList(),
    val totalAccountBalance: Double = 0.0
)