package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.usecase.plan.GetPlanByIdUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.UpdatePlanProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateProgressViewModel @Inject constructor(
    private val getPlanByIdUseCase: GetPlanByIdUseCase,
    private val updatePlanProgressUseCase: UpdatePlanProgressUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""
    
    data class UpdateProgressUiState(
        val isLoading: Boolean = false,
        val progress: Float = 0f,
        val hasChildren: Boolean = false,
        val error: String? = null,
        val isSaved: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UpdateProgressUiState())
    val uiState: StateFlow<UpdateProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadPlan()
    }
    
    private fun loadPlan() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                getPlanByIdUseCase(planId)?.let { plan ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progress = plan.progress.toFloat(),
                        hasChildren = plan.hasChildren
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "计划不存在"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载计划失败：${e.message}"
                )
            }
        }
    }
    
    fun updateProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(
            progress = progress,
            error = null
        )
    }
    
    fun saveProgress() {
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                updatePlanProgressUseCase(
                    planId = planId,
                    progress = state.progress
                )
                
                _uiState.value = state.copy(
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "保存失败：${e.message}"
                )
            }
        }
    }
}