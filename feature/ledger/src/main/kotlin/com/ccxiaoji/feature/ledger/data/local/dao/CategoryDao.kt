package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0 AND isHidden = 0 ORDER BY type, displayOrder, name")
    fun getCategoriesByUser(userId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0 AND isHidden = 0 ORDER BY type, displayOrder, name")
    suspend fun getCategoriesByUserSync(userId: String): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type AND isDeleted = 0 AND isHidden = 0 ORDER BY displayOrder, name")
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = 'EXPENSE' AND isDeleted = 0 AND isHidden = 0 ORDER BY displayOrder, name")
    fun getExpenseCategories(userId: String): Flow<List<CategoryEntity>>
    
    @Query("SELECT * FROM categories WHERE id = :categoryId AND isDeleted = 0")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND parentId = :parentId AND isDeleted = 0 AND isHidden = 0 ORDER BY displayOrder, name")
    suspend fun getSubcategories(userId: String, parentId: String): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND level = 1 AND type = :type AND isDeleted = 0 AND isActive = 1 AND isHidden = 0 ORDER BY displayOrder, name")
    suspend fun getParentCategories(userId: String, type: String): List<CategoryEntity>

    @Query("SELECT MAX(displayOrder) FROM categories WHERE userId = :userId AND type = :type AND level = 1 AND isDeleted = 0")
    suspend fun getMaxParentDisplayOrder(userId: String, type: String): Int?
    
    @Query("SELECT * FROM categories WHERE userId = :userId AND level = 2 AND type = :type AND isDeleted = 0 AND isActive = 1 AND isHidden = 0 ORDER BY displayOrder, name")
    suspend fun getLeafCategories(userId: String, type: String): List<CategoryEntity>
    
    @Query("SELECT * FROM categories WHERE parentId = :parentId AND isDeleted = 0 AND isActive = 1 AND isHidden = 0 ORDER BY displayOrder, name")
    suspend fun getChildCategories(parentId: String): List<CategoryEntity>
    
    @Query("""SELECT * FROM categories 
        WHERE userId = :userId AND type = :type AND isDeleted = 0 AND isActive = 1 AND isHidden = 0 
        ORDER BY level, parentId, displayOrder, name""")
    suspend fun getCategoriesByTypeWithLevels(userId: String, type: String): List<CategoryEntity>
    
    @Query("SELECT path FROM categories WHERE id = :categoryId AND isDeleted = 0")
    suspend fun getCategoryPath(categoryId: String): String?
    
    @Query("UPDATE categories SET isActive = :isActive, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun updateCategoryStatus(categoryId: String, isActive: Boolean, timestamp: Long)
    
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
    
    @Query("""SELECT c.*, p.name as parentName, p.icon as parentIcon, p.color as parentColor 
        FROM categories c 
        LEFT JOIN categories p ON c.parentId = p.id 
        WHERE c.id = :categoryId AND c.isDeleted = 0""")
    suspend fun getCategoryWithParent(categoryId: String): CategoryWithParent?
    
    @Query("SELECT * FROM categories WHERE name = :name AND type = :type AND userId = :userId AND isDeleted = 0 LIMIT 1")
    suspend fun findByNameAndType(name: String, type: String, userId: String): CategoryEntity?
    
    @Query("SELECT * FROM categories WHERE name = :name AND parentId = :parentId AND userId = :userId AND isDeleted = 0 LIMIT 1")
    suspend fun findByNameAndParent(name: String, parentId: String, userId: String): CategoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)
    
    @Query("SELECT COUNT(*) > 0 FROM transactions WHERE userId = :userId AND isDeleted = 0")
    suspend fun hasAnyTransactions(userId: String): Boolean
    
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllCategoriesForUser(userId: String)
}

data class CategoryWithCount(
    val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String?,
    val level: Int,
    val path: String,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean,
    val isSystem: Boolean,
    val usageCount: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val transactionCount: Int
)

data class CategoryWithParent(
    val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String?,
    val level: Int,
    val path: String,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean,
    val isSystem: Boolean,
    val usageCount: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val parentName: String?,
    val parentIcon: String?,
    val parentColor: String?
)
