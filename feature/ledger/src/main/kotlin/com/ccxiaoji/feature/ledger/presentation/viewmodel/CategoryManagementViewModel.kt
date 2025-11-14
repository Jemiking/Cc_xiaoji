package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.usecase.GetCategoryTreeUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ManageCategoryUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.ValidateCategoryUseCase
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 分类管理ViewModel
 * 用于处理分类的增删改查操作
 */
@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val getCategoryTree: GetCategoryTreeUseCase,
    private val manageCategory: ManageCategoryUseCase,
    private val validateCategory: ValidateCategoryUseCase,
    private val userApi: UserApi
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    /**
     * 加载分类数据
     */
    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val userId = userApi.getCurrentUserId()
                println("🔧 [CategoryManagementViewModel] 开始加载分类，用户ID: $userId")
                
                // 尝试强制重新初始化默认分类（非关键操作，失败不影响正常加载）
                try {
                    println("🔧 [CategoryManagementViewModel] 开始强制重新初始化默认分类")
                    manageCategory.checkAndInitializeDefaultCategories(userId, forceReinitialize = true)
                    println("🔧 [CategoryManagementViewModel] 强制重新初始化完成")
                } catch (initException: Exception) {
                    println("⚠️ [CategoryManagementViewModel] 强制重新初始化失败，继续正常加载: ${initException.message}")
                    // 初始化失败，尝试正常的默认分类检查
                    try {
                        manageCategory.checkAndInitializeDefaultCategories(userId, forceReinitialize = false)
                        println("🔧 [CategoryManagementViewModel] 正常分类检查完成")
                    } catch (normalInitException: Exception) {
                        println("⚠️ [CategoryManagementViewModel] 正常分类检查也失败，但继续加载现有分类: ${normalInitException.message}")
                    }
                }
                
                // 清除缓存以确保获取最新数据
                println("🔧 [CategoryManagementViewModel] 清除分类缓存")
                getCategoryTree.refreshCategoryTree(userId, "EXPENSE")
                getCategoryTree.refreshCategoryTree(userId, "INCOME")
                
                val expenseGroups = getCategoryTree.getExpenseTree(userId)
                val incomeGroups = getCategoryTree.getIncomeTree(userId)
                
                println("🔧 [CategoryManagementViewModel] 获取分类树结果:")
                println("   - 支出分类组数: ${expenseGroups.size}")
                println("   - 收入分类组数: ${incomeGroups.size}")
                
                expenseGroups.forEachIndexed { index, group ->
                    println("   - 支出组 $index: ${group.parent.name} (子分类 ${group.children.size} 个)")
                    group.children.forEach { child ->
                        println("     └─ ${child.name}")
                    }
                }
                
                incomeGroups.forEachIndexed { index, group ->
                    println("   - 收入组 $index: ${group.parent.name} (子分类 ${group.children.size} 个)")
                    group.children.forEach { child ->
                        println("     └─ ${child.name}")
                    }
                }
                
                _uiState.update {
                    it.copy(
                        expenseGroups = expenseGroups,
                        incomeGroups = incomeGroups,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                println("❌ [CategoryManagementViewModel] 加载分类失败: ${e.message}")
                e.printStackTrace()
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
     * 切换Tab
     */
    fun selectTab(tab: CategoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    /**
     * 展开/折叠分类组
     */
    fun toggleGroupExpansion(groupId: String) {
        _uiState.update { state ->
            val currentExpanded = state.expandedGroups[groupId] ?: true
            state.copy(
                expandedGroups = state.expandedGroups + (groupId to !currentExpanded)
            )
        }
    }
    
    /**
     * 显示添加父分类对话框
     */
    fun showAddParentDialog(type: Category.Type) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                dialogMode = DialogMode.ADD_PARENT,
                dialogCategoryType = type,
                dialogParentId = null
            )
        }
    }
    
    /**
     * 显示添加子分类对话框
     */
    fun showAddChildDialog(parentGroup: CategoryGroup) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                dialogMode = DialogMode.ADD_CHILD,
                dialogCategoryType = parentGroup.parent.type,
                dialogParentId = parentGroup.parent.id,
                dialogParentName = parentGroup.parent.name
            )
        }
    }
    
    /**
     * 显示编辑分类对话框
     */
    fun showEditDialog(category: Category, parentName: String? = null) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                dialogMode = if (category.level == 1) DialogMode.EDIT_PARENT else DialogMode.EDIT_CHILD,
                editingCategory = category,
                dialogParentName = parentName,
                dialogName = category.name,
                dialogIcon = category.icon,
                dialogColor = category.color
            )
        }
    }
    
    /**
     * 关闭对话框
     */
    fun closeDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                dialogMode = DialogMode.ADD_PARENT,
                editingCategory = null,
                dialogName = "",
                dialogIcon = "",
                dialogColor = "",
                dialogError = null
            )
        }
    }
    
    /**
     * 更新对话框输入
     */
    fun updateDialogInput(name: String? = null, icon: String? = null, color: String? = null) {
        _uiState.update {
            it.copy(
                dialogName = name ?: it.dialogName,
                dialogIcon = icon ?: it.dialogIcon,
                dialogColor = color ?: it.dialogColor,
                dialogError = null
            )
        }
    }
    
    /**
     * 保存分类（根据对话框模式）
     */
    fun saveCategory() {
        viewModelScope.launch {
            val state = _uiState.value
            val userId = userApi.getCurrentUserId()
            
            
            try {
                when (state.dialogMode) {
                    DialogMode.ADD_PARENT -> {
                        // 验证分类名称
                        val validation = validateCategory.validateCategoryName(
                            name = state.dialogName,
                            parentId = null,
                            userId = userId,
                            type = state.dialogCategoryType?.name ?: "EXPENSE"
                        )
                        
                        if (!validation.isValid) {
                            _uiState.update { it.copy(dialogError = validation.errorMessage) }
                            return@launch
                        }
                        
                        val finalIcon = state.dialogIcon.ifEmpty { "📝" }
                        val finalColor = state.dialogColor.ifEmpty { "#6200EE" }
                        
                        
                        manageCategory.createParentCategory(
                            userId = userId,
                            name = state.dialogName,
                            type = state.dialogCategoryType?.name ?: "EXPENSE",
                            icon = finalIcon,
                            color = finalColor
                        )
                        
                    }
                    
                    DialogMode.ADD_CHILD -> {
                        if (state.dialogParentId == null) return@launch

                        val finalIcon = state.dialogIcon.ifEmpty { "📝" }
                        val finalColor = state.dialogColor.ifEmpty { null }
                        val userId = userApi.getCurrentUserId()

                        manageCategory.createSubcategory(
                            userId = userId,
                            parentId = state.dialogParentId,
                            name = state.dialogName,
                            icon = finalIcon,
                            color = finalColor
                        )
                        
                    }
                    
                    DialogMode.EDIT_PARENT, DialogMode.EDIT_CHILD -> {
                        val category = state.editingCategory ?: return@launch
                        
                        val newName = state.dialogName.ifEmpty { null }
                        val newIcon = state.dialogIcon.ifEmpty { null }
                        val newColor = state.dialogColor.ifEmpty { null }
                        
                        
                        manageCategory.updateCategory(
                            category = category,
                            newName = newName,
                            newIcon = newIcon,
                            newColor = newColor
                        )
                        
                    }
                }
                
                // 关闭对话框并刷新数据
                closeDialog()
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(dialogError = e.message ?: "操作失败")
                }
            }
        }
    }
    
    /**
     * 删除分类
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                val userId = userApi.getCurrentUserId()
                // 验证是否可以删除
                val validation = validateCategory.canDeleteCategory(categoryId, userId)
                
                if (!validation.isValid) {
                    _uiState.update {
                        it.copy(error = validation.errorMessage)
                    }
                    return@launch
                }
                
                manageCategory.deleteCategory(categoryId)
                
                // 刷新数据
                loadCategories()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "删除失败")
                }
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 分类管理UI状态
 */
data class CategoryManagementUiState(
    val expenseGroups: List<CategoryGroup> = emptyList(),
    val incomeGroups: List<CategoryGroup> = emptyList(),
    val expandedGroups: Map<String, Boolean> = emptyMap(),
    val selectedTab: CategoryTab = CategoryTab.EXPENSE,
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // 对话框状态
    val showAddDialog: Boolean = false,
    val dialogMode: DialogMode = DialogMode.ADD_PARENT,
    val dialogCategoryType: Category.Type? = null,
    val dialogParentId: String? = null,
    val dialogParentName: String? = null,
    val editingCategory: Category? = null,
    val dialogName: String = "",
    val dialogIcon: String = "",
    val dialogColor: String = "",
    val dialogError: String? = null
) {
    val currentGroups: List<CategoryGroup>
        get() = if (selectedTab == CategoryTab.EXPENSE) expenseGroups else incomeGroups
}

/**
 * 对话框模式
 */
enum class DialogMode {
    ADD_PARENT,   // 添加父分类
    ADD_CHILD,    // 添加子分类
    EDIT_PARENT,  // 编辑父分类
    EDIT_CHILD    // 编辑子分类
}