package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionEntity

@Dao
interface DebugDao {
    /**
     * 调试：查询指定月份的交易数量
     */
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE userId = :userId 
        AND createdAt >= :startMillis 
        AND createdAt < :endMillis
        AND isDeleted = 0
    """)
    suspend fun getTransactionCountInRange(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): Int
    
    /**
     * 调试：获取指定月份的前10条交易
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND createdAt >= :startMillis 
        AND createdAt < :endMillis
        AND isDeleted = 0
        ORDER BY createdAt DESC
        LIMIT 10
    """)
    suspend fun getTransactionsInRange(
        userId: String,
        startMillis: Long,
        endMillis: Long
    ): List<TransactionEntity>
    
    /**
     * 调试：获取用户最新和最旧的交易时间
     */
    @Query("""
        SELECT MIN(createdAt) as min, MAX(createdAt) as max 
        FROM transactions 
        WHERE userId = :userId AND isDeleted = 0
    """)
    suspend fun getTransactionTimeRange(userId: String): TimeRange?
    
    data class TimeRange(
        val min: Long?,
        val max: Long?
    )
}