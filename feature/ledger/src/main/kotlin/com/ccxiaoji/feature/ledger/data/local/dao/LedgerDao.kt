package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerEntity
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerWithStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 记账簿数据访问对象
 */
@Dao
interface LedgerDao {
    
    /**
     * 获取用户的所有激活记账簿（按显示顺序排序）
     */
    @Query("""
        SELECT * FROM ledgers 
        WHERE userId = :userId AND isActive = 1 
        ORDER BY displayOrder ASC, createdAt ASC
    """)
    fun getUserLedgers(userId: String): Flow<List<LedgerEntity>>
    
    /**
     * 获取用户的默认记账簿
     */
    @Query("""
        SELECT * FROM ledgers 
        WHERE userId = :userId AND isDefault = 1 AND isActive = 1 
        LIMIT 1
    """)
    suspend fun getDefaultLedger(userId: String): LedgerEntity?
    
    /**
     * 根据ID获取记账簿
     */
    @Query("SELECT * FROM ledgers WHERE id = :ledgerId")
    suspend fun getLedgerById(ledgerId: String): LedgerEntity?
    
    /**
     * 获取记账簿及其统计数据
     */
    @Query("""
        SELECT 
            l.*,
            COUNT(CASE WHEN t.isDeleted = 0 THEN t.id END) as transactionCount,
            COALESCE(SUM(CASE WHEN t.amountCents > 0 AND t.isDeleted = 0 THEN t.amountCents END), 0) as totalIncome,
            COALESCE(ABS(SUM(CASE WHEN t.amountCents < 0 AND t.isDeleted = 0 THEN t.amountCents END)), 0) as totalExpense,
            MAX(CASE WHEN t.isDeleted = 0 THEN COALESCE(t.transactionDate, t.createdAt) END) as lastTransactionDate
        FROM ledgers l 
        LEFT JOIN transactions t ON l.id = t.ledgerId 
        WHERE l.userId = :userId AND l.isActive = 1 
        GROUP BY l.id 
        ORDER BY l.displayOrder ASC, l.createdAt ASC
    """)
    fun getUserLedgersWithStats(userId: String): Flow<List<LedgerWithStatsEntity>>
    
    /**
     * 插入记账簿
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: LedgerEntity): Long
    
    /**
     * 更新记账簿
     */
    @Update
    suspend fun updateLedger(ledger: LedgerEntity)
    
    /**
     * 删除记账簿（软删除 - 设置为非激活状态）
     */
    @Query("UPDATE ledgers SET isActive = 0, updatedAt = :updatedAt WHERE id = :ledgerId")
    suspend fun deactivateLedger(ledgerId: String, updatedAt: Long)
    
    /**
     * 硬删除记账簿（仅用于清理操作）
     */
    @Delete
    suspend fun deleteLedger(ledger: LedgerEntity)
    
    /**
     * 检查用户是否有默认记账簿
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM ledgers 
        WHERE userId = :userId AND isDefault = 1 AND isActive = 1
    """)
    suspend fun hasDefaultLedger(userId: String): Boolean
    
    /**
     * 设置默认记账簿（先清除其他默认记账簿）
     */
    @Transaction
    suspend fun setDefaultLedger(userId: String, ledgerId: String, updatedAt: Long) {
        // 清除其他默认记账簿
        clearDefaultLedgers(userId, updatedAt)
        // 设置新的默认记账簿
        setLedgerAsDefault(ledgerId, updatedAt)
    }
    
    /**
     * 清除用户的所有默认记账簿标记
     */
    @Query("UPDATE ledgers SET isDefault = 0, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun clearDefaultLedgers(userId: String, updatedAt: Long)
    
    /**
     * 设置指定记账簿为默认
     */
    @Query("UPDATE ledgers SET isDefault = 1, updatedAt = :updatedAt WHERE id = :ledgerId")
    suspend fun setLedgerAsDefault(ledgerId: String, updatedAt: Long)
    
    /**
     * 更新记账簿显示顺序
     */
    @Query("UPDATE ledgers SET displayOrder = :order, updatedAt = :updatedAt WHERE id = :ledgerId")
    suspend fun updateLedgerOrder(ledgerId: String, order: Int, updatedAt: Long)
    
    /**
     * 获取下一个显示顺序
     */
    @Query("SELECT COALESCE(MAX(displayOrder), 0) + 1 FROM ledgers WHERE userId = :userId")
    suspend fun getNextDisplayOrder(userId: String): Int
}