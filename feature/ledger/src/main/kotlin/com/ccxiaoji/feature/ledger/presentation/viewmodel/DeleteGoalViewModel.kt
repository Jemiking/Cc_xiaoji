package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.data.repository.SavingsGoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeleteGoalUiState(
    val goalId: Long = 0L,
    val goalName: String = "",
    val hasContributions: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class DeleteGoalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savingsGoalRepository: SavingsGoalRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeleteGoalUiState())
    val uiState: StateFlow<DeleteGoalUiState> = _uiState.asStateFlow()
    
    init {
        // 获取传递过来的目标信息
        val goalId = savedStateHandle.get<Long>("goalId") ?: 0L
        val goalName = savedStateHandle.get<String>("goalName") ?: ""
        
        _uiState.update { 
            it.copy(
                goalId = goalId,
                goalName = goalName
            ) 
        }
        
        checkContributions(goalId)
    }
    
    private fun checkContributions(goalId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 检查是否有贡献记录
                savingsGoalRepository.getContributionsByGoalId(goalId).collect { contributions ->
                    _uiState.update { 
                        it.copy(
                            hasContributions = contributions.isNotEmpty(),
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}