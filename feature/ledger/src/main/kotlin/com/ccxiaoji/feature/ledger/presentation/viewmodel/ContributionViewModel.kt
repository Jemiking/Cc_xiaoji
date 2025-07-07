package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import com.ccxiaoji.feature.ledger.domain.model.SavingsContribution
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class ContributionUiState(
    val isLoading: Boolean = true,
    val goalId: Long = 0L,
    val goalName: String = "",
    val currentAmount: String = "0",
    val targetAmount: String = "0",
    val goalProgress: Float = 0f,
    val amount: String = "",
    val note: String = "",
    val isDeposit: Boolean = true,
    val amountError: String? = null,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ContributionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {
    
    private val goalId: Long = savedStateHandle.get<String>("goalId")?.toLongOrNull() ?: 0L
    
    private val _uiState = MutableStateFlow(ContributionUiState(goalId = goalId))
    val uiState: StateFlow<ContributionUiState> = _uiState.asStateFlow()
    
    init {
        loadGoalInfo()
    }
    
    private fun loadGoalInfo() {
        viewModelScope.launch {
            try {
                val goal = savingsGoalRepository.getSavingsGoalById(goalId)
                if (goal != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            goalName = goal.name,
                            currentAmount = String.format("%.2f", goal.currentAmount),
                            targetAmount = String.format("%.2f", goal.targetAmount),
                            goalProgress = if (goal.targetAmount > 0) {
                                (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                            } else 0f
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "储蓄目标不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载储蓄目标失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun updateAmount(amount: String) {
        val filtered = amount.filter { char -> char.isDigit() || char == '.' }
        _uiState.update {
            it.copy(
                amount = filtered,
                amountError = null
            )
        }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun updateDepositMode(isDeposit: Boolean) {
        _uiState.update { it.copy(isDeposit = isDeposit) }
    }
    
    fun saveContribution() {
        viewModelScope.launch {
            // 验证输入
            val amountValue = _uiState.value.amount.toDoubleOrNull()
            
            if (amountValue == null || amountValue <= 0) {
                _uiState.update { it.copy(amountError = "请输入有效金额") }
                return@launch
            }
            
            // 检查取出金额是否超过当前余额
            if (!_uiState.value.isDeposit) {
                val currentAmount = _uiState.value.currentAmount.toDoubleOrNull() ?: 0.0
                if (amountValue > currentAmount) {
                    _uiState.update { it.copy(amountError = "取出金额不能超过当前余额") }
                    return@launch
                }
            }
            
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // 创建贡献记录
                val contribution = SavingsContribution(
                    id = 0, // 将由数据库生成
                    goalId = goalId,
                    amount = if (_uiState.value.isDeposit) amountValue else -amountValue,
                    note = _uiState.value.note.ifBlank { null },
                    createdAt = LocalDateTime.now()
                )
                
                // 保存贡献
                savingsGoalRepository.addContribution(contribution)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "保存失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}