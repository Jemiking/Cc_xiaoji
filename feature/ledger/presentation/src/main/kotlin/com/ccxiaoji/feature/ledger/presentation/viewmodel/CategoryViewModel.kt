package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.LedgerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val ledgerApi: LedgerApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val allCategories = ledgerApi.getAllCategories()
                val expenseCategories = allCategories.filter { it.type == "EXPENSE" }
                val incomeCategories = allCategories.filter { it.type == "INCOME" }
                
                _uiState.update { 
                    it.copy(
                        expenseCategories = expenseCategories,
                        incomeCategories = incomeCategories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "加载分类失败：${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun createCategory(
        name: String,
        type: String,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                ledgerApi.addCategory(
                    name = name,
                    type = type,
                    icon = icon,
                    color = color
                )
                // 重新加载分类列表
                loadCategories()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "创建分类失败：${e.message}")
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
                ledgerApi.updateCategory(
                    categoryId = categoryId,
                    name = name,
                    icon = icon,
                    color = color
                )
                // 重新加载分类列表
                loadCategories()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "更新分类失败：${e.message}")
                }
            }
        }
    }
    
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                // 先检查使用次数
                val usageCount = ledgerApi.getCategoryUsageCount(categoryId)
                if (usageCount > 0) {
                    _uiState.update { 
                        it.copy(errorMessage = "无法删除该分类，已有 $usageCount 笔交易使用")
                    }
                    return@launch
                }
                
                // 检查是否为系统分类
                val category = _uiState.value.expenseCategories.find { it.id == categoryId }
                    ?: _uiState.value.incomeCategories.find { it.id == categoryId }
                    
                if (category?.isSystem == true) {
                    _uiState.update { 
                        it.copy(errorMessage = "无法删除系统分类")
                    }
                    return@launch
                }
                
                ledgerApi.deleteCategory(categoryId)
                // 重新加载分类列表
                loadCategories()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "删除分类失败：${e.message}")
                }
            }
        }
    }
    
    fun setEditingCategory(category: CategoryItem?) {
        _uiState.update { it.copy(editingCategory = category) }
    }
    
    fun toggleAddDialog(type: String? = null) {
        _uiState.update { 
            it.copy(
                showAddDialog = !it.showAddDialog,
                addingCategoryType = type ?: "EXPENSE"
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
    val expenseCategories: List<CategoryItem> = emptyList(),
    val incomeCategories: List<CategoryItem> = emptyList(),
    val isLoading: Boolean = true,
    val editingCategory: CategoryItem? = null,
    val showAddDialog: Boolean = false,
    val addingCategoryType: String = "EXPENSE",
    val errorMessage: String? = null,
    val selectedTab: CategoryTab = CategoryTab.EXPENSE
)

enum class CategoryTab {
    EXPENSE, INCOME
}