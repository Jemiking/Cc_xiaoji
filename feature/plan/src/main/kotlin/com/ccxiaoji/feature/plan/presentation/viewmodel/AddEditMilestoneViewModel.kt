package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Milestone
import com.ccxiaoji.feature.plan.domain.usecase.milestone.CreateMilestoneUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.GetMilestoneByIdUseCase
import com.ccxiaoji.feature.plan.domain.usecase.milestone.UpdateMilestoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditMilestoneViewModel @Inject constructor(
    private val createMilestoneUseCase: CreateMilestoneUseCase,
    private val updateMilestoneUseCase: UpdateMilestoneUseCase,
    private val getMilestoneByIdUseCase: GetMilestoneByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val planId: String = savedStateHandle.get<String>("planId") ?: ""
    private val milestoneId: String? = savedStateHandle.get<String>("milestoneId")
    
    data class AddEditMilestoneUiState(
        val isLoading: Boolean = false,
        val title: String = "",
        val description: String = "",
        val targetDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(7, DateTimeUnit.DAY),
        val titleError: String? = null,
        val error: String? = null,
        val isSaved: Boolean = false,
        val originalMilestone: Milestone? = null
    )
    
    private val _uiState = MutableStateFlow(AddEditMilestoneUiState())
    val uiState: StateFlow<AddEditMilestoneUiState> = _uiState.asStateFlow()
    
    init {
        if (milestoneId != null) {
            loadMilestone()
        }
    }
    
    private fun loadMilestone() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val milestone = getMilestoneByIdUseCase(milestoneId!!)
                if (milestone != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        title = milestone.title,
                        description = milestone.description,
                        targetDate = milestone.targetDate,
                        originalMilestone = milestone
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "里程碑不存在"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载里程碑失败：${e.message}"
                )
            }
        }
    }
    
    fun updateTitle(title: String) {
        val error = when {
            title.isBlank() -> "标题不能为空"
            title.length > 100 -> "标题不能超过100个字符"
            else -> null
        }
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = error,
            error = null
        )
    }
    
    fun updateDescription(description: String) {
        if (description.length <= 500) {
            _uiState.value = _uiState.value.copy(
                description = description,
                error = null
            )
        }
    }
    
    fun updateTargetDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            targetDate = date,
            error = null
        )
    }
    
    fun saveMilestone() {
        val state = _uiState.value
        
        if (state.title.isBlank() || state.titleError != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            try {
                if (milestoneId == null) {
                    // 创建新里程碑
                    val milestone = Milestone(
                        id = UUID.randomUUID().toString(),
                        planId = planId,
                        title = state.title.trim(),
                        description = state.description.trim(),
                        targetDate = state.targetDate,
                        isCompleted = false,
                        completedDate = null
                    )
                    
                    val result = createMilestoneUseCase(planId, milestone)
                    result.fold(
                        onSuccess = {
                            _uiState.value = state.copy(
                                isLoading = false,
                                isSaved = true
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = state.copy(
                                isLoading = false,
                                error = "创建里程碑失败：${exception.message}"
                            )
                        }
                    )
                } else {
                    // 更新现有里程碑
                    val originalMilestone = state.originalMilestone!!
                    val updatedMilestone = originalMilestone.copy(
                        title = state.title.trim(),
                        description = state.description.trim(),
                        targetDate = state.targetDate
                    )
                    
                    val result = updateMilestoneUseCase(updatedMilestone)
                    result.fold(
                        onSuccess = {
                            _uiState.value = state.copy(
                                isLoading = false,
                                isSaved = true
                            )
                        },
                        onFailure = { exception ->
                            _uiState.value = state.copy(
                                isLoading = false,
                                error = "更新里程碑失败：${exception.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "保存失败：${e.message}"
                )
            }
        }
    }
}