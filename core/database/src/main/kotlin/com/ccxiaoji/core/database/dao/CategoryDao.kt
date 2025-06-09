package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0 ORDER BY type, displayOrder, name")
    fun getCategoriesByUser(userId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type AND isDeleted = 0 ORDER BY displayOrder, name")
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = 'EXPENSE' AND isDeleted = 0 ORDER BY displayOrder, name")
    fun getExpenseCategories(userId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId AND isDeleted = 0")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND parentId = :parentId AND isDeleted = 0 ORDER BY displayOrder, name")
    suspend fun getSubcategories(userId: String, parentId: String): List<CategoryEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    @Update
    suspend fun updateCategory(category: CategoryEntity)
    
    @Query("UPDATE categories SET isDeleted = 1, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun deleteCategory(categoryId: String, timestamp: Long)
    
    @Query("UPDATE categories SET displayOrder = :order, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun updateCategoryOrder(categoryId: String, order: Int, timestamp: Long)
    
    @Query("UPDATE categories SET usageCount = usageCount + 1 WHERE id = :categoryId")
    suspend fun incrementUsageCount(categoryId: String)
    
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId AND isDeleted = 0")
    suspend fun getTransactionCountForCategory(categoryId: String): Int
    
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("""
        SELECT c.*, COUNT(t.id) as transactionCount 
        FROM categories c 
        LEFT JOIN transactions t ON c.id = t.categoryId AND t.isDeleted = 0
        WHERE c.userId = :userId AND c.isDeleted = 0
        GROUP BY c.id
        ORDER BY c.type, c.displayOrder, c.name
    """)
    fun getCategoriesWithTransactionCount(userId: String): Flow<List<CategoryWithCount>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId AND isDeleted = 0")
    fun getCategoryByIdSync(categoryId: String): CategoryEntity?
}

data class CategoryWithCount(
    val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String?,
    val displayOrder: Int,
    val isSystem: Boolean,
    val usageCount: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val transactionCount: Int
)