package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import com.ccxiaoji.feature.plan.domain.usecase.template.ApplyTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import javax.inject.Inject

@HiltViewModel
class ApplyTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val templateRepository: TemplateRepository,
    private val applyTemplateUseCase: ApplyTemplateUseCase
) : ViewModel() {
    
    private val templateId: String = checkNotNull(savedStateHandle.get<String>("templateId"))
    
    private val _uiState = MutableStateFlow(ApplyTemplateUiState())
    val uiState = _uiState.asStateFlow()
    
    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            templateRepository.getTemplateById(templateId)
                .onSuccess { template ->
                    _uiState.update { state ->
                        state.copy(
                            template = template,
                            title = template.title,
                            isLoading = false
                        )
                    }
                }
                .onError { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "加载模板失败：${exception.message}"
                        )
                    }
                }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }
    
    fun updateParentPlanId(parentId: String?) {
        _uiState.update { it.copy(parentPlanId = parentId) }
    }
    
    fun applyTemplate() {
        val currentState = _uiState.value
        if (currentState.title.isBlank() || currentState.template == null) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val planId = applyTemplateUseCase(
                    templateId = templateId,
                    title = currentState.title,
                    startDate = currentState.startDate,
                    parentId = currentState.parentPlanId
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        appliedPlanId = planId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "应用模板失败：${e.message}"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ApplyTemplateUiState(
    val template: Template? = null,
    val title: String = "",
    val startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val parentPlanId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val appliedPlanId: String? = null
)