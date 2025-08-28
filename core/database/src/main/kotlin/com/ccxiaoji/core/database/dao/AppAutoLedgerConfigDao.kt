package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.AppAutoLedgerConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * 应用自动记账配置数据访问对象
 * 
 * 提供配置表的增删查改操作
 */
@Dao
interface AppAutoLedgerConfigDao {
    
    /**
     * 插入或更新配置
     * 
     * @param entity 配置实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppAutoLedgerConfigEntity)
    
    /**
     * 根据包名查询配置
     * 
     * @param packageName 应用包名
     * @return 配置实体，可能为null
     */
    @Query("SELECT * FROM app_auto_ledger_config WHERE appPkg = :packageName")
    suspend fun getByPackage(packageName: String): AppAutoLedgerConfigEntity?
    
    /**
     * 观察指定包名的配置变化
     * 
     * @param packageName 应用包名
     * @return 配置流，可能为null
     */
    @Query("SELECT * FROM app_auto_ledger_config WHERE appPkg = :packageName")
    fun observeByPackage(packageName: String): Flow<AppAutoLedgerConfigEntity?>
    
    /**
     * 获取所有配置
     * 
     * @return 配置列表
     */
    @Query("SELECT * FROM app_auto_ledger_config ORDER BY appPkg")
    suspend fun getAll(): List<AppAutoLedgerConfigEntity>
    
    /**
     * 观察所有配置变化
     * 
     * @return 配置列表流
     */
    @Query("SELECT * FROM app_auto_ledger_config ORDER BY appPkg")
    fun observeAll(): Flow<List<AppAutoLedgerConfigEntity>>
    
    /**
     * 根据模式查询配置
     * 
     * @param mode 自动记账模式 (0:禁用, 1:半自动, 2:全自动)
     * @return 匹配的配置列表
     */
    @Query("SELECT * FROM app_auto_ledger_config WHERE mode = :mode ORDER BY appPkg")
    suspend fun getByMode(mode: Int): List<AppAutoLedgerConfigEntity>
    
    /**
     * 获取启用的配置（模式 > 0）
     * 
     * @return 启用的配置列表
     */
    @Query("SELECT * FROM app_auto_ledger_config WHERE mode > 0 ORDER BY appPkg")
    suspend fun getEnabled(): List<AppAutoLedgerConfigEntity>
    
    /**
     * 观察启用的配置变化
     * 
     * @return 启用的配置列表流
     */
    @Query("SELECT * FROM app_auto_ledger_config WHERE mode > 0 ORDER BY appPkg")
    fun observeEnabled(): Flow<List<AppAutoLedgerConfigEntity>>
    
    /**
     * 更新应用模式
     * 
     * @param packageName 应用包名
     * @param mode 新的模式
     * @param updatedAt 更新时间
     * @return 更新的行数
     */
    @Query("""
        UPDATE app_auto_ledger_config 
        SET mode = :mode, updatedAt = :updatedAt 
        WHERE appPkg = :packageName
    """)
    suspend fun updateMode(packageName: String, mode: Int, updatedAt: Long): Int
    
    /**
     * 更新置信度阈值
     * 
     * @param packageName 应用包名
     * @param threshold 新的阈值
     * @param updatedAt 更新时间
     * @return 更新的行数
     */
    @Query("""
        UPDATE app_auto_ledger_config 
        SET confidenceThreshold = :threshold, updatedAt = :updatedAt 
        WHERE appPkg = :packageName
    """)
    suspend fun updateConfidenceThreshold(
        packageName: String, 
        threshold: Double, 
        updatedAt: Long
    ): Int
    
    /**
     * 更新黑名单
     * 
     * @param packageName 应用包名
     * @param blacklist 黑名单JSON
     * @param updatedAt 更新时间
     * @return 更新的行数
     */
    @Query("""
        UPDATE app_auto_ledger_config 
        SET blacklist = :blacklist, updatedAt = :updatedAt 
        WHERE appPkg = :packageName
    """)
    suspend fun updateBlacklist(packageName: String, blacklist: String?, updatedAt: Long): Int
    
    /**
     * 更新白名单
     * 
     * @param packageName 应用包名
     * @param whitelist 白名单JSON
     * @param updatedAt 更新时间
     * @return 更新的行数
     */
    @Query("""
        UPDATE app_auto_ledger_config 
        SET whitelist = :whitelist, updatedAt = :updatedAt 
        WHERE appPkg = :packageName
    """)
    suspend fun updateWhitelist(packageName: String, whitelist: String?, updatedAt: Long): Int
    
    /**
     * 删除指定应用的配置
     * 
     * @param packageName 应用包名
     * @return 删除的行数
     */
    @Query("DELETE FROM app_auto_ledger_config WHERE appPkg = :packageName")
    suspend fun deleteByPackage(packageName: String): Int
    
    /**
     * 重置所有配置为默认值
     * 
     * @param updatedAt 更新时间
     * @return 更新的行数
     */
    @Query("""
        UPDATE app_auto_ledger_config 
        SET mode = 1, 
            confidenceThreshold = 0.85, 
            amountWindowSec = 300,
            updatedAt = :updatedAt
    """)
    suspend fun resetToDefaults(updatedAt: Long): Int
}