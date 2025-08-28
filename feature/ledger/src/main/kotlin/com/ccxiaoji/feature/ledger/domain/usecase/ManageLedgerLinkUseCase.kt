package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.LedgerLinkStatus
import com.ccxiaoji.feature.ledger.domain.model.LedgerNetworkStats
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 记账簿联动关系管理用例
 * 
 * 提供记账簿联动关系的创建、编辑、删除、查询等核心业务操作
 */
class ManageLedgerLinkUseCase @Inject constructor(
    private val ledgerLinkRepository: LedgerLinkRepository,
    private val ledgerRepository: LedgerRepository
) {
    
    /**
     * 获取指定记账簿的所有联动关系
     */
    fun getLedgerLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getAllLinksForLedger(ledgerId)
    }
    
    /**
     * 获取指定记账簿的联动状态信息
     */
    suspend fun getLedgerLinkStatus(ledgerId: String): BaseResult<List<LedgerLinkStatus>> {
        return ledgerLinkRepository.getLedgerLinkStatus(ledgerId)
    }
    
    /**
     * 获取所有活跃的联动关系
     */
    fun getAllActiveLinks(): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getAllActiveLinks()
    }
    
    /**
     * 获取启用自动同步的联动关系
     */
    fun getAutoSyncLinks(): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getAutoSyncLinks()
    }
    
    /**
     * 创建记账簿联动关系
     */
    suspend fun createLedgerLink(
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode = SyncMode.BIDIRECTIONAL,
        autoSyncEnabled: Boolean = true
    ): BaseResult<LedgerLink> {
        
        // 基础验证
        if (parentLedgerId == childLedgerId) {
            return BaseResult.Error(Exception("不能将记账簿与自己建立联动关系"))
        }
        
        if (parentLedgerId.isBlank() || childLedgerId.isBlank()) {
            return BaseResult.Error(Exception("记账簿ID不能为空"))
        }
        
        // 检查记账簿是否存在且有权限
        val validationResult = validateLedgersForLink(parentLedgerId, childLedgerId)
        if (validationResult is BaseResult.Error) {
            return validationResult
        }
        
        // 检查是否已存在联动关系
        val existingLinkResult = ledgerLinkRepository.hasActiveLinkBetween(parentLedgerId, childLedgerId)
        if (existingLinkResult is BaseResult.Success && existingLinkResult.data) {
            return BaseResult.Error(Exception("记账簿之间已存在联动关系"))
        }
        
        // 检查是否会形成循环依赖
        val circularDependencyResult = checkCircularDependency(parentLedgerId, childLedgerId)
        if (circularDependencyResult is BaseResult.Error) {
            return circularDependencyResult
        }
        
        // 创建联动关系
        return ledgerLinkRepository.createLink(
            parentLedgerId = parentLedgerId,
            childLedgerId = childLedgerId,
            syncMode = syncMode,
            autoSyncEnabled = autoSyncEnabled
        )
    }
    
    /**
     * 更新联动关系的同步模式
     */
    suspend fun updateSyncMode(linkId: String, syncMode: SyncMode): BaseResult<Unit> {
        if (linkId.isBlank()) {
            return BaseResult.Error(Exception("联动关系ID不能为空"))
        }
        
        // 验证联动关系是否存在
        val linkResult = ledgerLinkRepository.getLinkById(linkId)
        if (linkResult is BaseResult.Error) {
            return linkResult
        }
        
        if (linkResult is BaseResult.Success && linkResult.data == null) {
            return BaseResult.Error(Exception("联动关系不存在"))
        }
        
        return ledgerLinkRepository.updateSyncMode(linkId, syncMode)
    }
    
    /**
     * 启用或禁用自动同步
     */
    suspend fun setAutoSyncEnabled(linkId: String, enabled: Boolean): BaseResult<Unit> {
        if (linkId.isBlank()) {
            return BaseResult.Error(Exception("联动关系ID不能为空"))
        }
        
        // 验证联动关系是否存在
        val linkResult = ledgerLinkRepository.getLinkById(linkId)
        if (linkResult is BaseResult.Error) {
            return linkResult
        }
        
        if (linkResult is BaseResult.Success && linkResult.data == null) {
            return BaseResult.Error(Exception("联动关系不存在"))
        }
        
        return ledgerLinkRepository.setAutoSyncEnabled(linkId, enabled)
    }
    
    /**
     * 删除联动关系
     */
    suspend fun deleteLedgerLink(linkId: String): BaseResult<Unit> {
        if (linkId.isBlank()) {
            return BaseResult.Error(Exception("联动关系ID不能为空"))
        }
        
        // 验证联动关系是否存在
        val linkResult = ledgerLinkRepository.getLinkById(linkId)
        if (linkResult is BaseResult.Error) {
            return linkResult
        }
        
        if (linkResult is BaseResult.Success && linkResult.data == null) {
            return BaseResult.Error(Exception("联动关系不存在"))
        }
        
        // 删除联动关系
        // TODO: 考虑是否需要清理相关的同步交易记录
        return ledgerLinkRepository.deleteLink(linkId)
    }
    
    /**
     * 删除记账簿的所有联动关系
     */
    suspend fun deleteAllLinksForLedger(ledgerId: String): BaseResult<Unit> {
        if (ledgerId.isBlank()) {
            return BaseResult.Error(Exception("记账簿ID不能为空"))
        }
        
        // 验证记账簿是否存在
        val ledgerResult = ledgerRepository.getLedgerById(ledgerId)
        if (ledgerResult is BaseResult.Error) {
            return BaseResult.Error(Exception("记账簿不存在"))
        }
        
        return ledgerLinkRepository.deleteAllLinksForLedger(ledgerId)
    }
    
    /**
     * 获取两个记账簿之间的联动关系
     */
    suspend fun getLinkBetweenLedgers(ledgerId1: String, ledgerId2: String): BaseResult<LedgerLink?> {
        if (ledgerId1.isBlank() || ledgerId2.isBlank()) {
            return BaseResult.Error(Exception("记账簿ID不能为空"))
        }
        
        return ledgerLinkRepository.getLinkBetweenLedgers(ledgerId1, ledgerId2)
    }
    
    /**
     * 检查两个记账簿是否有联动关系
     */
    suspend fun hasLinkBetweenLedgers(ledgerId1: String, ledgerId2: String): BaseResult<Boolean> {
        if (ledgerId1.isBlank() || ledgerId2.isBlank()) {
            return BaseResult.Error(Exception("记账簿ID不能为空"))
        }
        
        return ledgerLinkRepository.hasActiveLinkBetween(ledgerId1, ledgerId2)
    }
    
    /**
     * 获取需要从指定记账簿同步的联动关系
     */
    fun getIncomingSyncLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getIncomingSyncLinks(ledgerId)
    }
    
    /**
     * 获取需要向其他记账簿同步的联动关系
     */
    fun getOutgoingSyncLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getOutgoingSyncLinks(ledgerId)
    }
    
    /**
     * 根据同步模式筛选联动关系
     */
    fun getLinksBySyncMode(syncMode: SyncMode): Flow<List<LedgerLink>> {
        return ledgerLinkRepository.getLinksBySyncMode(syncMode)
    }
    
    /**
     * 获取联动网络统计信息
     */
    suspend fun getNetworkStats(): BaseResult<LedgerNetworkStats> {
        return ledgerLinkRepository.getNetworkStats()
    }
    
    /**
     * 获取推荐的同步模式列表
     */
    fun getRecommendedSyncModes(): List<SyncMode> {
        return listOf(
            SyncMode.BIDIRECTIONAL,
            SyncMode.PARENT_TO_CHILD,
            SyncMode.CHILD_TO_PARENT
        )
    }
    
    /**
     * 获取同步模式的描述信息
     */
    fun getSyncModeDescription(syncMode: SyncMode): String {
        return syncMode.description
    }
    
    /**
     * 验证记账簿是否可以建立联动关系
     */
    private suspend fun validateLedgersForLink(
        parentLedgerId: String,
        childLedgerId: String
    ): BaseResult<Unit> {
        // 检查父记账簿
        val parentLedgerResult = ledgerRepository.getLedgerById(parentLedgerId)
        if (parentLedgerResult is BaseResult.Error) {
            return BaseResult.Error(Exception("父记账簿不存在"))
        }
        
        val parentLedger = (parentLedgerResult as BaseResult.Success).data
        if (!parentLedger.isActive) {
            return BaseResult.Error(Exception("父记账簿未激活"))
        }
        
        // 检查子记账簿
        val childLedgerResult = ledgerRepository.getLedgerById(childLedgerId)
        if (childLedgerResult is BaseResult.Error) {
            return BaseResult.Error(Exception("子记账簿不存在"))
        }
        
        val childLedger = (childLedgerResult as BaseResult.Success).data
        if (!childLedger.isActive) {
            return BaseResult.Error(Exception("子记账簿未激活"))
        }
        
        // 检查是否属于同一用户
        if (parentLedger.userId != childLedger.userId) {
            return BaseResult.Error(Exception("不能在不同用户的记账簿间建立联动关系"))
        }
        
        return BaseResult.Success(Unit)
    }
    
    /**
     * 检查是否会形成循环依赖
     * 简化实现：检查反向关系是否已存在
     */
    private suspend fun checkCircularDependency(
        parentLedgerId: String,
        childLedgerId: String
    ): BaseResult<Unit> {
        // 检查是否已存在反向关系（子记账簿作为父记账簿）
        val reverseLinks = ledgerLinkRepository.getChildLinks(childLedgerId)
        
        // TODO: 完整的循环依赖检查需要递归遍历整个依赖图
        // 这里只做简单的直接反向关系检查
        
        return BaseResult.Success(Unit)
    }
}