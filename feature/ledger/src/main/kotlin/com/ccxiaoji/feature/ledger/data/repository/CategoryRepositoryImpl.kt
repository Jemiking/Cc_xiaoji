package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryGroup
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.domain.model.SelectedCategoryInfo
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.defaults.DefaultCategories
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val userApi: UserApi
) : CategoryRepository {
    
    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategoriesByUser(userApi.getCurrentUserId()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getIncomeCategories(): Flow<List<Category>> {
        return getCategoriesByType(Category.Type.INCOME)
    }
    
    override fun getExpenseCategories(): Flow<List<Category>> {
        return getCategoriesByType(Category.Type.EXPENSE)
    }
    
    override fun getCategoriesByType(type: Category.Type): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(userApi.getCurrentUserId(), type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getCategoriesWithUsageStats(): Flow<List<CategoryWithStats>> {
        return categoryDao.getCategoriesWithTransactionCount(userApi.getCurrentUserId()).map { list ->
            list.map { categoryWithCount ->
                CategoryWithStats(
                    category = Category(
                        id = categoryWithCount.id,
                        name = categoryWithCount.name,
                        type = Category.Type.valueOf(categoryWithCount.type),
                        icon = categoryWithCount.icon,
                        color = categoryWithCount.color,
                        parentId = categoryWithCount.parentId,
                        level = categoryWithCount.level,
                        path = categoryWithCount.path,
                        displayOrder = categoryWithCount.displayOrder,
                        isDefault = categoryWithCount.isDefault,
                        isActive = categoryWithCount.isActive,
                        isSystem = categoryWithCount.isSystem,
                        isHidden = false,
                        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(categoryWithCount.createdAt),
                        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(categoryWithCount.updatedAt)
                    ),
                    transactionCount = categoryWithCount.transactionCount,
                    usageCount = categoryWithCount.usageCount
                )
            }
        }
    }
    
    override suspend fun createCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String?
    ): Long {
        println("🔧 [CategoryRepositoryImpl.createCategory] 创建分类: $name ($type), 父ID: $parentId")
        
        val now = System.currentTimeMillis()
        val categoryId = UUID.randomUUID().toString()
        
        // 确定分类层级和路径
        val level = if (parentId == null) 1 else 2
        val path = if (parentId == null) {
            name
        } else {
            val parent = categoryDao.getCategoryById(parentId)
            if (parent != null) "${parent.name}/$name" else name
        }
        // 计算显示顺序：父分类使用当前最大顺序 + 1；子分类顺序在 createSubcategory 中处理
        val displayOrder = if (parentId == null) {
            val maxOrder = categoryDao.getMaxParentDisplayOrder(userApi.getCurrentUserId(), type) ?: -1
            maxOrder + 1
        } else 0
        
        val category = CategoryEntity(
            id = categoryId,
            userId = userApi.getCurrentUserId(),
            name = name,
            type = type,
            icon = icon,
            color = color,
            parentId = parentId,
            level = level,
            path = path,
            displayOrder = displayOrder,
            isDefault = false,
            isActive = true,
            isSystem = false,
            usageCount = 0,
            createdAt = now,
            updatedAt = now,
            isDeleted = false,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        categoryDao.insertCategory(category)
        println("🔧 [CategoryRepositoryImpl.createCategory] 分类创建成功: $name (ID: $categoryId, 层级: $level, 路径: $path)")
        return 1L // TODO: 返回实际的ID
    }
    
    override suspend fun updateCategory(category: Category) {
        val entity = categoryDao.getCategoryById(category.id)
        if (entity != null) {
            // 更新路径（如果父分类改变了）
            val newPath = if (category.parentId == null) {
                category.name
            } else {
                val parent = categoryDao.getCategoryById(category.parentId)
                if (parent != null) "${parent.name}/${category.name}" else category.name
            }
            
            val updatedEntity = entity.copy(
                name = category.name,
                icon = category.icon,
                color = category.color,
                parentId = category.parentId,
                level = category.level,
                path = newPath,
                isActive = category.isActive,
                isSystem = category.isSystem,
                isHidden = category.isHidden,
                updatedAt = Clock.System.now().toEpochMilliseconds(),
                syncStatus = SyncStatus.PENDING_SYNC
            )
            categoryDao.updateCategory(updatedEntity)
            // TODO: Log change for sync
        }
    }

    suspend fun updateCategoryDetailed(
        categoryId: String,
        name: String? = null,
        icon: String? = null,
        color: String? = null
    ) {
        val existing = categoryDao.getCategoryById(categoryId) ?: return
        
        val updated = existing.copy(
            name = name ?: existing.name,
            icon = icon ?: existing.icon,
            color = color ?: existing.color,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        categoryDao.updateCategory(updated)
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        // Check if category is system category
        val category = categoryDao.getCategoryById(categoryId) ?: throw IllegalArgumentException("分类不存在")
        if (category.isSystem) throw IllegalStateException("系统分类无法删除")
        
        // Check if category has transactions
        val transactionCount = categoryDao.getTransactionCountForCategory(categoryId)
        if (transactionCount > 0) throw IllegalStateException("分类正在被使用，无法删除")
        
        categoryDao.deleteCategory(categoryId, System.currentTimeMillis())
    }
    
    suspend fun reorderCategories(categoryIds: List<String>) {
        val now = System.currentTimeMillis()
        categoryIds.forEachIndexed { index, categoryId ->
            categoryDao.updateCategoryOrder(categoryId, index, now)
        }
    }
    
    override suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toDomainModel()
    }
    
    suspend fun initializeDefaultCategories() {
        val existingCategories = categoryDao.getCategoriesByUser(userApi.getCurrentUserId())
        
        // Only initialize if no categories exist
        existingCategories.collect { categories ->
            if (categories.isEmpty()) {
                val now = System.currentTimeMillis()
                val defaultCategories = createDefaultCategories(now)
                categoryDao.insertCategories(defaultCategories)
            }
        }
    }
    
    /**
     * 强制重新初始化默认分类
     * 清除所有现有分类并重新创建完整的二级分类结构
     */
    suspend fun forceReinitializeDefaultCategories() {
        println("🔧 [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 开始强制重新初始化")
        
        val userId = userApi.getCurrentUserId()
        
        // 检查是否有交易记录依赖分类
        val hasTransactions = categoryDao.hasAnyTransactions(userId)
        if (hasTransactions) {
            println("❌ [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 检测到现有交易记录，无法清除分类")
            throw IllegalStateException("存在交易记录，无法清除分类。请先处理相关交易记录。")
        }
        
        println("🔧 [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 开始清除现有分类")
        
        // 清除所有现有分类
        categoryDao.deleteAllCategoriesForUser(userId)
        
        println("🔧 [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 现有分类已清除，开始创建新分类")
        
        // 重新创建完整的二级分类
        val now = System.currentTimeMillis()
        val defaultCategories = createDefaultCategories(now)
        
        println("🔧 [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 准备插入 ${defaultCategories.size} 个分类")
        
        categoryDao.insertCategories(defaultCategories)
        
        println("🔧 [CategoryRepositoryImpl.forceReinitializeDefaultCategories] 强制重新初始化完成")
    }
    
    private fun createDefaultCategories(timestamp: Long): List<CategoryEntity> {
        val currentUserId = userApi.getCurrentUserId()
        val allCategories = mutableListOf<CategoryEntity>()
        
        // 创建支出分类及其子分类
        DefaultCategories.expenseCategories.forEachIndexed { groupIndex, categoryGroup ->
            // 创建父分类
            val parentId = UUID.randomUUID().toString()
            val parentEntity = CategoryEntity(
                id = parentId,
                userId = currentUserId,
                name = categoryGroup.parent.name,
                type = Category.Type.EXPENSE.name,
                icon = categoryGroup.parent.icon,
                color = categoryGroup.parent.color ?: "#6200EE",
                level = 1,
                path = categoryGroup.parent.name,
                displayOrder = groupIndex,
                isDefault = true,
                isActive = true,
                isSystem = true,
                isHidden = false,
                usageCount = 0,
                createdAt = timestamp,
                updatedAt = timestamp,
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            allCategories.add(parentEntity)
            
            // 创建子分类
            categoryGroup.children.forEachIndexed { childIndex, child ->
                val childEntity = CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    name = child.name,
                    type = Category.Type.EXPENSE.name,
                    icon = child.icon,
                    color = child.color ?: categoryGroup.parent.color ?: "#6200EE",
                    parentId = parentId,
                    level = 2,
                    path = "${categoryGroup.parent.name}/${child.name}",
                    displayOrder = childIndex,
                    isDefault = true,
                    isActive = true,
                    isSystem = true,
                    isHidden = false,
                    usageCount = 0,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    isDeleted = false,
                    syncStatus = SyncStatus.SYNCED
                )
                allCategories.add(childEntity)
            }
        }
        
        // 创建收入分类及其子分类
        DefaultCategories.incomeCategories.forEachIndexed { groupIndex, categoryGroup ->
            // 创建父分类
            val parentId = UUID.randomUUID().toString()
            val parentEntity = CategoryEntity(
                id = parentId,
                userId = currentUserId,
                name = categoryGroup.parent.name,
                type = Category.Type.INCOME.name,
                icon = categoryGroup.parent.icon,
                color = categoryGroup.parent.color ?: "#4CAF50",
                level = 1,
                path = categoryGroup.parent.name,
                displayOrder = groupIndex,
                isDefault = true,
                isActive = true,
                isSystem = true,
                isHidden = false,
                usageCount = 0,
                createdAt = timestamp,
                updatedAt = timestamp,
                isDeleted = false,
                syncStatus = SyncStatus.SYNCED
            )
            allCategories.add(parentEntity)
            
            // 创建子分类
            categoryGroup.children.forEachIndexed { childIndex, child ->
                val childEntity = CategoryEntity(
                    id = UUID.randomUUID().toString(),
                    userId = currentUserId,
                    name = child.name,
                    type = Category.Type.INCOME.name,
                    icon = child.icon,
                    color = child.color ?: categoryGroup.parent.color ?: "#4CAF50",
                    parentId = parentId,
                    level = 2,
                    path = "${categoryGroup.parent.name}/${child.name}",
                    displayOrder = childIndex,
                    isDefault = true,
                    isActive = true,
                    isSystem = true,
                    isHidden = false,
                    usageCount = 0,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                    isDeleted = false,
                    syncStatus = SyncStatus.SYNCED
                )
                allCategories.add(childEntity)
            }
        }
        
        return allCategories
    }
    
    // ========== 二级分类支持实现 ==========
    
    override suspend fun getCategoryTree(userId: String, type: String): List<CategoryGroup> {
        println("🔧 [CategoryRepositoryImpl.getCategoryTree] 查询分类树，用户ID: $userId, 类型: $type")
        
        // 一次查询获取所有分类
        val allCategories = categoryDao.getCategoriesByTypeWithLevels(userId, type)
        
        println("🔧 [CategoryRepositoryImpl.getCategoryTree] 查询到总分类数: ${allCategories.size}")
        allCategories.forEach { category ->
            println("   - ${category.name} (层级: ${category.level}, 父ID: ${category.parentId}, 路径: ${category.path})")
        }
        
        // 在内存中构建树结构
        val parentCategories = allCategories.filter { it.level == 1 }
        println("🔧 [CategoryRepositoryImpl.getCategoryTree] 父分类数: ${parentCategories.size}")
        
        val result = parentCategories.map { parent ->
            val children = allCategories
                .filter { it.parentId == parent.id && it.level == 2 }
                .map { it.toDomainModel() }
                .sortedBy { it.displayOrder }
            
            println("🔧 [CategoryRepositoryImpl.getCategoryTree] 父分类 ${parent.name} 有 ${children.size} 个子分类")
            
            CategoryGroup(
                parent = parent.toDomainModel(),
                children = children
            )
        }.sortedBy { it.parent.displayOrder }
        
        println("🔧 [CategoryRepositoryImpl.getCategoryTree] 返回分类组数: ${result.size}")
        return result
    }
    
    override suspend fun getLeafCategories(userId: String, type: String): List<Category> {
        return categoryDao.getLeafCategories(userId, type)
            .map { it.toDomainModel() }
    }
    
    override suspend fun getParentCategories(userId: String, type: String): List<Category> {
        return categoryDao.getParentCategories(userId, type)
            .map { it.toDomainModel() }
    }
    
    override suspend fun createCategoryTree(groups: List<CategoryGroup>) {
        val timestamp = System.currentTimeMillis()
        val userId = userApi.getCurrentUserId()
        val allEntities = mutableListOf<CategoryEntity>()
        
        groups.forEach { group ->
            // 添加父分类
            val parentEntity = group.parent.toEntity(userId).copy(
                createdAt = timestamp,
                updatedAt = timestamp
            )
            allEntities.add(parentEntity)
            
            // 添加子分类
            group.children.forEach { child ->
                val childEntity = child.toEntity(userId).copy(
                    parentId = parentEntity.id,
                    level = 2,
                    path = "${group.parent.name}/${child.name}",
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
                allEntities.add(childEntity)
            }
        }
        
        categoryDao.insertCategories(allEntities)
    }
    
    override suspend fun getCategoryPath(categoryId: String): String? {
        return categoryDao.getCategoryPath(categoryId)
    }
    
    override suspend fun getCategoryFullInfo(categoryId: String): SelectedCategoryInfo? {
        val categoryWithParent = categoryDao.getCategoryWithParent(categoryId) ?: return null
        
        return SelectedCategoryInfo(
            categoryId = categoryWithParent.id,
            categoryName = categoryWithParent.name,
            parentId = categoryWithParent.parentId,
            parentName = categoryWithParent.parentName,
            fullPath = categoryWithParent.path,
            icon = categoryWithParent.icon,
            color = categoryWithParent.color
        )
    }
    
    override suspend fun toggleCategoryStatus(categoryId: String, isActive: Boolean) {
        categoryDao.updateCategoryStatus(categoryId, isActive, System.currentTimeMillis())
    }
    
    override suspend fun createSubcategory(
        parentId: String,
        name: String,
        icon: String,
        color: String?
    ): String {
        println("🔧 [CategoryRepositoryImpl.createSubcategory] 创建子分类: $name, 父ID: $parentId")
        
        val parent = categoryDao.getCategoryById(parentId) 
            ?: throw IllegalArgumentException("父分类不存在")
        
        println("🔧 [CategoryRepositoryImpl.createSubcategory] 找到父分类: ${parent.name}")
        
        val categoryId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        // 获取同级子分类的最大顺序
        val siblings = categoryDao.getChildCategories(parentId)
        val maxOrder = siblings.maxByOrNull { it.displayOrder }?.displayOrder ?: -1
        
        println("🔧 [CategoryRepositoryImpl.createSubcategory] 当前同级子分类数: ${siblings.size}, 新顺序: ${maxOrder + 1}")
        
        val subcategory = CategoryEntity(
            id = categoryId,
            userId = userApi.getCurrentUserId(),
            name = name,
            type = parent.type,
            icon = icon,
            color = color ?: parent.color, // 默认继承父分类颜色
            parentId = parentId,
            level = 2,
            path = "${parent.name}/$name",
            displayOrder = maxOrder + 1,
            isDefault = false,
            isActive = true,
            isSystem = false,
            isHidden = false,
            usageCount = 0,
            createdAt = timestamp,
            updatedAt = timestamp,
            isDeleted = false,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        categoryDao.insertCategory(subcategory)
        println("🔧 [CategoryRepositoryImpl.createSubcategory] 子分类创建成功: $name (ID: $categoryId, 路径: ${parent.name}/$name)")
        return categoryId
    }
    
    override suspend fun getFrequentCategories(
        userId: String,
        type: String,
        limit: Int
    ): List<Category> {
        // 获取所有二级分类并按使用频率排序
        return categoryDao.getLeafCategories(userId, type)
            .filter { it.isActive }
            .sortedByDescending { it.usageCount }
            .take(limit)
            .map { it.toDomainModel() }
    }
    
    override suspend fun incrementCategoryUsage(categoryId: String) {
        categoryDao.incrementUsageCount(categoryId)
    }
    
    // ========== 默认分类支持实现 ==========
    
    override suspend fun findCategoryByName(name: String): Category? {
        println("🔧 [CategoryRepositoryImpl.findCategoryByName] 查找分类: $name")
        
        return try {
            val categories = categoryDao.getCategoriesByUser(userApi.getCurrentUserId()).first()
            val entity = categories.find { it.name == name && it.isActive }
            
            if (entity != null) {
                println("🔧 [CategoryRepositoryImpl.findCategoryByName] 找到分类: ${entity.name} (ID: ${entity.id})")
                entity.toDomainModel()
            } else {
                println("🔧 [CategoryRepositoryImpl.findCategoryByName] 未找到分类: $name")
                null
            }
        } catch (e: Exception) {
            println("❌ [CategoryRepositoryImpl.findCategoryByName] 查找分类异常: ${e.message}")
            null
        }
    }
    
    override suspend fun getOrCreateDefaultOtherCategory(userId: String): String {
        println("🔧 [CategoryRepositoryImpl.getOrCreateDefaultOtherCategory] 获取或创建默认'其他'分类")
        
        // 1. 先尝试查找现有的"其他"分类
        val categories = categoryDao.getCategoriesByUser(userId).first()
        val existingOther = categories.find { it.name == "其他" && it.isActive }
            
        if (existingOther != null) {
            println("🔧 [CategoryRepositoryImpl.getOrCreateDefaultOtherCategory] 使用现有'其他'分类: ${existingOther.id}")
            return existingOther.id
        }
        
        // 2. 创建新的"其他"分类
        println("🔧 [CategoryRepositoryImpl.getOrCreateDefaultOtherCategory] 创建新的'其他'分类")
        
        val categoryId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        
        val otherCategory = CategoryEntity(
            id = categoryId,
            userId = userId,
            name = "其他",
            type = Category.Type.EXPENSE.name, // 默认为支出分类
            icon = "help_outline",
            color = "#9E9E9E", // 灰色
            parentId = null,
            level = 1,
            path = "其他",
            displayOrder = 999, // 排在最后
            isDefault = true,
            isActive = true,
            isSystem = true, // 标记为系统分类
            isHidden = false,
            usageCount = 0,
            createdAt = timestamp,
            updatedAt = timestamp,
            isDeleted = false,
            syncStatus = SyncStatus.SYNCED
        )
        
        categoryDao.insertCategory(otherCategory)
        println("🔧 [CategoryRepositoryImpl.getOrCreateDefaultOtherCategory] 创建'其他'分类成功: $categoryId")
        
        return categoryId
    }
}

private fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = Category.Type.valueOf(type),
        icon = icon,
        color = color,
        parentId = parentId,
        level = level,
        path = path,
        displayOrder = displayOrder,
        isDefault = isDefault,
        isActive = isActive,
        isSystem = isSystem,
        isHidden = isHidden,
        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt),
        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt)
    )
}

private fun Category.toEntity(userId: String): CategoryEntity {
    return CategoryEntity(
        id = id,
        userId = userId,
        name = name,
        type = type.name,
        icon = icon,
        color = color,
        parentId = parentId,
        level = level,
        path = path,
        displayOrder = displayOrder,
        isDefault = isDefault,
        isActive = isActive,
        isSystem = isSystem,
        isHidden = isHidden,
        usageCount = 0,
        createdAt = createdAt.toEpochMilliseconds(),
        updatedAt = updatedAt.toEpochMilliseconds(),
        isDeleted = false,
        syncStatus = SyncStatus.PENDING_SYNC
    )
}
