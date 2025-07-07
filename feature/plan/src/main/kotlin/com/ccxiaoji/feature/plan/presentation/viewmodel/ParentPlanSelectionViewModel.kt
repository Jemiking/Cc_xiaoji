package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 父计划选择页面ViewModel
 */
@HiltViewModel
class ParentPlanSelectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository
) : ViewModel() {
    
    // 从导航参数获取选中的父计划ID和当前计划ID
    private val selectedParentId: String? = savedStateHandle.get<String>("selectedParentId")
    private val currentPlanId: String? = savedStateHandle.get<String>("currentPlanId")
    
    private val _uiState = MutableStateFlow(ParentPlanSelectionUiState(selectedId = selectedParentId))
    val uiState: StateFlow<ParentPlanSelectionUiState> = _uiState.asStateFlow()
    
    /**
     * 加载计划列表
     */
    fun loadPlans(excludePlanId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            planRepository.getAllPlansTree().collect { allPlans ->
                val filteredPlans = if (excludePlanId != null) {
                    // 排除当前计划及其子计划
                    filterOutPlanAndChildren(allPlans, excludePlanId)
                } else {
                    allPlans
                }
                
                _uiState.value = _uiState.value.copy(
                    plans = filteredPlans,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 切换展开/折叠状态
     */
    fun toggleExpand(planId: String) {
        _uiState.value = _uiState.value.let { state ->
            val newExpandedIds = if (state.expandedIds.contains(planId)) {
                state.expandedIds - planId
            } else {
                state.expandedIds + planId
            }
            state.copy(expandedIds = newExpandedIds)
        }
    }
    
    /**
     * 过滤掉指定计划及其所有子计划
     */
    private fun filterOutPlanAndChildren(plans: List<Plan>, excludeId: String): List<Plan> {
        return plans.mapNotNull { plan ->
            when {
                plan.id == excludeId -> null
                plan.hasChildren -> {
                    val filteredChildren = filterOutPlanAndChildren(plan.children, excludeId)
                    // 更新子计划列表
                    plan.copy(children = filteredChildren)
                }
                else -> plan
            }
        }
    }
}

/**
 * 父计划选择页面UI状态
 */
data class ParentPlanSelectionUiState(
    val plans: List<Plan> = emptyList(),
    val expandedIds: Set<String> = emptySet(),
    val selectedId: String? = null,
    val isLoading: Boolean = true
)