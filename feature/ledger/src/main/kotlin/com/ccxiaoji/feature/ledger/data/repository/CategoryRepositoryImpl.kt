package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.CategoryDao
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryWithStats
import com.ccxiaoji.feature.ledger.domain.repository.CategoryRepository
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
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
    
    override suspend fun createCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String?
    ): Long {
        val now = System.currentTimeMillis()
        val categoryId = UUID.randomUUID().toString()
        
        // Get current max display order
        val categories = categoryDao.getCategoriesByType(userApi.getCurrentUserId(), type)
            .map { it.maxByOrNull { cat -> cat.displayOrder }?.displayOrder ?: 0 }
        
        val category = CategoryEntity(
            id = categoryId,
            userId = userApi.getCurrentUserId(),
            name = name,
            type = type,
            icon = icon,
            color = color,
            parentId = parentId,
            displayOrder = 0, // Will be updated
            isSystem = false,
            usageCount = 0,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        categoryDao.insertCategory(category)
        return 1L // TODO: 返回实际的ID
    }
    
    override suspend fun updateCategory(category: Category) {
        val entity = categoryDao.getCategoryById(category.id)
        if (entity != null) {
            val updatedEntity = entity.copy(
                name = category.name,
                icon = category.icon,
                color = category.color,
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
    
    private fun createDefaultCategories(timestamp: Long): List<CategoryEntity> {
        val currentUserId = userApi.getCurrentUserId()
        
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