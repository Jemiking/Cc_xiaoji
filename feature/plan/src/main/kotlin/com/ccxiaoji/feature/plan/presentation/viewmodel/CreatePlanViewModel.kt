package com.ccxiaoji.feature.plan.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.usecase.plan.CreatePlanUseCase
import com.ccxiaoji.feature.plan.presentation.components.GlobalErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 创建计划页面的ViewModel
 */
@HiltViewModel
class CreatePlanViewModel @Inject constructor(
    private val createPlanUseCase: CreatePlanUseCase,
    private val planRepository: PlanRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreatePlanUiState())
    val uiState: StateFlow<CreatePlanUiState> = _uiState.asStateFlow()
    
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
     * 设置父计划
     */
    fun setParentPlan(parentPlanId: String) {
        viewModelScope.launch {
            val parentPlan = planRepository.getPlanById(parentPlanId)
            if (parentPlan != null) {
                _uiState.update { state ->
                    state.copy(
                        parentPlan = ParentPlanInfo(
                            id = parentPlan.id,
                            title = parentPlan.title
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 设置父计划详情
     */
    fun setParentPlanDetails(parentPlanId: String, parentPlanTitle: String) {
        _uiState.update { state ->
            state.copy(
                parentPlan = if (parentPlanId.isNotEmpty()) {
                    ParentPlanInfo(
                        id = parentPlanId,
                        title = parentPlanTitle
                    )
                } else {
                    null
                }
            )
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
     * 创建计划
     */
    fun createPlan() {
        val state = _uiState.value
        
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
                val plan = Plan(
                    id = "", // 由UseCase生成
                    parentId = state.parentPlan?.id,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    startDate = state.startDate!!,
                    endDate = state.endDate!!,
                    status = PlanStatus.NOT_STARTED,
                    progress = 0f,
                    color = state.color,
                    priority = state.priority,
                    tags = state.tags,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    orderIndex = 0,
                    reminderSettings = null,
                    children = emptyList(),
                    milestones = emptyList()
                )
                
                val result = createPlanUseCase(plan)
                
                result.fold(
                    onSuccess = { planId ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                createdPlanId = planId
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(isLoading = false)
                        }
                        GlobalErrorHandler.showError(
                            exception.message ?: "创建计划失败"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false)
                }
                GlobalErrorHandler.showError(
                    e.message ?: "创建计划失败"
                )
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
    
    /**
     * 父计划信息
     */
    data class ParentPlanInfo(
        val id: String,
        val title: String
    )
}

/**
 * 创建计划页面的UI状态
 */
data class CreatePlanUiState(
    val title: String = "",
    val titleError: String? = null,
    val description: String = "",
    val startDate: LocalDate? = null,
    val startDateError: String? = null,
    val endDate: LocalDate? = null,
    val endDateError: String? = null,
    val parentPlan: CreatePlanViewModel.ParentPlanInfo? = null,
    val color: String = "#2196F3",
    val tags: List<String> = emptyList(),
    val priority: Int = 2,
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdPlanId: String? = null
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