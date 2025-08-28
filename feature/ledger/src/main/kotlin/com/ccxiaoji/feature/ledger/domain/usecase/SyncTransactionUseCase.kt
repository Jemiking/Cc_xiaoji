package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.LedgerLink
import com.ccxiaoji.feature.ledger.domain.model.RelationType
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation
import com.ccxiaoji.feature.ledger.domain.model.TransactionWithLedgers
import com.ccxiaoji.feature.ledger.domain.repository.LedgerLinkRepository
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * 交易同步用例
 * 
 * 处理记账簿间的交易同步逻辑，包括自动同步和手动同步
 */
class SyncTransactionUseCase @Inject constructor(
    private val ledgerLinkRepository: LedgerLinkRepository,
    private val transactionLedgerRelationDao: TransactionLedgerRelationDao
) {
    
    /**
     * 同步交易到相关记账簿
     * 当在某个记账簿中创建或修改交易时调用
     */
    suspend fun syncTransactionToLinkedLedgers(
        transaction: Transaction,
        sourceLedgerId: String
    ): BaseResult<List<TransactionLedgerRelation>> {
        
        if (transaction.id.isBlank() || sourceLedgerId.isBlank()) {
            return BaseResult.Error(Exception("交易ID和源记账簿ID不能为空"))
        }
        
        try {
            // 获取需要同步的目标记账簿
            val outgoingLinks = ledgerLinkRepository.getOutgoingSyncLinks(sourceLedgerId).first()
            val targetLedgerIds = outgoingLinks.mapNotNull { link ->
                link.getOtherLedgerId(sourceLedgerId)
            }
            
            if (targetLedgerIds.isEmpty()) {
                return BaseResult.Success(emptyList())
            }
            
            val syncedRelations = mutableListOf<TransactionLedgerRelation>()
            
            // 为每个目标记账簿创建同步关系
            for (targetLedgerId in targetLedgerIds) {
                val link = outgoingLinks.find { 
                    it.getOtherLedgerId(sourceLedgerId) == targetLedgerId 
                }
                
                if (link != null && link.autoSyncEnabled) {
                    val relationType = when {
                        link.isParentLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_PARENT
                        link.isChildLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_CHILD
                        else -> RelationType.SYNCED_FROM_PARENT // 默认
                    }
                    
                    val syncRelation = createSyncRelation(
                        transactionId = transaction.id,
                        targetLedgerId = targetLedgerId,
                        sourceLedgerId = sourceLedgerId,
                        relationType = relationType
                    )
                    
                    transactionLedgerRelationDao.insertRelation(syncRelation.toEntity())
                    syncedRelations.add(syncRelation)
                }
            }
            
            return BaseResult.Success(syncedRelations)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("同步交易失败: ${e.message}"))
        }
    }
    
    /**
     * 移除交易的同步关系
     * 当删除交易时调用
     */
    suspend fun removeSyncedTransaction(transactionId: String): BaseResult<Unit> {
        if (transactionId.isBlank()) {
            return BaseResult.Error(Exception("交易ID不能为空"))
        }
        
        try {
            // 删除所有同步关系，保留PRIMARY关系
            val allRelations = transactionLedgerRelationDao.getRelationsForTransaction(transactionId).first()
            val syncedRelations = allRelations.filter { 
                RelationType.fromString(it.relationType) != RelationType.PRIMARY 
            }
            
            syncedRelations.forEach { relation ->
                transactionLedgerRelationDao.deleteRelation(relation)
            }
            
            return BaseResult.Success(Unit)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("移除同步交易失败: ${e.message}"))
        }
    }
    
    /**
     * 更新同步交易
     * 当修改交易时调用
     */
    suspend fun updateSyncedTransaction(
        transaction: Transaction,
        sourceLedgerId: String
    ): BaseResult<Unit> {
        if (transaction.id.isBlank() || sourceLedgerId.isBlank()) {
            return BaseResult.Error(Exception("交易ID和源记账簿ID不能为空"))
        }
        
        try {
            // 先移除现有的同步关系
            val removeResult = removeSyncedTransaction(transaction.id)
            if (removeResult is BaseResult.Error) {
                return removeResult
            }
            
            // 重新创建同步关系
            val syncResult = syncTransactionToLinkedLedgers(transaction, sourceLedgerId)
            if (syncResult is BaseResult.Error) {
                return BaseResult.Error(syncResult.exception)
            }
            
            return BaseResult.Success(Unit)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("更新同步交易失败: ${e.message}"))
        }
    }
    
    /**
     * 手动同步特定交易到指定记账簿
     */
    suspend fun manualSyncTransaction(
        transactionId: String,
        sourceLedgerId: String,
        targetLedgerId: String
    ): BaseResult<TransactionLedgerRelation> {
        
        if (transactionId.isBlank() || sourceLedgerId.isBlank() || targetLedgerId.isBlank()) {
            return BaseResult.Error(Exception("交易ID、源记账簿ID和目标记账簿ID不能为空"))
        }
        
        if (sourceLedgerId == targetLedgerId) {
            return BaseResult.Error(Exception("源记账簿和目标记账簿不能相同"))
        }
        
        try {
            // 检查是否存在联动关系
            val linkResult = ledgerLinkRepository.getLinkBetweenLedgers(sourceLedgerId, targetLedgerId)
            if (linkResult is BaseResult.Error) {
                return BaseResult.Error(Exception("获取联动关系失败: ${linkResult.exception.message}"))
            }
            
            val link = (linkResult as BaseResult.Success).data
            if (link == null) {
                return BaseResult.Error(Exception("记账簿之间没有联动关系"))
            }
            
            // 检查是否已存在同步关系
            val existingRelation = transactionLedgerRelationDao.getRelationForTransactionInLedger(
                transactionId, targetLedgerId
            )
            if (existingRelation != null) {
                return BaseResult.Error(Exception("交易已在目标记账簿中"))
            }
            
            // 创建手动同步关系
            val relationType = when {
                link.isParentLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_PARENT
                link.isChildLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_CHILD
                else -> RelationType.SYNCED_FROM_PARENT
            }
            
            val syncRelation = createSyncRelation(
                transactionId = transactionId,
                targetLedgerId = targetLedgerId,
                sourceLedgerId = sourceLedgerId,
                relationType = relationType
            )
            
            transactionLedgerRelationDao.insertRelation(syncRelation.toEntity())
            
            return BaseResult.Success(syncRelation)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("手动同步交易失败: ${e.message}"))
        }
    }
    
    /**
     * 批量同步记账簿中的所有交易
     */
    suspend fun batchSyncLedgerTransactions(
        sourceLedgerId: String,
        targetLedgerId: String
    ): BaseResult<Int> {
        
        if (sourceLedgerId.isBlank() || targetLedgerId.isBlank()) {
            return BaseResult.Error(Exception("源记账簿ID和目标记账簿ID不能为空"))
        }
        
        try {
            // 检查联动关系
            val linkResult = ledgerLinkRepository.getLinkBetweenLedgers(sourceLedgerId, targetLedgerId)
            if (linkResult is BaseResult.Error) {
                return BaseResult.Error(Exception("获取联动关系失败: ${linkResult.exception.message}"))
            }
            
            val link = (linkResult as BaseResult.Success).data
            if (link == null) {
                return BaseResult.Error(Exception("记账簿之间没有联动关系"))
            }
            
            // 获取源记账簿中的原始交易
            val sourceRelations = transactionLedgerRelationDao.getRelationsByType(
                sourceLedgerId, RelationType.PRIMARY.name
            ).first()
            
            var syncedCount = 0
            
            // 为每个原始交易创建同步关系
            for (relation in sourceRelations) {
                val existingRelation = transactionLedgerRelationDao.getRelationForTransactionInLedger(
                    relation.transactionId, targetLedgerId
                )
                
                if (existingRelation == null) {
                    val relationType = when {
                        link.isParentLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_PARENT
                        link.isChildLedger(sourceLedgerId) -> RelationType.SYNCED_FROM_CHILD
                        else -> RelationType.SYNCED_FROM_PARENT
                    }
                    
                    val syncRelation = createSyncRelation(
                        transactionId = relation.transactionId,
                        targetLedgerId = targetLedgerId,
                        sourceLedgerId = sourceLedgerId,
                        relationType = relationType
                    )
                    
                    transactionLedgerRelationDao.insertRelation(syncRelation.toEntity())
                    syncedCount++
                }
            }
            
            return BaseResult.Success(syncedCount)
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("批量同步失败: ${e.message}"))
        }
    }
    
    /**
     * 获取交易的所有同步信息
     */
    suspend fun getTransactionSyncInfo(transactionId: String): BaseResult<TransactionWithLedgers> {
        if (transactionId.isBlank()) {
            return BaseResult.Error(Exception("交易ID不能为空"))
        }
        
        try {
            val relations = transactionLedgerRelationDao.getRelationsForTransaction(transactionId).first()
            val domainRelations = relations.map { it.toDomain() }
            
            // TODO: 获取完整的Transaction对象和相关的Ledger信息
            // 这里需要注入TransactionRepository和LedgerRepository
            
            return BaseResult.Error(Exception("获取交易同步信息功能尚未完全实现"))
            
        } catch (e: Exception) {
            return BaseResult.Error(Exception("获取交易同步信息失败: ${e.message}"))
        }
    }
    
    /**
     * 检查交易是否需要同步
     */
    suspend fun shouldSyncTransaction(
        transactionId: String,
        sourceLedgerId: String
    ): BaseResult<Boolean> {
        try {
            val outgoingLinks = ledgerLinkRepository.getOutgoingSyncLinks(sourceLedgerId).first()
            return BaseResult.Success(outgoingLinks.any { it.autoSyncEnabled })
        } catch (e: Exception) {
            return BaseResult.Error(Exception("检查同步需求失败: ${e.message}"))
        }
    }
    
    /**
     * 创建同步关系对象
     */
    private fun createSyncRelation(
        transactionId: String,
        targetLedgerId: String,
        sourceLedgerId: String,
        relationType: RelationType
    ): TransactionLedgerRelation {
        return TransactionLedgerRelation(
            id = UUID.randomUUID().toString(),
            transactionId = transactionId,
            ledgerId = targetLedgerId,
            relationType = relationType,
            syncSourceLedgerId = sourceLedgerId,
            createdAt = Clock.System.now()
        )
    }
}

/**
 * 扩展函数：转换为Entity
 */
private fun TransactionLedgerRelation.toEntity(): com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity {
    return com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity(
        id = id,
        transactionId = transactionId,
        ledgerId = ledgerId,
        relationType = relationType.name,
        syncSourceLedgerId = syncSourceLedgerId,
        createdAt = createdAt
    )
}

/**
 * 扩展函数：转换为Domain
 */
private fun com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity.toDomain(): TransactionLedgerRelation {
    return TransactionLedgerRelation(
        id = id,
        transactionId = transactionId,
        ledgerId = ledgerId,
        relationType = RelationType.fromString(relationType),
        syncSourceLedgerId = syncSourceLedgerId,
        createdAt = createdAt
    )
}