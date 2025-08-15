package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            // 简化实现：暂时只获取分类，不包含统计信息
            categoryRepository.getCategories().collect { categories ->
                val expenseCategories = categories
                    .filter { it.type == Category.Type.EXPENSE }
                    .map { CategoryWithStats(it, 0, 0L) }
                val incomeCategories = categories
                    .filter { it.type == Category.Type.INCOME }
                    .map { CategoryWithStats(it, 0, 0L) }
                
                _uiState.update { 
                    it.copy(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun createCategory(
        name: String,
        type: Category.Type,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                categoryRepository.createCategory(
                    name = name,
                    type = type.name,
                    icon = icon,
                    color = color,
                    parentId = null
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = e.message ?: "创建分类失败")
                }
            }
        }
    }
    
    fun updateCategory(
        categoryId: String,
        name: String,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                // 获取现有分类
                val existingCategory = categoryRepository.getCategoryById(categoryId) ?: return@launch
                
                // 创建更新后的分类对象
                val updatedCategory = existingCategory.copy(
                    name = name,
                    icon = icon,
                    color = color
                )
                
                categoryRepository.updateCategory(updatedCategory)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = e.message ?: "更新分类失败")
                }
            }
        }
    }
    
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                categoryRepository.deleteCategory(categoryId)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = e.message ?: "无法删除该分类"
                    )
                }
            }
        }
    }
    
    fun setEditingCategory(category: Category?) {
        _uiState.update { it.copy(editingCategory = category) }
    }
    
    fun toggleAddDialog(type: Category.Type? = null) {
        _uiState.update { 
            it.copy(
                showAddDialog = !it.showAddDialog,
                addingCategoryType = type ?: Category.Type.EXPENSE
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun setSelectedTab(tab: CategoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}

data class CategoryUiState(
    val expenseCategories: List<CategoryWithStats> = emptyList(),
    val incomeCategories: List<CategoryWithStats> = emptyList(),
    val isLoading: Boolean = true,
    val editingCategory: Category? = null,
    val showAddDialog: Boolean = false,
    val addingCategoryType: Category.Type = Category.Type.EXPENSE,
    val errorMessage: String? = null,
    val selectedTab: CategoryTab = CategoryTab.EXPENSE
)

enum class CategoryTab {
    EXPENSE, INCOME
}