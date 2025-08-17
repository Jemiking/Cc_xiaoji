package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.domain.usecase.GetCategoryTreeUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ValidateCategoryUseCase
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val getCategoryTree: GetCategoryTreeUseCase,
    private val manageCategory: ManageCategoryUseCase,
    private val validateCategory: ValidateCategoryUseCase,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = userApi.getCurrentUserId()
                
                // 获取分类树
                val expenseTree = getCategoryTree.getExpenseTree(userId)
                val incomeTree = getCategoryTree.getIncomeTree(userId)
                
                _uiState.update { 
                    it.copy(
                        expenseCategoryGroups = expenseTree,
                        incomeCategoryGroups = incomeTree,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "加载分类失败",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * 创建父分类（一级分类）
     */
    fun createParentCategory(
        name: String,
        type: Category.Type,
        icon: String,
        color: String
    ) {
        viewModelScope.launch {
            try {
                val userId = userApi.getCurrentUserId()
                
                // 验证分类名称
                val validation = validateCategory.validateCategoryName(
                    name = name,
                    parentId = null,
                    userId = userId,
                    type = type.name
                )
                
                if (!validation.isValid) {
                    _uiState.update { 
                        it.copy(errorMessage = validation.errorMessage)
                    }
                    return@launch
                }
                
                manageCategory.createParentCategory(
                    userId = userId,
                    name = name,
                    type = type.name,
                    icon = icon,
                    color = color
                )
                
                // 刷新分类列表
                loadCategories()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = e.message ?: "创建分类失败")
                }
            }
        }
    }
    
    /**
     * 创建子分类（二级分类）
     */
    fun createSubcategory(
        parentId: String,
        name: String,
        icon: String,
        color: String? = null
    ) {
        viewModelScope.launch {
            try {
                manageCategory.createSubcategory(
                    parentId = parentId,
                    name = name,
                    icon = icon,
                    color = color
                )
                
                // 刷新分类列表
                loadCategories()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = e.message ?: "创建子分类失败")
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
                // 验证是否可以删除
                val validation = validateCategory.canDeleteCategory(categoryId)
                
                if (!validation.isValid) {
                    _uiState.update { 
                        it.copy(errorMessage = validation.errorMessage)
                    }
                    return@launch
                }
                
                categoryRepository.deleteCategory(categoryId)
                
                // 刷新分类列表
                loadCategories()
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
    
    fun setEditingParentCategory(parentId: String?) {
        _uiState.update { it.copy(editingParentId = parentId) }
    }
    
    fun toggleAddDialog(type: Category.Type? = null, parentId: String? = null) {
        _uiState.update { 
            it.copy(
                showAddDialog = !it.showAddDialog,
                addingCategoryType = type ?: Category.Type.EXPENSE,
                editingParentId = parentId
            )
        }
    }
    
    /**
     * 展开/折叠分类组
     */
    fun toggleGroupExpansion(groupId: String) {
        _uiState.update { state ->
            val currentExpanded = state.expandedGroups[groupId] ?: false
            state.copy(
                expandedGroups = state.expandedGroups + (groupId to !currentExpanded)
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
    val expenseCategoryGroups: List<CategoryGroup> = emptyList(),
    val incomeCategoryGroups: List<CategoryGroup> = emptyList(),
    val expandedGroups: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
    val editingCategory: Category? = null,
    val editingParentId: String? = null,
    val showAddDialog: Boolean = false,
    val addingCategoryType: Category.Type = Category.Type.EXPENSE,
    val errorMessage: String? = null,
    val selectedTab: CategoryTab = CategoryTab.EXPENSE
)

enum class CategoryTab {
    EXPENSE, INCOME
}