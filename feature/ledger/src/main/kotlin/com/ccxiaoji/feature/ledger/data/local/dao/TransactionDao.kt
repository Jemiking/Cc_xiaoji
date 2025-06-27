package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity
import com.ccxiaoji.common.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByUser(userId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByDateRange(userId: String, startTime: Long, endTime: Long): Flow<List<TransactionEntity>>
    
    
    @Query("SELECT SUM(amountCents) FROM transactions WHERE userId = :userId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0")
    suspend fun getTotalAmountByDateRange(userId: String, startTime: Long, endTime: Long): Int?
    
    
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
    
    @Query("SELECT * FROM transactions WHERE id = :transactionId AND isDeleted = 0")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND accountId = :accountId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByAccount(userId: String, accountId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND accountId = :accountId AND createdAt >= :startTime AND createdAt < :endTime AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getTransactionsByAccountAndDateRange(userId: String, accountId: String, startTime: Long, endTime: Long): Flow<List<TransactionEntity>>
    
    // 查询账单周期内的交易
    @Query("""
        SELECT * FROM transactions 
        WHERE accountId = :accountId 
        AND createdAt >= :startDate 
        AND createdAt <= :endDate 
        AND isDeleted = 0 
        ORDER BY createdAt DESC
    """)
    suspend fun getTransactionsByBillingCycle(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): List<TransactionEntity>
    
    // 统计账单周期内的消费总额
    @Query("""
        SELECT SUM(t.amountCents) FROM transactions t
        JOIN categories c ON t.categoryId = c.id
        WHERE t.accountId = :accountId 
        AND c.type = 'EXPENSE'
        AND t.createdAt >= :startDate 
        AND t.createdAt <= :endDate 
        AND t.isDeleted = 0
    """)
    suspend fun getTotalExpenseInBillingCycle(
        accountId: String,
        startDate: Long,
        endDate: Long
    ): Long?
    
    // 分页查询交易记录
    @Transaction
    suspend fun getTransactionsPaginated(
        userId: String,
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDateMillis: Long? = null,
        endDateMillis: Long? = null
    ): Pair<List<TransactionEntity>, Int> {
        val queryBuilder = StringBuilder("""
            SELECT * FROM transactions 
            WHERE userId = :userId AND isDeleted = 0
        """)
        
        if (accountId != null) queryBuilder.append(" AND accountId = :accountId")
        if (startDateMillis != null) queryBuilder.append(" AND createdAt >= :startDateMillis")
        if (endDateMillis != null) queryBuilder.append(" AND createdAt <= :endDateMillis")
        
        queryBuilder.append(" ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
        
        // 获取数据
        val transactions = getTransactionsPaginatedData(
            userId, offset, limit, accountId, startDateMillis, endDateMillis
        )
        
        // 获取总数
        val totalCount = getTransactionsPaginatedCount(
            userId, accountId, startDateMillis, endDateMillis
        )
        
        return Pair(transactions, totalCount)
    }
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND isDeleted = 0
        AND (:accountId IS NULL OR accountId = :accountId)
        AND (:startDateMillis IS NULL OR createdAt >= :startDateMillis)
        AND (:endDateMillis IS NULL OR createdAt <= :endDateMillis)
        ORDER BY createdAt DESC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTransactionsPaginatedData(
        userId: String,
        offset: Int,
        limit: Int,
        accountId: String?,
        startDateMillis: Long?,
        endDateMillis: Long?
    ): List<TransactionEntity>
    
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE userId = :userId AND isDeleted = 0
        AND (:accountId IS NULL OR accountId = :accountId)
        AND (:startDateMillis IS NULL OR createdAt >= :startDateMillis)
        AND (:endDateMillis IS NULL OR createdAt <= :endDateMillis)
    """)
    suspend fun getTransactionsPaginatedCount(
        userId: String,
        accountId: String?,
        startDateMillis: Long?,
        endDateMillis: Long?
    ): Int
}


data class CategoryStatistic(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Int,
    val transactionCount: Int
)