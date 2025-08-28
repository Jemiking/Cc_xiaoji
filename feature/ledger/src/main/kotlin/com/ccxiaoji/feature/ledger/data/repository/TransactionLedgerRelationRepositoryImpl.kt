package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.data.local.dao.TransactionLedgerRelationDao
import com.ccxiaoji.feature.ledger.data.local.dao.SyncTarget
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity
import com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation
import com.ccxiaoji.feature.ledger.domain.model.RelationType
import com.ccxiaoji.feature.ledger.domain.repository.TransactionLedgerRelationRepository
import com.ccxiaoji.feature.ledger.domain.repository.SyncTargetInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 交易记账簿关联关系仓库实现
 */
@Singleton
class TransactionLedgerRelationRepositoryImpl @Inject constructor(
    private val dao: TransactionLedgerRelationDao
) : TransactionLedgerRelationRepository {
    
    override fun getRelationsByTransaction(transactionId: String): Flow<List<TransactionLedgerRelation>> {
        return dao.getRelationsForTransaction(transactionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRelationsByLedger(ledgerId: String): Flow<List<TransactionLedgerRelation>> {
        return dao.getRelationsForLedger(ledgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRelationsByType(ledgerId: String, relationType: RelationType): Flow<List<TransactionLedgerRelation>> {
        return dao.getRelationsByType(ledgerId, relationType.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getRelationForTransactionInLedger(
        transactionId: String,
        ledgerId: String
    ): BaseResult<TransactionLedgerRelation?> {
        return try {
            val entity = dao.getRelationForTransactionInLedger(transactionId, ledgerId)
            BaseResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取交易记账簿关系失败: ${e.message}"))
        }
    }
    
    override suspend fun getRelationById(relationId: String): BaseResult<TransactionLedgerRelation?> {
        return try {
            val entity = dao.getRelationById(relationId)
            BaseResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取关系记录失败: ${e.message}"))
        }
    }
    
    override fun getRelationsSyncedFrom(sourceLedgerId: String): Flow<List<TransactionLedgerRelation>> {
        return dao.getRelationsSyncedFrom(sourceLedgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRelationsSyncedTo(targetLedgerId: String): Flow<List<TransactionLedgerRelation>> {
        return dao.getRelationsSyncedTo(targetLedgerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun isTransactionInLedger(transactionId: String, ledgerId: String): BaseResult<Boolean> {
        return try {
            val exists = dao.isTransactionInLedger(transactionId, ledgerId)
            BaseResult.Success(exists)
        } catch (e: Exception) {
            BaseResult.Error(Exception("检查交易存在性失败: ${e.message}"))
        }
    }
    
    override suspend fun getPrimaryRelation(transactionId: String): BaseResult<TransactionLedgerRelation?> {
        return try {
            val entity = dao.getPrimaryRelation(transactionId)
            BaseResult.Success(entity?.toDomain())
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取主要关系失败: ${e.message}"))
        }
    }
    
    override fun getSyncedRelations(transactionId: String): Flow<List<TransactionLedgerRelation>> {
        return dao.getSyncedRelations(transactionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun createRelation(relation: TransactionLedgerRelation): BaseResult<Unit> {
        return try {
            val entity = TransactionLedgerRelationEntity.fromDomain(relation)
            dao.insertRelation(entity)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("创建关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun createRelations(relations: List<TransactionLedgerRelation>): BaseResult<Unit> {
        return try {
            val entities = relations.map { TransactionLedgerRelationEntity.fromDomain(it) }
            dao.insertRelations(entities)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("批量创建关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun updateRelation(relation: TransactionLedgerRelation): BaseResult<Unit> {
        return try {
            val entity = TransactionLedgerRelationEntity.fromDomain(relation)
            dao.updateRelation(entity)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("更新关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteRelation(relationId: String): BaseResult<Unit> {
        return try {
            val entity = dao.getRelationById(relationId)
            if (entity != null) {
                dao.deleteRelation(entity)
            }
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteAllRelationsForTransaction(transactionId: String): BaseResult<Unit> {
        return try {
            dao.deleteAllRelationsForTransaction(transactionId)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除交易关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteAllRelationsForLedger(ledgerId: String): BaseResult<Unit> {
        return try {
            dao.deleteAllRelationsForLedger(ledgerId)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除记账簿关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteAllRelationsSyncedFrom(sourceLedgerId: String): BaseResult<Unit> {
        return try {
            dao.deleteAllRelationsSyncedFrom(sourceLedgerId)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除同步关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun deleteRelationForTransactionInLedger(
        transactionId: String,
        ledgerId: String
    ): BaseResult<Unit> {
        return try {
            dao.deleteRelationForTransactionInLedger(transactionId, ledgerId)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("删除特定关系记录失败: ${e.message}"))
        }
    }
    
    override suspend fun getTransactionLedgerCount(transactionId: String): BaseResult<Int> {
        return try {
            val count = dao.getTransactionLedgerCount(transactionId)
            BaseResult.Success(count)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取交易记账簿数量失败: ${e.message}"))
        }
    }
    
    override suspend fun getTransactionLedgerIds(transactionId: String): BaseResult<List<String>> {
        return try {
            val ledgerIds = dao.getTransactionLedgerIds(transactionId)
            BaseResult.Success(ledgerIds)
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取交易记账簿ID列表失败: ${e.message}"))
        }
    }
    
    override suspend fun createPrimaryRelation(
        transactionId: String,
        ledgerId: String,
        createdAt: Long
    ): BaseResult<Unit> {
        return try {
            dao.createPrimaryRelation(transactionId, ledgerId, createdAt)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("创建主要关系失败: ${e.message}"))
        }
    }
    
    override suspend fun createSyncRelation(
        transactionId: String,
        targetLedgerId: String,
        relationType: RelationType,
        sourceLedgerId: String,
        createdAt: Long
    ): BaseResult<Unit> {
        return try {
            dao.createSyncRelation(
                transactionId = transactionId,
                targetLedgerId = targetLedgerId,
                relationType = relationType.name,
                sourceLedgerId = sourceLedgerId,
                createdAt = createdAt
            )
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("创建同步关系失败: ${e.message}"))
        }
    }
    
    override suspend fun createSyncRelations(
        transactionId: String,
        syncTargets: List<SyncTargetInfo>,
        createdAt: Long
    ): BaseResult<Unit> {
        return try {
            val daoSyncTargets = syncTargets.map { target ->
                SyncTarget(
                    targetLedgerId = target.targetLedgerId,
                    relationType = target.relationType.name,
                    sourceLedgerId = target.sourceLedgerId
                )
            }
            dao.createSyncRelations(transactionId, daoSyncTargets, createdAt)
            BaseResult.Success(Unit)
        } catch (e: Exception) {
            BaseResult.Error(Exception("批量创建同步关系失败: ${e.message}"))
        }
    }
}

/**
 * Entity到Domain的转换扩展函数
 */
private fun TransactionLedgerRelationEntity.toDomain(): TransactionLedgerRelation {
    return TransactionLedgerRelation(
        id = id,
        transactionId = transactionId,
        ledgerId = ledgerId,
        relationType = RelationType.fromString(relationType),
        syncSourceLedgerId = syncSourceLedgerId,
        createdAt = createdAt
    )
}

/**
 * Domain到Entity的转换扩展函数
 */
private fun TransactionLedgerRelationEntity.Companion.fromDomain(
    relation: TransactionLedgerRelation
): TransactionLedgerRelationEntity {
    return TransactionLedgerRelationEntity(
        id = relation.id,
        transactionId = relation.transactionId,
        ledgerId = relation.ledgerId,
        relationType = relation.relationType.name,
        syncSourceLedgerId = relation.syncSourceLedgerId,
        createdAt = relation.createdAt
    )
}