package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.usecase.plan.DeletePlanUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.UpdatePlanProgressUseCase
import com.ccxiaoji.feature.plan.domain.usecase.plan.UpdatePlanStatusUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.CreateMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.UpdateMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.DeleteMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.ToggleMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.template.CreateTemplateFromPlanUseCase
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.feature.plan.presentation.components.GlobalErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * 计划详情页面的ViewModel
 */
@HiltViewModel
class PlanDetailViewModel @Inject constructor(
    private val planRepository: PlanRepository,
    private val deletePlanUseCase: DeletePlanUseCase,
    private val updatePlanProgressUseCase: UpdatePlanProgressUseCase,
    private val updatePlanStatusUseCase: UpdatePlanStatusUseCase,
    private val createMilestoneUseCase: CreateMilestoneUseCase,
    private val updateMilestoneUseCase: UpdateMilestoneUseCase,
    private val deleteMilestoneUseCase: DeleteMilestoneUseCase,
    private val toggleMilestoneUseCase: ToggleMilestoneUseCase,
    private val createTemplateFromPlanUseCase: CreateTemplateFromPlanUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlanDetailUiState())
    val uiState: StateFlow<PlanDetailUiState> = _uiState.asStateFlow()
    
    private var currentPlanId: String = ""
    
    /**
     * 加载计划数据
     */
    fun loadPlan(planId: String) {
        currentPlanId = planId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val plan = planRepository.getPlanById(planId)
                if (plan != null) {
                    _uiState.update { 
                        it.copy(
                            plan = plan,
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
     * 更新计划状态
     */
    fun updateStatus(status: PlanStatus) {
        viewModelScope.launch {
            try {
                val result = updatePlanStatusUseCase(currentPlanId, status)
                result.fold(
                    onSuccess = {
                        // 重新加载计划
                        loadPlan(currentPlanId)
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(error = exception.message ?: "更新状态失败")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "更新状态失败")
                }
            }
        }
    }
    
    /**
     * 更新计划进度
     */
    fun updateProgress(progress: Float) {
        val plan = _uiState.value.plan ?: return
        if (plan.hasChildren) return // 有子计划时不允许手动更新进度
        
        viewModelScope.launch {
            try {
                val result = updatePlanProgressUseCase(currentPlanId, progress)
                result.fold(
                    onSuccess = {
                        // 重新加载计划
                        loadPlan(currentPlanId)
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(error = exception.message ?: "更新进度失败")
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = e.message ?: "更新进度失败")
                }
            }
        }
    }
    
    /**
     * 切换里程碑状态
     */
    fun toggleMilestone(milestoneId: String) {
        viewModelScope.launch {
            try {
                val result = toggleMilestoneUseCase(milestoneId)
                result.fold(
                    onSuccess = {
                        // 重新加载计划以更新里程碑状态
                        loadPlan(currentPlanId)
                    },
                    onFailure = { exception ->
                        GlobalErrorHandler.showError(
                            exception.message ?: "更新里程碑状态失败"
                        )
                    }
                )
            } catch (e: Exception) {
                GlobalErrorHandler.showError(
                    e.message ?: "更新里程碑状态失败"
                )
            }
        }
    }
    
    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }
    
    /**
     * 隐藏删除确认对话框
     */
    fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }
    
    /**
     * 删除计划
     */
    fun deletePlan() {
        viewModelScope.launch {
            try {
                val result = deletePlanUseCase(currentPlanId)
                result.fold(
                    onSuccess = {
                        // 删除成功，页面会返回
                    },
                    onFailure = { exception ->
                        _uiState.update { 
                            it.copy(
                                error = exception.message ?: "删除计划失败",
                                showDeleteDialog = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "删除计划失败",
                        showDeleteDialog = false
                    )
                }
            }
        }
    }
    
    /**
     * 显示添加里程碑对话框
     */
    fun showAddMilestoneDialog() {
        _uiState.update { it.copy(showMilestoneDialog = true, editingMilestone = null) }
    }
    
    /**
     * 显示编辑里程碑对话框
     */
    fun showEditMilestoneDialog(milestone: Milestone) {
        _uiState.update { it.copy(showMilestoneDialog = true, editingMilestone = milestone) }
    }
    
    /**
     * 隐藏里程碑对话框
     */
    fun hideMilestoneDialog() {
        _uiState.update { it.copy(showMilestoneDialog = false, editingMilestone = null) }
    }
    
    /**
     * 保存里程碑
     */
    fun saveMilestone(milestone: Milestone) {
        viewModelScope.launch {
            try {
                val result = if (_uiState.value.editingMilestone == null) {
                    // 创建新里程碑
                    createMilestoneUseCase(currentPlanId, milestone)
                } else {
                    // 更新现有里程碑
                    updateMilestoneUseCase(milestone).map { Unit }
                }
                
                result.fold(
                    onSuccess = {
                        hideMilestoneDialog()
                        loadPlan(currentPlanId)
                    },
                    onFailure = { exception ->
                        GlobalErrorHandler.showError(
                            exception.message ?: "保存里程碑失败"
                        )
                    }
                )
            } catch (e: Exception) {
                GlobalErrorHandler.showError(
                    e.message ?: "保存里程碑失败"
                )
            }
        }
    }
    
    /**
     * 显示删除里程碑确认对话框
     */
    fun showDeleteMilestoneDialog(milestone: Milestone) {
        _uiState.update { it.copy(deletingMilestone = milestone) }
    }
    
    /**
     * 隐藏删除里程碑确认对话框
     */
    fun hideDeleteMilestoneDialog() {
        _uiState.update { it.copy(deletingMilestone = null) }
    }
    
    /**
     * 删除里程碑
     */
    fun deleteMilestone() {
        viewModelScope.launch {
            val milestone = _uiState.value.deletingMilestone ?: return@launch
            
            try {
                val result = deleteMilestoneUseCase(milestone.id)
                result.fold(
                    onSuccess = {
                        hideDeleteMilestoneDialog()
                        loadPlan(currentPlanId)
                    },
                    onFailure = { exception ->
                        GlobalErrorHandler.showError(
                            exception.message ?: "删除里程碑失败"
                        )
                    }
                )
            } catch (e: Exception) {
                GlobalErrorHandler.showError(
                    e.message ?: "删除里程碑失败"
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
     * 显示创建模板对话框
     */
    fun showCreateTemplateDialog() {
        _uiState.update { it.copy(showCreateTemplateDialog = true) }
    }
    
    /**
     * 隐藏创建模板对话框
     */
    fun hideCreateTemplateDialog() {
        _uiState.update { it.copy(showCreateTemplateDialog = false) }
    }
    
    /**
     * 从计划创建模板
     */
    fun createTemplateFromPlan(
        title: String,
        description: String,
        category: TemplateCategory,
        tags: List<String>,
        isPublic: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingTemplate = true) }
            
            try {
                val templateId = createTemplateFromPlanUseCase(
                    planId = currentPlanId,
                    templateTitle = title,
                    templateDescription = description,
                    category = category,
                    isPublic = isPublic
                )
                
                hideCreateTemplateDialog()
                _uiState.update { it.copy(isCreatingTemplate = false) }
                GlobalErrorHandler.showError("模板创建成功")
            } catch (e: Exception) {
                _uiState.update { it.copy(isCreatingTemplate = false) }
                GlobalErrorHandler.showError(
                    e.message ?: "创建模板失败"
                )
            }
        }
    }
}

/**
 * 计划详情页面的UI状态
 */
data class PlanDetailUiState(
    val plan: Plan? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val showMilestoneDialog: Boolean = false,
    val editingMilestone: Milestone? = null,
    val deletingMilestone: Milestone? = null,
    val showCreateTemplateDialog: Boolean = false,
    val isCreatingTemplate: Boolean = false
)