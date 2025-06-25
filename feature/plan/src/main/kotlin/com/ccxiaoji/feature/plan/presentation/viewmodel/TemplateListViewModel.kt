package com.ccxiaoji.feature.plan.presentation.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.feature.plan.domain.usecase.template.GetTemplatesUseCase
import com.ccxiaoji.feature.plan.domain.usecase.template.ApplyTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * 模板列表页面ViewModel
 */
@HiltViewModel
class TemplateListViewModel @Inject constructor(
    private val getTemplatesUseCase: GetTemplatesUseCase,
    private val applyTemplateUseCase: ApplyTemplateUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(TemplateListUiState())
    val uiState: StateFlow<TemplateListUiState> = _uiState.asStateFlow()
    
    init {
        loadTemplates()
    }
    
    /**
     * 加载模板列表
     */
    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                getTemplatesUseCase.getAllTemplates()
                    .collect { templates ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            templates = templates,
                            filteredTemplates = filterTemplatesByCategory(templates, _uiState.value.selectedCategory),
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载模板失败"
                )
            }
        }
    }
    
    /**
     * 切换分类筛选
     */
    fun selectCategory(category: TemplateCategory?) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            filteredTemplates = filterTemplatesByCategory(_uiState.value.templates, category)
        )
    }
    
    /**
     * 搜索模板
     */
    fun searchTemplates(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        val filtered = if (query.isBlank()) {
            filterTemplatesByCategory(_uiState.value.templates, _uiState.value.selectedCategory)
        } else {
            _uiState.value.templates.filter { template ->
                template.title.contains(query, ignoreCase = true) ||
                template.description.contains(query, ignoreCase = true) ||
                template.tags.any { it.contains(query, ignoreCase = true) }
            }.let { templates ->
                if (_uiState.value.selectedCategory != null) {
                    templates.filter { it.category == _uiState.value.selectedCategory }
                } else {
                    templates
                }
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredTemplates = filtered)
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 应用模板创建计划
     */
    fun applyTemplate(
        templateId: String,
        title: String,
        startDate: LocalDate,
        parentPlanId: String? = null
    ) {
        if (_uiState.value.isApplying) return  // 防止重复提交
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplying = true, error = null)
            
            try {
                val planId = applyTemplateUseCase(
                    templateId = templateId,
                    title = title,
                    startDate = startDate,
                    parentId = parentPlanId
                )
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    appliedPlanId = planId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isApplying = false,
                    error = e.message ?: "应用模板失败"
                )
            }
        }
    }
    
    /**
     * 清除应用成功的计划ID（导航后调用）
     */
    fun clearAppliedPlanId() {
        _uiState.value = _uiState.value.copy(appliedPlanId = null)
    }
    
    /**
     * 根据分类筛选模板
     */
    private fun filterTemplatesByCategory(
        templates: List<Template>,
        category: TemplateCategory?
    ): List<Template> {
        return if (category == null) {
            templates
        } else {
            templates.filter { it.category == category }
        }
    }
}

/**
 * 模板列表页面UI状态
 */
data class TemplateListUiState(
    val isLoading: Boolean = false,
    val isApplying: Boolean = false,
    val templates: List<Template> = emptyList(),
    val filteredTemplates: List<Template> = emptyList(),
    val selectedCategory: TemplateCategory? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val appliedPlanId: String? = null  // 应用模板成功后的计划ID
)