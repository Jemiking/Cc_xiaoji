package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.usecase.plan.DeletePlanUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.GetPlanByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeletePlanViewModel @Inject constructor(
    private val getPlanByIdUseCase: GetPlanByIdUseCase,
    private val deletePlanUseCase: DeletePlanUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DeletePlanUiState())
    val uiState: StateFlow<DeletePlanUiState> = _uiState.asStateFlow()
    
    fun loadPlan(planId: String) {
        viewModelScope.launch {
            val plan = getPlanByIdUseCase(planId)
            _uiState.value = _uiState.value.copy(
                plan = plan,
                planId = planId
            )
        }
    }
    
    fun deletePlan() {
        val planId = _uiState.value.planId ?: return
        
        viewModelScope.launch {
            deletePlanUseCase(planId)
        }
    }
}

data class DeletePlanUiState(
    val plan: Plan? = null,
    val planId: String? = null
)