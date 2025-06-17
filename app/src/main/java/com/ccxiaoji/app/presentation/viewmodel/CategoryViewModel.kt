package com.ccxiaoji.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.app.data.repository.CategoryRepository
import com.ccxiaoji.app.data.repository.CategoryWithStats
import com.ccxiaoji.app.domain.model.Category
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
    
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategoriesWithUsageStats().collect { categoriesWithStats ->
                val expenseCategories = categoriesWithStats.filter { it.category.type == Category.Type.EXPENSE }
                val incomeCategories = categoriesWithStats.filter { it.category.type == Category.Type.INCOME }
                
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
            categoryRepository.createCategory(
                name = name,
                type = type,
                icon = icon,
                color = color
            )
        }
    }
    
    fun updateCategory(
        categoryId: String,
        name: String? = null,
        icon: String? = null,
        color: String? = null
    ) {
        viewModelScope.launch {
            categoryRepository.updateCategory(
                categoryId = categoryId,
                name = name,
                icon = icon,
                color = color
            )
        }
    }
    
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val success = categoryRepository.deleteCategory(categoryId)
            if (!success) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "无法删除该分类，可能是系统分类或已有交易使用"
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