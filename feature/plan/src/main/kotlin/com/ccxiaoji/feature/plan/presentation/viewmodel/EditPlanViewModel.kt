package com.ccxiaoji.feature.plan.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.usecase.plan.UpdatePlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 编辑计划页面的ViewModel
 */
@HiltViewModel
class EditPlanViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val updatePlanUseCase: UpdatePlanUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EditPlanUiState())
    val uiState: StateFlow<EditPlanUiState> = _uiState.asStateFlow()
    
    private var currentPlan: Plan? = null
    
    /**
     * 加载计划数据
     */
    fun loadPlan(planId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val plan = planRepository.getPlanById(planId)
                if (plan != null) {
                    currentPlan = plan
                    _uiState.update { state ->
                        state.copy(
                            planId = plan.id,
                            title = plan.title,
                            description = plan.description,
                            startDate = plan.startDate,
                            endDate = plan.endDate,
                            status = plan.status.name,
                            progress = plan.progress,
                            color = plan.color,
                            priority = plan.priority,
                            tags = plan.tags,
                            hasChildren = plan.hasChildren,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "计划不存在"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载计划失败"
                    )
                }
            }
        }
    }
    
    /**
     * 更新标题
     */
    fun updateTitle(title: String) {
        _uiState.update { state ->
            state.copy(
                title = title,
                titleError = validateTitle(title)
            )
        }
    }
    
    /**
     * 更新描述
     */
    fun updateDescription(description: String) {
        _uiState.update { state ->
            state.copy(description = description)
        }
    }
    
    /**
     * 更新开始日期
     */
    fun updateStartDate(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                startDate = date,
                startDateError = validateDates(date, state.endDate).first,
                endDateError = validateDates(date, state.endDate).second
            )
        }
    }
    
    /**
     * 更新结束日期
     */
    fun updateEndDate(date: LocalDate) {
        _uiState.update { state ->
            state.copy(
                endDate = date,
                startDateError = validateDates(state.startDate, date).first,
                endDateError = validateDates(state.startDate, date).second
            )
        }
    }
    
    /**
     * 更新状态
     */
    fun updateStatus(status: String) {
        _uiState.update { state ->
            state.copy(status = status)
        }
    }
    
    /**
     * 更新进度
     */
    fun updateProgress(progress: Float) {
        _uiState.update { state ->
            state.copy(progress = progress)
        }
    }
    
    /**
     * 更新颜色
     */
    fun updateColor(color: String) {
        _uiState.update { state ->
            state.copy(color = color)
        }
    }
    
    /**
     * 更新标签
     */
    fun updateTags(tags: List<String>) {
        _uiState.update { state ->
            state.copy(tags = tags)
        }
    }
    
    /**
     * 更新优先级
     */
    fun updatePriority(priority: Int) {
        _uiState.update { state ->
            state.copy(priority = priority)
        }
    }
    
    /**
     * 更新计划
     */
    fun updatePlan() {
        val state = _uiState.value
        val plan = currentPlan ?: return
        
        // 验证所有字段
        val titleError = validateTitle(state.title)
        val (startDateError, endDateError) = validateDates(state.startDate, state.endDate)
        
        if (titleError != null || startDateError != null || endDateError != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    titleError = titleError,
                    startDateError = startDateError,
                    endDateError = endDateError
                )
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val updatedPlan = plan.copy(
                    title = state.title.trim(),
                    description = state.description.trim(),
                    startDate = state.startDate!!,
                    endDate = state.endDate!!,
                    status = PlanStatus.valueOf(state.status),
                    progress = state.progress,
                    color = state.color,
                    priority = state.priority,
                    tags = state.tags,
                    updatedAt = Clock.System.now().toEpochMilliseconds()
                )
                
                val result = updatePlanUseCase(updatedPlan)
                
                result.fold(
                    onSuccess = {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isUpdated = true
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = exception.message ?: "更新计划失败"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "更新计划失败"
                    )
                }
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 验证标题
     */
    private fun validateTitle(title: String): String? {
        return when {
            title.isBlank() -> "标题不能为空"
            title.length > 100 -> "标题不能超过100个字符"
            else -> null
        }
    }
    
    /**
     * 验证日期
     */
    private fun validateDates(startDate: LocalDate?, endDate: LocalDate?): Pair<String?, String?> {
        var startError: String? = null
        var endError: String? = null
        
        if (startDate == null) {
            startError = "请选择开始日期"
        }
        
        if (endDate == null) {
            endError = "请选择结束日期"
        }
        
        if (startDate != null && endDate != null && startDate > endDate) {
            endError = "结束日期不能早于开始日期"
        }
        
        return startError to endError
    }
}

/**
 * 编辑计划页面的UI状态
 */
data class EditPlanUiState(
    val planId: String = "",
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val startDate: LocalDate? = null,
    val startDateError: String? = null,
    val endDate: LocalDate? = null,
    val endDateError: String? = null,
    val status: String = "NOT_STARTED",
    val progress: Float = 0f,
    val color: String = "#2196F3",
    val tags: List<String> = emptyList(),
    val priority: Int = 2,
    val hasChildren: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdated: Boolean = false
) {
    /**
     * 表单是否有效
     */
    val isValid: Boolean
        get() = title.isNotBlank() && 
                startDate != null && 
                endDate != null && 
                startDate <= endDate &&
                titleError == null &&
                startDateError == null &&
                endDateError == null
}