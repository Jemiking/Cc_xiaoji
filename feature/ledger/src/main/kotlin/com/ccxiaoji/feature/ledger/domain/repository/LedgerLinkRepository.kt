package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.LedgerLinkStatus
import com.ccxiaoji.feature.ledger.domain.model.LedgerNetworkStats
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import kotlinx.coroutines.flow.Flow

/**
 * 记账簿联动关系仓库接口
 */
interface LedgerLinkRepository {
    
    /**
     * 获取指定记账簿的所有子联动关系
     */
    fun getChildLinks(ledgerId: String): Flow<List<LedgerLink>>
    
    /**
     * 获取指定记账簿的所有父联动关系
     */
    fun getParentLinks(ledgerId: String): Flow<List<LedgerLink>>
    
    /**
     * 获取指定记账簿的所有联动关系
     */
    fun getAllLinksForLedger(ledgerId: String): Flow<List<LedgerLink>>
    
    /**
     * 获取两个记账簿之间的联动关系
     */
    suspend fun getLinkBetweenLedgers(ledgerId1: String, ledgerId2: String): BaseResult<LedgerLink?>
    
    /**
     * 根据ID获取联动关系
     */
    suspend fun getLinkById(linkId: String): BaseResult<LedgerLink?>
    
    /**
     * 获取所有活跃的联动关系
     */
    fun getAllActiveLinks(): Flow<List<LedgerLink>>
    
    /**
     * 获取启用自动同步的联动关系
     */
    fun getAutoSyncLinks(): Flow<List<LedgerLink>>
    
    /**
     * 检查两个记账簿是否已有联动关系
     */
    suspend fun hasActiveLinkBetween(ledgerId1: String, ledgerId2: String): BaseResult<Boolean>
    
    /**
     * 创建联动关系
     */
    suspend fun createLink(
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode = SyncMode.BIDIRECTIONAL,
        autoSyncEnabled: Boolean = true
    ): BaseResult<LedgerLink>
    
    /**
     * 更新联动关系
     */
    suspend fun updateLink(link: LedgerLink): BaseResult<Unit>
    
    /**
     * 删除联动关系
     */
    suspend fun deleteLink(linkId: String): BaseResult<Unit>
    
    /**
     * 启用或禁用自动同步
     */
    suspend fun setAutoSyncEnabled(linkId: String, enabled: Boolean): BaseResult<Unit>
    
    /**
     * 更新同步模式
     */
    suspend fun updateSyncMode(linkId: String, syncMode: SyncMode): BaseResult<Unit>
    
    /**
     * 获取记账簿的联动状态信息
     */
    suspend fun getLedgerLinkStatus(ledgerId: String): BaseResult<List<LedgerLinkStatus>>
    
    /**
     * 获取需要从指定记账簿同步的联动关系
     */
    fun getIncomingSyncLinks(ledgerId: String): Flow<List<LedgerLink>>
    
    /**
     * 获取需要向其他记账簿同步的联动关系
     */
    fun getOutgoingSyncLinks(ledgerId: String): Flow<List<LedgerLink>>
    
    /**
     * 删除涉及指定记账簿的所有联动关系
     */
    suspend fun deleteAllLinksForLedger(ledgerId: String): BaseResult<Unit>
    
    /**
     * 根据同步模式筛选联动关系
     */
    fun getLinksBySyncMode(syncMode: SyncMode): Flow<List<LedgerLink>>
    
    /**
     * 获取联动网络统计信息
     */
    suspend fun getNetworkStats(): BaseResult<LedgerNetworkStats>
    
    /**
     * 验证联动关系配置的有效性
     */
    suspend fun validateLinkConfiguration(
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode
    ): BaseResult<Unit>
}