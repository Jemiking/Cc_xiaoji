package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.data.defaults.DefaultCategories
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 管理分类的UseCase
 * 处理分类的创建、更新、删除等操作
 */
class ManageCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    /**
     * 创建父分类（一级分类）
     */
    suspend fun createParentCategory(
        userId: String,
        name: String,
        type: String,
        icon: String,
        color: String
    ): Long {
        // 验证名称不重复
        val existingCategories = categoryRepository.getParentCategories(userId, type)
        if (existingCategories.any { it.name == name }) {
            throw IllegalArgumentException("分类名称已存在")
        }
        
        return categoryRepository.createCategory(
            name = name,
            type = type,
            icon = icon,
            color = color,
            parentId = null
        )
    }
    
    /**
     * 创建子分类（二级分类）
     */
    suspend fun createSubcategory(
        parentId: String,
        name: String,
        icon: String,
        color: String? = null
    ): String {
        // 验证父分类存在
        val parent = categoryRepository.getCategoryById(parentId)
            ?: throw IllegalArgumentException("父分类不存在")
        
        // 验证子分类名称在同一父分类下不重复
        val parentTree = categoryRepository.getCategoryTree(parent.id, parent.type.name)
        val parentGroup = parentTree.find { it.parent.id == parentId }
        if (parentGroup?.children?.any { it.name == name } == true) {
            throw IllegalArgumentException("该子分类名称已存在")
        }
        
        return categoryRepository.createSubcategory(parentId, name, icon, color)
    }
    
    /**
     * 更新分类信息
     */
    suspend fun updateCategory(
        category: Category,
        newName: String? = null,
        newIcon: String? = null,
        newColor: String? = null
    ) {
        val updated = category.copy(
            name = newName ?: category.name,
            icon = newIcon ?: category.icon,
            color = newColor ?: category.color
        )
        categoryRepository.updateCategory(updated)
    }
    
    /**
     * 删除分类
     * 注意：只能删除没有交易记录的分类
     */
    suspend fun deleteCategory(categoryId: String) {
        categoryRepository.deleteCategory(categoryId)
    }
    
    /**
     * 切换分类状态（启用/禁用）
     */
    suspend fun toggleCategoryStatus(categoryId: String, isActive: Boolean) {
        categoryRepository.toggleCategoryStatus(categoryId, isActive)
    }
    
    /**
     * 获取分类的完整信息
     */
    suspend fun getCategoryFullInfo(categoryId: String): SelectedCategoryInfo? {
        return categoryRepository.getCategoryFullInfo(categoryId)
    }
    
    /**
     * 批量创建默认分类
     * 为新用户创建系统预设的分类结构
     */
    suspend fun createDefaultCategories(userId: String) = withContext(Dispatchers.IO) {
        println("🔧 [ManageCategoryUseCase.createDefaultCategories] 开始创建默认分类，用户ID: $userId")
        
        // 检查用户是否已有分类
        val expenseCategories = categoryRepository.getCategoryTree(userId, "EXPENSE")
        val incomeCategories = categoryRepository.getCategoryTree(userId, "INCOME")
        
        println("🔧 [ManageCategoryUseCase.createDefaultCategories] 再次检查分类状态:")
        println("   - 支出分类组数: ${expenseCategories.size}")
        println("   - 收入分类组数: ${incomeCategories.size}")
        
        if (expenseCategories.isNotEmpty() || incomeCategories.isNotEmpty()) {
            // 用户已有分类，不需要创建默认分类
            println("🔧 [ManageCategoryUseCase.createDefaultCategories] 用户已有分类，退出")
            return@withContext
        }
        
        println("🔧 [ManageCategoryUseCase.createDefaultCategories] 开始创建支出分类 (${DefaultCategories.expenseCategories.size} 组)")
        // 创建支出分类
        createDefaultCategoriesForType(userId, "EXPENSE", DefaultCategories.expenseCategories)
        
        println("🔧 [ManageCategoryUseCase.createDefaultCategories] 开始创建收入分类 (${DefaultCategories.incomeCategories.size} 组)")
        // 创建收入分类
        createDefaultCategoriesForType(userId, "INCOME", DefaultCategories.incomeCategories)
        
        println("🔧 [ManageCategoryUseCase.createDefaultCategories] 所有默认分类创建完成")
    }
    
    /**
     * 为指定类型创建默认分类
     */
    private suspend fun createDefaultCategoriesForType(
        userId: String,
        type: String,
        defaultGroups: List<com.ccxiaoji.feature.ledger.data.defaults.DefaultCategoryGroup>
    ) {
        println("🔧 [ManageCategoryUseCase.createDefaultCategoriesForType] 创建 $type 分类，共 ${defaultGroups.size} 组")
        
        defaultGroups.forEachIndexed { groupIndex, group ->
            println("🔧 [ManageCategoryUseCase] 创建分类组 $groupIndex: ${group.parent.name} (${group.children.size} 个子分类)")
            
            // 创建父分类
            val parentCategoryId = categoryRepository.createCategory(
                name = group.parent.name,
                type = type,
                icon = group.parent.icon,
                color = group.parent.color ?: "#6200EE",
                parentId = null
            )
            
            println("🔧 [ManageCategoryUseCase] 父分类创建成功，ID: $parentCategoryId")
            
            // 创建子分类
            group.children.forEachIndexed { childIndex, child ->
                val childId = categoryRepository.createSubcategory(
                    parentId = parentCategoryId.toString(),
                    name = child.name,
                    icon = child.icon,
                    color = child.color
                )
                println("🔧 [ManageCategoryUseCase] 子分类 $childIndex 创建成功: ${child.name} (ID: $childId)")
            }
        }
        
        println("🔧 [ManageCategoryUseCase.createDefaultCategoriesForType] $type 分类创建完成")
    }
    
    /**
     * 检查用户是否需要初始化默认分类
     */
    suspend fun checkAndInitializeDefaultCategories(userId: String, forceReinitialize: Boolean = false) {
        println("🔧 [ManageCategoryUseCase] 开始检查用户分类，用户ID: $userId, 强制重新初始化: $forceReinitialize")
        
        if (forceReinitialize) {
            println("🔧 [ManageCategoryUseCase] 强制重新初始化模式，直接调用强制重新初始化")
            forceReinitializeCategories(userId)
            return
        }
        
        val expenseCategories = categoryRepository.getCategoryTree(userId, "EXPENSE")
        val incomeCategories = categoryRepository.getCategoryTree(userId, "INCOME")
        
        println("🔧 [ManageCategoryUseCase] 当前分类状态:")
        println("   - 支出分类组数: ${expenseCategories.size}")
        println("   - 收入分类组数: ${incomeCategories.size}")
        
        if (expenseCategories.isEmpty() && incomeCategories.isEmpty()) {
            println("🔧 [ManageCategoryUseCase] 检测到无分类，开始创建默认分类")
            createDefaultCategories(userId)
            println("🔧 [ManageCategoryUseCase] 默认分类创建完成")
        } else {
            println("🔧 [ManageCategoryUseCase] 用户已有分类，跳过默认分类创建")
        }
    }
    
    /**
     * 强制重新初始化所有默认分类
     * 清除现有分类并重新创建完整的二级分类结构
     */
    suspend fun forceReinitializeCategories(userId: String) = withContext(Dispatchers.IO) {
        println("🔧 [ManageCategoryUseCase.forceReinitializeCategories] 开始强制重新初始化分类")
        
        try {
            // 调用Repository的强制重新初始化方法
            (categoryRepository as? com.ccxiaoji.feature.ledger.data.repository.CategoryRepositoryImpl)
                ?.forceReinitializeDefaultCategories()
                ?: throw IllegalStateException("Repository 不支持强制重新初始化")
            
            println("🔧 [ManageCategoryUseCase.forceReinitializeCategories] 强制重新初始化完成")
        } catch (e: Exception) {
            println("❌ [ManageCategoryUseCase.forceReinitializeCategories] 强制重新初始化失败: ${e.message}")
            throw e
        }
    }
}