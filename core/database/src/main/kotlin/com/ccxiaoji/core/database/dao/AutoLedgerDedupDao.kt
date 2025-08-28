package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.AutoLedgerDedupEntity

/**
 * 自动记账去重数据访问对象
 * 
 * 提供去重表的增删查改操作
 */
@Dao
interface AutoLedgerDedupDao {
    
    /**
     * 插入去重记录
     * 
     * @param entity 去重实体
     * @return 插入行数，0表示已存在（唯一约束冲突）
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: AutoLedgerDedupEntity): Long
    
    /**
     * 检查事件是否已存在
     * 
     * @param eventKey 事件指纹
     * @return 是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM auto_ledger_dedup WHERE eventKey = :eventKey")
    suspend fun exists(eventKey: String): Boolean
    
    /**
     * 根据事件指纹查询
     * 
     * @param eventKey 事件指纹
     * @return 去重实体，可能为null
     */
    @Query("SELECT * FROM auto_ledger_dedup WHERE eventKey = :eventKey")
    suspend fun getByEventKey(eventKey: String): AutoLedgerDedupEntity?
    
    /**
     * 清理过期记录
     * 
     * @param expiredBefore 过期时间点（之前的记录将被删除）
     * @return 删除的记录数
     */
    @Query("DELETE FROM auto_ledger_dedup WHERE createdAt < :expiredBefore")
    suspend fun cleanup(expiredBefore: Long): Int
    
    /**
     * 根据应用包名和时间范围查询
     * 用于检查重复通知
     * 
     * @param packageName 应用包名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 去重记录列表
     */
    @Query("""
        SELECT * FROM auto_ledger_dedup 
        WHERE packageName = :packageName 
        AND postTime BETWEEN :startTime AND :endTime
        ORDER BY postTime DESC
    """)
    suspend fun findByPackageAndTimeRange(
        packageName: String,
        startTime: Long,
        endTime: Long
    ): List<AutoLedgerDedupEntity>
    
    /**
     * 根据金额和时间窗口查询
     * 用于防止相同金额的重复记账
     * 
     * @param amountCents 金额（分）
     * @param windowStart 时间窗口开始
     * @param windowEnd 时间窗口结束
     * @return 匹配的记录列表
     */
    @Query("""
        SELECT * FROM auto_ledger_dedup 
        WHERE amountCents = :amountCents 
        AND postTime BETWEEN :windowStart AND :windowEnd
        ORDER BY postTime DESC
    """)
    suspend fun findByAmountAndTimeWindow(
        amountCents: Long,
        windowStart: Long,
        windowEnd: Long
    ): List<AutoLedgerDedupEntity>
    
    /**
     * 获取去重统计信息
     * 
     * @return 去重记录数量
     */
    @Query("SELECT COUNT(*) FROM auto_ledger_dedup")
    suspend fun count(): Int
    
    /**
     * 获取各应用的去重统计
     * 
     * @return 包名和对应的记录数
     */
    @Query("""
        SELECT packageName, COUNT(*) as count 
        FROM auto_ledger_dedup 
        GROUP BY packageName 
        ORDER BY count DESC
    """)
    suspend fun getStatsByPackage(): List<PackageStats>
    
    /**
     * 删除指定应用的所有去重记录
     * 
     * @param packageName 应用包名
     * @return 删除的记录数
     */
    @Query("DELETE FROM auto_ledger_dedup WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String): Int

    /**
     * 清空所有去重记录
     *
     * 用于调试或用户在设置页一键清理缓存
     * @return 删除的记录数
     */
    @Query("DELETE FROM auto_ledger_dedup")
    suspend fun clearAll(): Int
}

/**
 * 应用包名统计数据类
 */
data class PackageStats(
    val packageName: String,
    val count: Int
)
