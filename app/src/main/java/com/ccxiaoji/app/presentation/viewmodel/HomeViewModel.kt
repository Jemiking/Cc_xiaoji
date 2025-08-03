package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.habit.api.HabitApi
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.app.data.repository.CountdownRepository
import com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.shared.user.api.UserApi
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.app.domain.model.Countdown
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.feature.plan.api.PlanApi
import com.ccxiaoji.app.data.preferences.ModulePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
// DateTimeUtils已移除 - 使用kotlinx.datetime替代
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi,
    private val countdownRepository: CountdownRepository,
    private val budgetRepository: BudgetRepository,
    private val savingsGoalRepository: SavingsGoalRepository,
    private val userApi: UserApi,
    private val accountRepository: AccountRepository,
    private val planApi: PlanApi,
    private val modulePreferences: ModulePreferencesRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 模块偏好设置
    val useClassicLayout = modulePreferences.useClassicLayout
    val hiddenModules = modulePreferences.hiddenModules
    
    // 切换布局模式
    fun toggleLayoutMode() {
        viewModelScope.launch {
            modulePreferences.toggleLayoutMode()
        }
    }
    
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
            val monthlyTotal = try {
                transactionRepository.getMonthlyTotal(now.year, now.monthNumber).getOrThrow()
            } catch (e: Exception) {
                0
            }
            val todayStart = now.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val todayEnd = now.date.plus(1, kotlinx.datetime.DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            transactionRepository.getTransactionsByDateRange(
                now.date,
                now.date
            ).collect { todayTransactions ->
                val todayIncome = todayTransactions
                    .filter { it.categoryDetails?.type == "INCOME" }
                    .sumOf { it.amountCents }
                val todayExpense = todayTransactions
                    .filter { it.categoryDetails?.type == "EXPENSE" }
                    .sumOf { it.amountCents }
                
                _uiState.value = _uiState.value.copy(
                    monthlyExpense = monthlyTotal / 100.0,
                    todayIncome = todayIncome / 100.0,
                    todayExpense = todayExpense / 100.0
                )
            }
        }
        
        viewModelScope.launch {
            // Load today's tasks count and completed count
            val tasks = todoApi.getTodayTasks()
            val totalCount = tasks.size
            val completedCount = tasks.count { it.completed }
            _uiState.value = _uiState.value.copy(
                todayTasks = totalCount,
                todayCompletedTasks = completedCount,
                todayTasksList = tasks
            )
        }
        
        viewModelScope.launch {
            // Load habits data
            combine(
                habitApi.getHabitsWithStreaks(),
                habitApi.getTodayCheckedHabitsCount()
            ) { habitsWithStreaks, todayCheckedCount ->
                Triple(habitsWithStreaks, todayCheckedCount, habitsWithStreaks.maxOfOrNull { it.currentStreak } ?: 0)
            }.collect { (habitsWithStreaks, todayCheckedCount, longestStreak) ->
                _uiState.value = _uiState.value.copy(
                    activeHabits = habitsWithStreaks.size,
                    todayCheckedHabits = todayCheckedCount,
                    longestHabitStreak = longestStreak,
                    habitStreaks = habitsWithStreaks
                )
            }
        }
        
        viewModelScope.launch {
            // Load recent transactions
            transactionRepository.getRecentTransactions(5).collect { transactions ->
                _uiState.value = _uiState.value.copy(recentTransactions = transactions)
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
            budgetRepository.getBudgetsWithSpent(now.year, now.monthNumber).collect { budgets ->
                // Calculate total budget and spent
                val totalBudgetCents = budgets.sumOf { it.budgetAmountCents }
                val totalSpentCents = budgets.sumOf { it.spentAmountCents }
                
                    val usagePercentage = if (totalBudgetCents > 0) {
                        (totalSpentCents.toFloat() / totalBudgetCents.toFloat()) * 100f
                    } else {
                        0f
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        budgetAmount = totalBudgetCents / 100.0,
                        budgetSpent = totalSpentCents / 100.0,
                        budgetUsagePercentage = usagePercentage
                    )
            }
        }
        
        viewModelScope.launch {
            // Load active savings goals
            savingsGoalRepository.getActiveSavingsGoals().collect { goals ->
                _uiState.value = _uiState.value.copy(savingsGoals = goals.take(3))
            }
        }
        
        viewModelScope.launch {
            // Load account balance
            val totalBalance = accountRepository.getTotalBalance()
            _uiState.value = _uiState.value.copy(totalAccountBalance = totalBalance)
        }
        
        viewModelScope.launch {
            // Load plan data
            try {
                val activePlans = planApi.getInProgressPlans(100)
                val todayPlans = planApi.getTodayPlans()
                
                // Calculate average progress
                val averageProgress = if (activePlans.isNotEmpty()) {
                    activePlans.map { it.progress }.average().toInt()
                } else {
                    0
                }
                
                _uiState.value = _uiState.value.copy(
                    activePlansCount = activePlans.size,
                    todayPlansCount = todayPlans.size,
                    planAverageProgress = averageProgress
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading plan data", e)
                // Keep default values on error
            }
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
    val recentTransactions: List<Transaction> = emptyList(),
    val todayTasksList: List<Task> = emptyList(),
    val habitStreaks: List<HabitWithStreak> = emptyList(),
    val upcomingCountdowns: List<Countdown> = emptyList(),
    val budgetAmount: Double = 0.0,
    val budgetSpent: Double = 0.0,
    val budgetUsagePercentage: Float = 0f,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val totalAccountBalance: Double = 0.0,
    val activePlansCount: Int = 0,
    val todayPlansCount: Int = 0,
    val planAverageProgress: Int = 0
)