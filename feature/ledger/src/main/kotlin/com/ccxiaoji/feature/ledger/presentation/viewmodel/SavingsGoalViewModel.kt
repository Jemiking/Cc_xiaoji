package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val repository: SavingsGoalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavingsGoalUiState())
    val uiState: StateFlow<SavingsGoalUiState> = _uiState.asStateFlow()
    
    val activeSavingsGoals = repository.getActiveSavingsGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedGoal = MutableStateFlow<SavingsGoal?>(null)
    val selectedGoal: StateFlow<SavingsGoal?> = _selectedGoal.asStateFlow()
    
    private val _contributions = MutableStateFlow<List<SavingsContribution>>(emptyList())
    val contributions: StateFlow<List<SavingsContribution>> = _contributions.asStateFlow()
    
    fun selectGoal(goalId: Long) {
        viewModelScope.launch {
            val goal = repository.getSavingsGoalById(goalId)
            _selectedGoal.value = goal
            
            goal?.let {
                repository.getContributionsByGoalId(goalId).collect { contributionList ->
                    _contributions.value = contributionList
                }
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
                val goal = SavingsGoal(
                    userId = "default_user", // TODO: Get from current user context
                    name = name,
                    targetAmount = targetAmount,
                    targetDate = targetDate,
                    description = description,
                    color = color,
                    iconName = iconName
                )
                repository.createSavingsGoal(goal)
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标创建成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "创建失败: ${e.message}")
                }
            }
        }
    }
    
    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.updateSavingsGoal(goal)
                _uiState.update { it.copy(isLoading = false, message = "储蓄目标更新成功") }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = "更新失败: ${e.message}")
                }
            }
        }
    }
    
    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deleteSavingsGoal(goal)
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
                val contribution = SavingsContribution(
                    goalId = goalId,
                    amount = amount,
                    note = note
                )
                repository.addContribution(contribution)
                
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
    
    fun deleteContribution(contribution: SavingsContribution) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deleteContribution(contribution)
                
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
    
    suspend fun calculateEstimatedCompletionDate(goal: SavingsGoal): LocalDate? {
        if (goal.isCompleted || goal.currentAmount <= 0) return null
        
        // Get recent contributions to calculate average monthly saving
        val contributions = repository.getRecentContributions(goal.id, 30)
        val totalContributions = contributions.filter { it.amount > 0 }.sumOf { it.amount }
        
        if (totalContributions > 0) {
            // Assume same saving rate continues
            val daysInPeriod = 30
            val dailyRate = totalContributions / daysInPeriod
            val remainingDays = (goal.remainingAmount / dailyRate).toInt()
            
            return LocalDate.now().plusDays(remainingDays.toLong())
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