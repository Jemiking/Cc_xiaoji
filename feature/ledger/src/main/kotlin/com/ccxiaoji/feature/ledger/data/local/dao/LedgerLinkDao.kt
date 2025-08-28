package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerLinkEntity
import kotlinx.coroutines.flow.Flow

/**
 * 记账簿联动关系数据访问对象
 */
@Dao
interface LedgerLinkDao {
    
    /**
     * 获取指定记账簿的所有活跃联动关系（作为父记账簿）
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE parent_ledger_id = :ledgerId AND is_active = 1 
        ORDER BY created_at ASC
    """)
    fun getChildLinks(ledgerId: String): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取指定记账簿的所有活跃联动关系（作为子记账簿）
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE child_ledger_id = :ledgerId AND is_active = 1 
        ORDER BY created_at ASC
    """)
    fun getParentLinks(ledgerId: String): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取指定记账簿的所有联动关系（无论活跃状态）
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE (parent_ledger_id = :ledgerId OR child_ledger_id = :ledgerId)
        ORDER BY created_at ASC
    """)
    fun getAllLinksForLedger(ledgerId: String): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取两个记账簿之间的联动关系
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE ((parent_ledger_id = :ledgerId1 AND child_ledger_id = :ledgerId2) 
            OR (parent_ledger_id = :ledgerId2 AND child_ledger_id = :ledgerId1))
        AND is_active = 1
        LIMIT 1
    """)
    suspend fun getLinkBetweenLedgers(ledgerId1: String, ledgerId2: String): LedgerLinkEntity?
    
    /**
     * 根据ID获取联动关系
     */
    @Query("SELECT * FROM ledger_links WHERE id = :linkId")
    suspend fun getLinkById(linkId: String): LedgerLinkEntity?
    
    /**
     * 获取所有活跃的联动关系
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE is_active = 1 
        ORDER BY created_at ASC
    """)
    fun getAllActiveLinks(): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取启用自动同步的联动关系
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE is_active = 1 AND auto_sync_enabled = 1 
        ORDER BY created_at ASC
    """)
    fun getAutoSyncLinks(): Flow<List<LedgerLinkEntity>>
    
    /**
     * 检查两个记账簿是否已有联动关系
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM ledger_links 
        WHERE ((parent_ledger_id = :ledgerId1 AND child_ledger_id = :ledgerId2) 
            OR (parent_ledger_id = :ledgerId2 AND child_ledger_id = :ledgerId1))
        AND is_active = 1
    """)
    suspend fun hasActiveLinkBetween(ledgerId1: String, ledgerId2: String): Boolean
    
    /**
     * 插入联动关系
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: LedgerLinkEntity): Long
    
    /**
     * 更新联动关系
     */
    @Update
    suspend fun updateLink(link: LedgerLinkEntity)
    
    /**
     * 删除联动关系（软删除 - 设置为非激活状态）
     */
    @Query("""
        UPDATE ledger_links 
        SET is_active = 0, updated_at = :updatedAt 
        WHERE id = :linkId
    """)
    suspend fun deactivateLink(linkId: String, updatedAt: Long)
    
    /**
     * 硬删除联动关系
     */
    @Delete
    suspend fun deleteLink(link: LedgerLinkEntity)
    
    /**
     * 启用或禁用自动同步
     */
    @Query("""
        UPDATE ledger_links 
        SET auto_sync_enabled = :enabled, updated_at = :updatedAt 
        WHERE id = :linkId
    """)
    suspend fun setAutoSyncEnabled(linkId: String, enabled: Boolean, updatedAt: Long)
    
    /**
     * 更新同步模式
     */
    @Query("""
        UPDATE ledger_links 
        SET sync_mode = :syncMode, updated_at = :updatedAt 
        WHERE id = :linkId
    """)
    suspend fun updateSyncMode(linkId: String, syncMode: String, updatedAt: Long)
    
    /**
     * 获取记账簿的子记账簿数量
     */
    @Query("""
        SELECT COUNT(*) FROM ledger_links 
        WHERE parent_ledger_id = :ledgerId AND is_active = 1
    """)
    suspend fun getChildLedgerCount(ledgerId: String): Int
    
    /**
     * 获取记账簿的父记账簿数量
     */
    @Query("""
        SELECT COUNT(*) FROM ledger_links 
        WHERE child_ledger_id = :ledgerId AND is_active = 1
    """)
    suspend fun getParentLedgerCount(ledgerId: String): Int
    
    /**
     * 删除涉及指定记账簿的所有联动关系
     */
    @Query("""
        UPDATE ledger_links 
        SET is_active = 0, updated_at = :updatedAt 
        WHERE (parent_ledger_id = :ledgerId OR child_ledger_id = :ledgerId) 
        AND is_active = 1
    """)
    suspend fun deactivateAllLinksForLedger(ledgerId: String, updatedAt: Long)
    
    /**
     * 根据同步模式筛选联动关系
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE sync_mode = :syncMode AND is_active = 1 
        ORDER BY created_at ASC
    """)
    fun getLinksBySyncMode(syncMode: String): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取需要从指定记账簿同步的联动关系
     * （即当前记账簿作为目标的同步关系）
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE is_active = 1 AND auto_sync_enabled = 1 
        AND (
            (child_ledger_id = :ledgerId AND (sync_mode = 'BIDIRECTIONAL' OR sync_mode = 'PARENT_TO_CHILD'))
            OR 
            (parent_ledger_id = :ledgerId AND (sync_mode = 'BIDIRECTIONAL' OR sync_mode = 'CHILD_TO_PARENT'))
        )
        ORDER BY created_at ASC
    """)
    fun getIncomingSyncLinks(ledgerId: String): Flow<List<LedgerLinkEntity>>
    
    /**
     * 获取需要向其他记账簿同步的联动关系
     * （即当前记账簿作为源的同步关系）
     */
    @Query("""
        SELECT * FROM ledger_links 
        WHERE is_active = 1 AND auto_sync_enabled = 1 
        AND (
            (parent_ledger_id = :ledgerId AND (sync_mode = 'BIDIRECTIONAL' OR sync_mode = 'PARENT_TO_CHILD'))
            OR 
            (child_ledger_id = :ledgerId AND (sync_mode = 'BIDIRECTIONAL' OR sync_mode = 'CHILD_TO_PARENT'))
        )
        ORDER BY created_at ASC
    """)
    fun getOutgoingSyncLinks(ledgerId: String): Flow<List<LedgerLinkEntity>>
}