package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.SavingsContributionItem
import com.ccxiaoji.feature.ledger.api.SavingsGoalItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.ccxiaoji.core.common.util.DateConverter
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()
    
    val activeSavingsGoals = ledgerApi.getSavingsGoalsFlow()
        .map { goals -> goals.filter { it.isActive } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedGoal = MutableStateFlow<SavingsGoalItem?>(null)
    val selectedGoal: StateFlow<SavingsGoalItem?> = _selectedGoal.asStateFlow()
    
    private val _contributions = MutableStateFlow<List<SavingsContributionItem>>(emptyList())
    val contributions: StateFlow<List<SavingsContributionItem>> = _contributions.asStateFlow()
    
    fun selectGoal(goalId: Long) {
        viewModelScope.launch {
            val goal = ledgerApi.getSavingsGoalById(goalId)
            _selectedGoal.value = goal
            
            goal?.let {
                val contributionList = ledgerApi.getSavingsContributions(goalId)
                _contributions.value = contributionList
            }
        }
    }
    
    fun createSavingsGoal(
        name: String,
        targetAmount: Double,
        targetDate: LocalDate?,
        description: String?,
        color: String,
        iconName: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val targetAmountCents = (targetAmount * 100).toLong()
                ledgerApi.createSavingsGoal(
                    name = name,
                    targetAmountCents = targetAmountCents,
                    targetDate = targetDate?.let { DateConverter.toJavaDate(it) },
                    description = description,
                    color = color,
                    iconName = iconName
                )
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标创建成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "创建失败: ${e.message}")
                }
            }
        }
    }
    
    fun updateSavingsGoal(goal: SavingsGoalItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ledgerApi.updateSavingsGoal(
                    goalId = goal.id,
                    name = goal.name,
                    targetAmountCents = goal.targetAmountCents,
                    targetDate = goal.targetDate,
                    description = goal.description,
                    color = goal.color,
                    iconName = goal.iconName
                )
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标更新成功") }
                // 刷新选中的目标
                selectGoal(goal.id)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "更新失败: ${e.message}")
                }
            }
        }
    }
    
    fun deleteSavingsGoal(goal: SavingsGoalItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ledgerApi.deleteSavingsGoal(goal.id)
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标删除成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "删除失败: ${e.message}")
                }
            }
        }
    }
    
    fun addContribution(goalId: Long, amount: Double, note: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val amountCents = (amount * 100).toLong()
                ledgerApi.addSavingsContribution(
                    goalId = goalId,
                    amountCents = amountCents,
                    note = note
                )
                
                // Refresh selected goal to show updated amount
                selectGoal(goalId)
                
                val action = if (amount > 0) "存入" else "取出"
                _uiState.update { 
                    it.copy(isLoading = false, message = "$action ¥${kotlin.math.abs(amount)} 成功")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "操作失败: ${e.message}")
                }
            }
        }
    }
    
    fun deleteContribution(contribution: SavingsContributionItem) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ledgerApi.deleteSavingsContribution(contribution.id)
                
                // Refresh selected goal to show updated amount
                selectGoal(contribution.goalId)
                
                _uiState.update { it.copy(isLoading = false, message = "记录删除成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "删除失败: ${e.message}")
                }
            }
        }
    }
    
    suspend fun calculateEstimatedCompletionDate(goal: SavingsGoalItem): LocalDate? {
        if (goal.isCompleted || goal.currentAmountCents <= 0) return null
        
        // Get recent contributions to calculate average monthly saving
        val contributions = ledgerApi.getSavingsContributions(goal.id)
        val recentContributions = contributions.takeLast(30)
        val totalContributionsCents = recentContributions
            .filter { it.amountCents > 0 }
            .sumOf { it.amountCents }
        
        if (totalContributionsCents > 0) {
            // Assume same saving rate continues
            val daysInPeriod = 30
            val dailyRate = totalContributionsCents.toDouble() / daysInPeriod
            val remainingDays = (goal.remainingAmountCents / dailyRate).toInt()
            
            return Clock.System.todayIn(TimeZone.currentSystemDefault())
                .plus(DatePeriod(days = remainingDays))
        }
        
        return null
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

data class SavingsGoalUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)