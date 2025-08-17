package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.usecase.GetCategoryTreeWithCacheUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.GetFrequentCategoriesUseCase
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 分类选择器ViewModel
 * 用于处理二级分类选择的UI逻辑
 */
@HiltViewModel
class CategoryPickerViewModel @Inject constructor(
    private val getCategoryTreeWithCache: GetCategoryTreeWithCacheUseCase,
    private val getFrequentCategories: GetFrequentCategoriesUseCase,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryPickerUiState())
    val uiState: StateFlow<CategoryPickerUiState> = _uiState.asStateFlow()
    
    /**
     * 初始化分类选择器
     * @param type 分类类型（INCOME/EXPENSE）
     * @param selectedCategoryId 当前选中的分类ID
     */
    fun initialize(type: Category.Type, selectedCategoryId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, categoryType = type) }
            
            try {
                val userId = userApi.getCurrentUserId()
                
                // 加载分类树
                val categoryTree = getCategoryTreeWithCache(
                    userId = userId,
                    type = type.name,
                    forceRefresh = false
                )
                
                // 加载常用分类
                val frequentCategories = getFrequentCategories(
                    userId = userId,
                    type = type.name,
                    limit = 8
                )
                
                // 如果有选中的分类，找到它所属的组
                var selectedGroup: CategoryGroup? = null
                var selectedCategory: Category? = null
                
                if (selectedCategoryId != null) {
                    for (group in categoryTree) {
                        val category = group.children.find { it.id == selectedCategoryId }
                        if (category != null) {
                            selectedGroup = group
                            selectedCategory = category
                            break
                        }
                    }
                }
                
                _uiState.update {
                    it.copy(
                        categoryGroups = categoryTree,
                        frequentCategories = frequentCategories,
                        selectedGroup = selectedGroup,
                        selectedCategory = selectedCategory,
                        expandedGroups = if (selectedGroup != null) {
                            mapOf(selectedGroup.parent.id to true)
                        } else {
                            emptyMap()
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message ?: "加载分类失败",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * 选择分类组（一级分类）
     * 展开/折叠该组的子分类
     */
    fun selectGroup(group: CategoryGroup) {
        _uiState.update { state ->
            val currentExpanded = state.expandedGroups[group.parent.id] ?: false
            state.copy(
                selectedGroup = if (currentExpanded) null else group,
                expandedGroups = state.expandedGroups + (group.parent.id to !currentExpanded)
            )
        }
    }
    
    /**
     * 选择具体分类（二级分类）
     */
    fun selectCategory(category: Category) {
        viewModelScope.launch {
            // 记录使用频率
            getFrequentCategories.recordCategoryUsage(category.id)
            
            // 找到所属的分类组
            val group = _uiState.value.categoryGroups.find { 
                it.children.any { child -> child.id == category.id }
            }
            
            _uiState.update {
                it.copy(
                    selectedCategory = category,
                    selectedGroup = group,
                    // 构建完整的分类信息
                    selectedCategoryInfo = if (group != null) {
                        SelectedCategoryInfo(
                            categoryId = category.id,
                            categoryName = category.name,
                            parentId = group.parent.id,
                            parentName = group.parent.name,
                            fullPath = "${group.parent.name}/${category.name}",
                            icon = category.icon,
                            color = category.color
                        )
                    } else {
                        null
                    }
                )
            }
        }
    }
    
    /**
     * 从常用分类中快速选择
     */
    fun selectFromFrequent(category: Category) {
        selectCategory(category)
    }
    
    /**
     * 搜索分类
     */
    fun searchCategories(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
            return
        }
        
        _uiState.update { state ->
            val results = mutableListOf<Category>()
            
            // 搜索所有二级分类
            state.categoryGroups.forEach { group ->
                results.addAll(
                    group.children.filter { 
                        it.name.contains(query, ignoreCase = true) ||
                        group.parent.name.contains(query, ignoreCase = true)
                    }
                )
            }
            
            state.copy(
                searchQuery = query,
                searchResults = results
            )
        }
    }
    
    /**
     * 清除搜索
     */
    fun clearSearch() {
        _uiState.update {
            it.copy(searchQuery = "", searchResults = emptyList())
        }
    }
    
    /**
     * 切换视图模式
     */
    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = if (it.viewMode == ViewMode.TREE) {
                    ViewMode.GRID
                } else {
                    ViewMode.TREE
                }
            )
        }
    }
    
    /**
     * 刷新分类数据
     */
    fun refresh() {
        val type = _uiState.value.categoryType ?: return
        val selectedId = _uiState.value.selectedCategory?.id
        initialize(type, selectedId)
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 分类选择器UI状态
 */
data class CategoryPickerUiState(
    val categoryGroups: List<CategoryGroup> = emptyList(),
    val frequentCategories: List<Category> = emptyList(),
    val selectedGroup: CategoryGroup? = null,
    val selectedCategory: Category? = null,
    val selectedCategoryInfo: SelectedCategoryInfo? = null,
    val expandedGroups: Map<String, Boolean> = emptyMap(),
    val searchQuery: String = "",
    val searchResults: List<Category> = emptyList(),
    val viewMode: ViewMode = ViewMode.TREE,
    val categoryType: Category.Type? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 视图模式
 */
enum class ViewMode {
    TREE,  // 树状展示
    GRID   // 网格展示
}