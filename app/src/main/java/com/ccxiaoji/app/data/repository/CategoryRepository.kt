package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.CategoryDao
import com.ccxiaoji.app.data.local.entity.CategoryEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.app.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    private val currentUserId = "current_user_id"
    
    fun getCategories(): Flow<List<Category>> {
        return categoryDao.getCategoriesByUser(currentUserId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getCategoriesByType(type: Category.Type): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(currentUserId, type.name).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    fun getCategoriesWithUsageStats(): Flow<List<CategoryWithStats>> {
        return categoryDao.getCategoriesWithTransactionCount(currentUserId).map { list ->
            list.map { categoryWithCount ->
                CategoryWithStats(
                    category = Category(
                        id = categoryWithCount.id,
                        name = categoryWithCount.name,
                        type = Category.Type.valueOf(categoryWithCount.type),
                        icon = categoryWithCount.icon,
                        color = categoryWithCount.color,
                        parentId = categoryWithCount.parentId,
                        displayOrder = categoryWithCount.displayOrder,
                        isSystem = categoryWithCount.isSystem,
                        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(categoryWithCount.createdAt),
                        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(categoryWithCount.updatedAt)
                    ),
                    transactionCount = categoryWithCount.transactionCount,
                    usageCount = categoryWithCount.usageCount
                )
            }
        }
    }
    
    suspend fun createCategory(
        name: String,
        type: Category.Type,
        icon: String,
        color: String,
        parentId: String? = null
    ): String {
        val now = System.currentTimeMillis()
        val categoryId = UUID.randomUUID().toString()
        
        // Get current max display order
        val categories = categoryDao.getCategoriesByType(currentUserId, type.name)
            .map { it.maxByOrNull { cat -> cat.displayOrder }?.displayOrder ?: 0 }
        
        val category = CategoryEntity(
            id = categoryId,
            userId = currentUserId,
            name = name,
            type = type.name,
            icon = icon,
            color = color,
            parentId = parentId,
            displayOrder = 0, // Will be updated
            isSystem = false,
            usageCount = 0,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        
        categoryDao.insertCategory(category)
        return categoryId
    }
    
    suspend fun updateCategory(
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
            syncStatus = SyncStatus.PENDING
        )
        
        categoryDao.updateCategory(updated)
    }
    
    suspend fun deleteCategory(categoryId: String): Boolean {
        // Check if category is system category
        val category = categoryDao.getCategoryById(categoryId) ?: return false
        if (category.isSystem) return false
        
        // Check if category has transactions
        val transactionCount = categoryDao.getTransactionCountForCategory(categoryId)
        if (transactionCount > 0) return false
        
        categoryDao.deleteCategory(categoryId, System.currentTimeMillis())
        return true
    }
    
    suspend fun reorderCategories(categoryIds: List<String>) {
        val now = System.currentTimeMillis()
        categoryIds.forEachIndexed { index, categoryId ->
            categoryDao.updateCategoryOrder(categoryId, index, now)
        }
    }
    
    suspend fun getCategoryById(categoryId: String): Category? {
        return categoryDao.getCategoryById(categoryId)?.toDomainModel()
    }
    
    suspend fun initializeDefaultCategories() {
        val existingCategories = categoryDao.getCategoriesByUser(currentUserId)
        
        // Only initialize if no categories exist
        existingCategories.collect { categories ->
            if (categories.isEmpty()) {
                val now = System.currentTimeMillis()
                val defaultCategories = createDefaultCategories(now)
                categoryDao.insertCategories(defaultCategories)
            }
        }
    }
    
    private fun createDefaultCategories(timestamp: Long): List<CategoryEntity> {
        val expenseCategories = listOf(
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "餐饮",
                type = Category.Type.EXPENSE.name,
                icon = "🍔",
                color = "#FF5252",
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "交通",
                type = Category.Type.EXPENSE.name,
                icon = "🚗",
                color = "#448AFF",
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "购物",
                type = Category.Type.EXPENSE.name,
                icon = "🛍️",
                color = "#FF9800",
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "娱乐",
                type = Category.Type.EXPENSE.name,
                icon = "🎮",
                color = "#9C27B0",
                displayOrder = 3,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "医疗",
                type = Category.Type.EXPENSE.name,
                icon = "🏥",
                color = "#00BCD4",
                displayOrder = 4,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "其他",
                type = Category.Type.EXPENSE.name,
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
                userId = currentUserId,
                name = "工资",
                type = Category.Type.INCOME.name,
                icon = "💰",
                color = "#4CAF50",
                displayOrder = 0,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "奖金",
                type = Category.Type.INCOME.name,
                icon = "🎁",
                color = "#8BC34A",
                displayOrder = 1,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "投资收益",
                type = Category.Type.INCOME.name,
                icon = "📈",
                color = "#00BCD4",
                displayOrder = 2,
                isSystem = true,
                createdAt = timestamp,
                updatedAt = timestamp
            ),
            CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = currentUserId,
                name = "其他收入",
                type = Category.Type.INCOME.name,
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

private fun CategoryEntity.toDomainModel(): Category {
    return Category(
        id = id,
        name = name,
        type = Category.Type.valueOf(type),
        icon = icon,
        color = color,
        parentId = parentId,
        displayOrder = displayOrder,
        isSystem = isSystem,
        createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt),
        updatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(updatedAt)
    )
}

data class CategoryWithStats(
    val category: Category,
    val transactionCount: Int,
    val usageCount: Long
)