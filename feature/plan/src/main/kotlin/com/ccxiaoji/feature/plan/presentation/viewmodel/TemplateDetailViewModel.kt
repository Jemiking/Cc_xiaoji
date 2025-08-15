package com.ccxiaoji.feature.plan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.usecase.template.ApplyTemplateUseCase
import com.ccxiaoji.feature.plan.domain.usecase.template.GetTemplatesUseCase
import com.ccxiaoji.common.base.BaseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class TemplateDetailUiState(
    val template: Template? = null,
    val isLoading: Boolean = false,
    val isApplying: Boolean = false,
    val error: String? = null,
    val appliedPlanId: String? = null
)

@HiltViewModel
class TemplateDetailViewModel @Inject constructor(
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val applyTemplateUseCase: ApplyTemplateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TemplateDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = getTemplatesUseCase.getTemplateById(templateId)) {
                is BaseResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            template = result.data,
                            isLoading = false
                        )
                    }
                }
                is BaseResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.exception.message ?: "获取模板详情失败"
                        )
                    }
                }
            }
        }
    }
    
    fun applyTemplate(
        templateId: String,
        title: String,
        startDate: LocalDate,
        parentId: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true) }
            
            try {
                val planId = applyTemplateUseCase(
                    templateId = templateId,
                    title = title,
                    startDate = startDate,
                    parentId = parentId
                )
                _uiState.update { 
                    it.copy(
                        isApplying = false,
                        appliedPlanId = planId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isApplying = false,
                        error = e.message ?: "应用模板失败"
                    )
                }
            }
        }
    }
    
    fun resetAppliedPlanId() {
        _uiState.update { it.copy(appliedPlanId = null) }
    }
}