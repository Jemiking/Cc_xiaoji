package com.ccxiaoji.app.data.local.dao

import androidx.room.*
import com.ccxiaoji.app.data.local.entity.TransactionEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND category = :category AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByCategory(userId: String, category: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT SUM(amountCents) FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0")
    suspend fun getTotalAmountByDateRange(userId: String, startTime: Long, endTime: Long): Int?
    
    @Query("SELECT category, SUM(amountCents) as total FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0 GROUP BY category")
    suspend fun getCategoryTotalsByDateRange(userId: String, startTime: Long, endTime: Long): List<CategoryTotal>
    
    @Query("SELECT * FROM transactions WHERE syncStatus != :syncStatus AND isDeleted = 0")
    suspend fun getUnsyncedTransactions(syncStatus: SyncStatus = SyncStatus.SYNCED): List<TransactionEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :transactionId")
    suspend fun softDeleteTransaction(transactionId: String, timestamp: Long, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
    
    @Query("UPDATE transactions SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, syncStatus: SyncStatus)
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND isDeleted = 0 
        AND (note LIKE '%' || :query || '%' 
            OR category LIKE '%' || :query || '%'
            OR CAST(amountCents AS TEXT) LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchTransactions(userId: String, query: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getTransactionsByDateRangeSync(userId: String, startTime: Long, endTime: Long): List<TransactionEntity>
    
    @Query("""
        SELECT c.id as categoryId, c.name as categoryName, c.icon as categoryIcon, 
               c.color as categoryColor, SUM(t.amountCents) as totalAmount, COUNT(t.id) as transactionCount
        FROM transactions t
        JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId 
        AND t.createdAt >= :startTime 
        AND t.createdAt < :endTime 
        AND t.isDeleted = 0
        AND c.type = :type
        GROUP BY c.id
        ORDER BY totalAmount DESC
    """)
    suspend fun getCategoryStatisticsByType(userId: String, startTime: Long, endTime: Long, type: String): List<CategoryStatistic>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND createdAt >= :startTime 
        AND createdAt < :endTime 
        AND isDeleted = 0
        AND categoryId IN (SELECT id FROM categories WHERE type = :type)
        ORDER BY amountCents DESC
        LIMIT :limit
    """)
    suspend fun getTopTransactionsByType(userId: String, startTime: Long, endTime: Long, type: String, limit: Int): List<TransactionEntity>
    
    @Query("""
        SELECT SUM(amountCents) FROM transactions t
        JOIN categories c ON t.categoryId = c.id
        WHERE t.userId = :userId 
        AND t.createdAt >= :startTime 
        AND t.createdAt < :endTime 
        AND t.isDeleted = 0
        AND c.type = :type
    """)
    suspend fun getTotalByType(userId: String, startTime: Long, endTime: Long, type: String): Int?
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId AND isDeleted = 0")
    fun getTransactionByIdSync(transactionId: String): TransactionEntity?
}

data class CategoryTotal(
    val category: String,
    val total: Int
)

data class CategoryStatistic(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Int,
    val transactionCount: Int
)