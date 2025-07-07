package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.PlanFilter
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanFilterViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // 获取传入的当前筛选条件（如果有）
    private val currentStatuses: Set<PlanStatus> = savedStateHandle.get<Array<String>>("statuses")
        ?.map { PlanStatus.valueOf(it) }
        ?.toSet() ?: emptySet()
    
    private val currentTags: Set<String> = savedStateHandle.get<Array<String>>("tags")
        ?.toSet() ?: emptySet()
    
    private val currentHasChildren: Boolean? = savedStateHandle.get<Boolean?>("hasChildren")
    
    private val _uiState = MutableStateFlow(
        PlanFilterUiState(
            selectedStatuses = currentStatuses,
            selectedTags = currentTags,
            hasChildren = currentHasChildren,
            hasActiveFilters = hasActiveFilters(currentStatuses, currentTags, currentHasChildren),
            // TODO: 从实际数据源获取可用标签
            availableTags = setOf("工作", "学习", "生活", "健康", "娱乐", "重要", "紧急")
        )
    )
    val uiState = _uiState.asStateFlow()
    
    fun toggleStatus(status: PlanStatus) {
        _uiState.update { state ->
            val newStatuses = if (status in state.selectedStatuses) {
                state.selectedStatuses - status
            } else {
                state.selectedStatuses + status
            }
            state.copy(
                selectedStatuses = newStatuses,
                hasActiveFilters = hasActiveFilters(newStatuses, state.selectedTags, state.hasChildren)
            )
        }
    }
    
    fun toggleTag(tag: String) {
        _uiState.update { state ->
            val newTags = if (tag in state.selectedTags) {
                state.selectedTags - tag
            } else {
                state.selectedTags + tag
            }
            state.copy(
                selectedTags = newTags,
                hasActiveFilters = hasActiveFilters(state.selectedStatuses, newTags, state.hasChildren)
            )
        }
    }
    
    fun updateHasChildren(hasChildren: Boolean?) {
        _uiState.update { state ->
            state.copy(
                hasChildren = hasChildren,
                hasActiveFilters = hasActiveFilters(state.selectedStatuses, state.selectedTags, hasChildren)
            )
        }
    }
    
    fun clearAllFilters() {
        _uiState.update { state ->
            state.copy(
                selectedStatuses = emptySet(),
                selectedTags = emptySet(),
                hasChildren = null,
                hasActiveFilters = false
            )
        }
    }
    
    fun applyFilter() {
        // 筛选条件将通过导航返回结果传递给PlanListScreen
        // PlanListScreen会调用PlanListViewModel的updateFilter方法
    }
    
    private fun hasActiveFilters(
        statuses: Set<PlanStatus>,
        tags: Set<String>,
        hasChildren: Boolean?
    ): Boolean {
        return statuses.isNotEmpty() || tags.isNotEmpty() || hasChildren != null
    }
}

data class PlanFilterUiState(
    val selectedStatuses: Set<PlanStatus> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val hasChildren: Boolean? = null,
    val availableTags: Set<String> = emptySet(),
    val hasActiveFilters: Boolean = false
)