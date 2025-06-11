package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.core.database.dao.CategoryDao
import com.ccxiaoji.core.database.entity.CategoryEntity
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分类管理Repository
 * 从app模块迁移到feature-ledger模块
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val userApi: UserApi
) {
    
    /**
     * 获取当前用户ID
     */
    private suspend fun getCurrentUserId(): String = userApi.getCurrentUserId()
    
    /**
     * 获取所有分类
     */
    suspend fun getAllCategories(): List<CategoryItem> {
        val categories = categoryDao.getCategoriesByUser(getCurrentUserId()).first()
        return categories.map { it.toCategoryItem() }
    }
    
    /**
     * 根据类型获取分类
     */
    suspend fun getCategoriesByType(type: String): List<CategoryItem> {
        val categories = categoryDao.getCategoriesByType(getCurrentUserId(), type).first()
        return categories.map { it.toCategoryItem() }
    }
    
    /**
     * 根据ID获取分类
     */
    suspend fun getCategoryById(categoryId: String): CategoryEntity? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    /**
     * 获取分类的使用次数
     */
    suspend fun getCategoryUsageCount(categoryId: String): Int {
        return categoryDao.getTransactionCountForCategory(categoryId)
    }
    
    /**
     * 创建分类
     */
    suspend fun createCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String? = null
    ): String {
        val now = System.currentTimeMillis()
        val categoryId = UUID.randomUUID().toString()
        
        // 获取当前类型中最大的显示顺序
        val maxOrder = categoryDao.getCategoriesByType(getCurrentUserId(), type).first()
            .maxByOrNull { it.displayOrder }?.displayOrder ?: -1
        
        val category = CategoryEntity(
            id = categoryId,
            userId = getCurrentUserId(),
            name = name,
            type = type,
            icon = icon,
            color = color,
            parentId = parentId,
            displayOrder = maxOrder + 1,
            isSystem = false,
            usageCount = 0,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        categoryDao.insertCategory(category)
        return categoryId
    }
    
    /**
     * 更新分类
     */
    suspend fun updateCategory(
        categoryId: String,
        name: String,
        icon: String,
        color: String
    ) {
        val existing = categoryDao.getCategoryById(categoryId) ?: return
        
        val updated = existing.copy(
            name = name,
            icon = icon,
            color = color,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        
        categoryDao.updateCategory(updated)
    }
    
    /**
     * 删除分类
     * @return true if deleted successfully, false if category is system or has transactions
     */
    suspend fun deleteCategory(categoryId: String): Boolean {
        // 检查是否为系统分类
        val category = categoryDao.getCategoryById(categoryId) ?: return false
        if (category.isSystem) return false
        
        // 检查是否有交易记录
        val transactionCount = categoryDao.getTransactionCountForCategory(categoryId)
        if (transactionCount > 0) return false
        
        categoryDao.deleteCategory(categoryId, System.currentTimeMillis())
        return true
    }
    
    /**
     * 重新排序分类
     */
    suspend fun reorderCategories(categoryIds: List<String>) {
        val now = System.currentTimeMillis()
        categoryIds.forEachIndexed { index, categoryId ->
            categoryDao.updateCategoryOrder(categoryId, index, now)
        }
    }
    
    /**
     * 初始化默认分类
     */
    suspend fun initializeDefaultCategories() {
        val existingCategories = categoryDao.getCategoriesByUser(getCurrentUserId()).first()
        
        // 只有在没有分类时才初始化
        if (existingCategories.isEmpty()) {
            val now = System.currentTimeMillis()
            val defaultCategories = createDefaultCategories(now)
            categoryDao.insertCategories(defaultCategories)
        }
    }
    
    /**
     * 创建默认分类列表
     */
    private suspend fun createDefaultCategories(timestamp: Long): List<CategoryEntity> {
        val userId = getCurrentUserId()
        
        val expenseCategories = listOf(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "餐饮",
                type = "EXPENSE",
                icon = "🍔",
                color = "#FF5252",
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "交通",
                type = "EXPENSE",
                icon = "🚗",
                color = "#448AFF",
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "购物",
                type = "EXPENSE",
                icon = "🛍️",
                color = "#FF9800",
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "娱乐",
                type = "EXPENSE",
                icon = "🎮",
                color = "#9C27B0",
                displayOrder = 3,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "医疗",
                type = "EXPENSE",
                icon = "🏥",
                color = "#00BCD4",
                displayOrder = 4,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "其他",
                type = "EXPENSE",
                icon = "📝",
                color = "#607D8B",
                displayOrder = 5,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        
        val incomeCategories = listOf(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "工资",
                type = "INCOME",
                icon = "💰",
                color = "#4CAF50",
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "奖金",
                type = "INCOME",
                icon = "🎁",
                color = "#8BC34A",
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "投资收益",
                type = "INCOME",
                icon = "📈",
                color = "#00BCD4",
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = "其他收入",
                type = "INCOME",
                icon = "💵",
                color = "#009688",
                displayOrder = 3,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )
        
        return expenseCategories + incomeCategories
    }
}

/**
 * 将数据库实体转换为API模型
 */
private suspend fun CategoryEntity.toCategoryItem(): CategoryItem {
    return CategoryItem(
        id = id,
        name = name,
        type = type,
        icon = icon,
        color = color,
        parentId = parentId,
        isSystem = isSystem,
        usageCount = usageCount.toInt()
    )
}