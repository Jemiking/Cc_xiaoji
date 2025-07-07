package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.feature.plan.domain.repository.PlanRepository
import com.ccxiaoji.feature.plan.domain.usecase.template.CreateTemplateFromPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val planRepository: PlanRepository,
    private val createTemplateFromPlanUseCase: CreateTemplateFromPlanUseCase
) : ViewModel() {
    
    private val planId: String = checkNotNull(savedStateHandle.get<String>("planId"))
    
    private val _uiState = MutableStateFlow(CreateTemplateUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadPlanTitle()
    }
    
    private fun loadPlanTitle() {
        viewModelScope.launch {
            planRepository.getPlanById(planId)?.let { plan ->
                _uiState.update { state ->
                    state.copy(
                        title = "${plan.title} 模板"
                    )
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { state ->
            state.copy(
                title = title,
                titleError = if (title.isBlank()) "标题不能为空" else null
            )
        }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { state ->
            state.copy(
                description = description,
                descriptionError = if (description.isBlank()) "描述不能为空" else null
            )
        }
    }
    
    fun updateCategory(category: TemplateCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    fun updateTagsText(tagsText: String) {
        _uiState.update { it.copy(tagsText = tagsText) }
    }
    
    fun updateIsPublic(isPublic: Boolean) {
        _uiState.update { it.copy(isPublic = isPublic) }
    }
    
    fun createTemplate() {
        val currentState = _uiState.value
        if (!currentState.isValid) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val tags = currentState.tagsText
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                
                createTemplateFromPlanUseCase(
                    planId = planId,
                    templateTitle = currentState.title,
                    templateDescription = currentState.description,
                    category = currentState.selectedCategory,
                    isPublic = currentState.isPublic
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isTemplateCreated = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "创建模板失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateTemplateUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: TemplateCategory = TemplateCategory.OTHER,
    val tagsText: String = "",
    val isPublic: Boolean = false,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isTemplateCreated: Boolean = false
) {
    val isValid: Boolean
        get() = title.isNotBlank() && description.isNotBlank() &&
                titleError == null && descriptionError == null
}