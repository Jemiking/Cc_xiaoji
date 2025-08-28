package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerDao
import com.ccxiaoji.feature.ledger.data.local.dao.LedgerLinkDao
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import com.ccxiaoji.feature.ledger.data.local.entity.LedgerLinkEntity
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.LedgerLinkStatus
import com.ccxiaoji.feature.ledger.domain.model.LedgerNetworkStats
import com.ccxiaoji.feature.ledger.domain.model.SyncMode
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账簿联动关系仓库实现
 */
@Singleton
class LedgerLinkRepositoryImpl @Inject constructor(
    private val ledgerLinkDao: LedgerLinkDao,
    private val ledgerDao: LedgerDao,
    private val transactionLedgerRelationDao: TransactionLedgerRelationDao
) : LedgerLinkRepository {
    
    override fun getChildLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getChildLinks(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getParentLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getParentLinks(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAllLinksForLedger(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getAllLinksForLedger(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getLinkBetweenLedgers(ledgerId1: String, ledgerId2: String): BaseResult<LedgerLink?> {
        return try {
            val entity = ledgerLinkDao.getLinkBetweenLedgers(ledgerId1, ledgerId2)
            BaseResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取联动关系失败: ${e.message}"))
        }
    }
    
    override suspend fun getLinkById(linkId: String): BaseResult<LedgerLink?> {
        return try {
            val entity = ledgerLinkDao.getLinkById(linkId)
            BaseResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取联动关系失败: ${e.message}"))
        }
    }
    
    override fun getAllActiveLinks(): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getAllActiveLinks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getAutoSyncLinks(): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getAutoSyncLinks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun hasActiveLinkBetween(ledgerId1: String, ledgerId2: String): BaseResult<Boolean> {
        return try {
            val hasLink = ledgerLinkDao.hasActiveLinkBetween(ledgerId1, ledgerId2)
            BaseResult.Success(hasLink)
        } catch (e: Exception) {
            BaseResult.Error(Exception("检查联动关系失败: ${e.message}"))
        }
    }
    
    override suspend fun createLink(
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode,
        autoSyncEnabled: Boolean
    ): BaseResult<LedgerLink> {
        return try {
            // 验证配置有效性
            when (val validationResult = validateLinkConfiguration(parentLedgerId, childLedgerId, syncMode)) {
                is BaseResult.Error -> return validationResult
                else -> { /* 验证通过 */ }
            }
            
            // 检查是否已存在联动关系
            if (ledgerLinkDao.hasActiveLinkBetween(parentLedgerId, childLedgerId)) {
                return BaseResult.Error(Exception("记账簿之间已存在联动关系"))
            }
            
            val now = Clock.System.now()
            val link = LedgerLinkEntity(
                id = UUID.randomUUID().toString(),
                parentLedgerId = parentLedgerId,
                childLedgerId = childLedgerId,
                syncMode = syncMode.name,
                autoSyncEnabled = autoSyncEnabled,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
            
            ledgerLinkDao.insertLink(link)
            BaseResult.Success(link.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("创建联动关系失败: ${e.message}"))
        }
    }
    
    override suspend fun updateLink(link: LedgerLink): BaseResult<Unit> {
        return try {
            val entity = LedgerLinkEntity.fromDomain(
                link.copy(updatedAt = Clock.System.now())
            )
            ledgerLinkDao.updateLink(entity)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("更新联动关系失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteLink(linkId: String): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerLinkDao.deactivateLink(linkId, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除联动关系失败: ${e.message}"))
        }
    }
    
    override suspend fun setAutoSyncEnabled(linkId: String, enabled: Boolean): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerLinkDao.setAutoSyncEnabled(linkId, enabled, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("设置自动同步失败: ${e.message}"))
        }
    }
    
    override suspend fun updateSyncMode(linkId: String, syncMode: SyncMode): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerLinkDao.updateSyncMode(linkId, syncMode.name, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("更新同步模式失败: ${e.message}"))
        }
    }
    
    override suspend fun getLedgerLinkStatus(ledgerId: String): BaseResult<List<LedgerLinkStatus>> {
        return try {
            val links = getAllLinksForLedger(ledgerId).first()
            val statuses = links.map { link ->
                val parentLedger = ledgerDao.getLedgerById(link.parentLedgerId)
                val childLedger = ledgerDao.getLedgerById(link.childLedgerId)
                
                if (parentLedger != null && childLedger != null) {
                    // 获取联动交易数量统计
                    val linkedTransactionCount = getLinkedTransactionCount(link.id)
                    
                    LedgerLinkStatus(
                        link = link,
                        parentLedger = parentLedger.toDomain(),
                        childLedger = childLedger.toDomain(),
                        linkedTransactionCount = linkedTransactionCount,
                        lastSyncTime = null // TODO: 实现同步时间追踪
                    )
                } else {
                    null
                }
            }.filterNotNull()
            
            BaseResult.Success(statuses)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取联动状态失败: ${e.message}"))
        }
    }
    
    override fun getIncomingSyncLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getIncomingSyncLinks(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getOutgoingSyncLinks(ledgerId: String): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getOutgoingSyncLinks(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun deleteAllLinksForLedger(ledgerId: String): BaseResult<Unit> {
        return try {
            val now = Clock.System.now()
            ledgerLinkDao.deactivateAllLinksForLedger(ledgerId, now.epochSeconds)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除记账簿联动关系失败: ${e.message}"))
        }
    }
    
    override fun getLinksBySyncMode(syncMode: SyncMode): Flow<List<LedgerLink>> {
        return ledgerLinkDao.getLinksBySyncMode(syncMode.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getNetworkStats(): BaseResult<LedgerNetworkStats> {
        return try {
            val allLinks = getAllActiveLinks().first()
            val allLedgerIds = mutableSetOf<String>()
            
            allLinks.forEach { link ->
                allLedgerIds.add(link.parentLedgerId)
                allLedgerIds.add(link.childLedgerId)
            }
            
            val totalLedgers = allLedgerIds.size
            val linkedLedgers = totalLedgers
            val independentLedgers = 0 // TODO: 计算独立记账簿数量
            val activeLinks = allLinks.size
            val autoSyncLinks = allLinks.count { it.autoSyncEnabled }
            
            val stats = LedgerNetworkStats(
                totalLedgers = totalLedgers,
                linkedLedgers = linkedLedgers,
                independentLedgers = independentLedgers,
                totalLinks = activeLinks,
                activeLinks = activeLinks,
                autoSyncLinks = autoSyncLinks
            )
            
            BaseResult.Success(stats)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取网络统计失败: ${e.message}"))
        }
    }
    
    override suspend fun validateLinkConfiguration(
        parentLedgerId: String,
        childLedgerId: String,
        syncMode: SyncMode
    ): BaseResult<Unit> {
        return try {
            // 检查记账簿是否相同
            if (parentLedgerId == childLedgerId) {
                return BaseResult.Error(Exception("不能将记账簿与自己建立联动关系"))
            }
            
            // 检查记账簿是否存在
            val parentLedger = ledgerDao.getLedgerById(parentLedgerId)
            val childLedger = ledgerDao.getLedgerById(childLedgerId)
            
            if (parentLedger == null) {
                return BaseResult.Error(Exception("父记账簿不存在"))
            }
            
            if (childLedger == null) {
                return BaseResult.Error(Exception("子记账簿不存在"))
            }
            
            // 检查记账簿是否激活
            if (!parentLedger.isActive) {
                return BaseResult.Error(Exception("父记账簿未激活"))
            }
            
            if (!childLedger.isActive) {
                return BaseResult.Error(Exception("子记账簿未激活"))
            }
            
            // 检查是否属于同一用户
            if (parentLedger.userId != childLedger.userId) {
                return BaseResult.Error(Exception("不能在不同用户的记账簿间建立联动关系"))
            }
            
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("验证联动配置失败: ${e.message}"))
        }
    }
    
    /**
     * 获取联动交易数量（占位实现）
     */
    private suspend fun getLinkedTransactionCount(linkId: String): Int {
        // TODO: 实现联动交易数量统计
        return 0
    }
}

/**
 * Entity到Domain的转换扩展函数
 */
private fun LedgerLinkEntity.toDomain(): LedgerLink {
    return LedgerLink(
        id = id,
        parentLedgerId = parentLedgerId,
        childLedgerId = childLedgerId,
        syncMode = SyncMode.fromString(syncMode),
        autoSyncEnabled = autoSyncEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive
    )
}

/**
 * Domain到Entity的转换扩展函数
 */
private fun LedgerLinkEntity.Companion.fromDomain(link: LedgerLink): LedgerLinkEntity {
    return LedgerLinkEntity(
        id = link.id,
        parentLedgerId = link.parentLedgerId,
        childLedgerId = link.childLedgerId,
        syncMode = link.syncMode.name,
        autoSyncEnabled = link.autoSyncEnabled,
        createdAt = link.createdAt,
        updatedAt = link.updatedAt,
        isActive = link.isActive
    )
}