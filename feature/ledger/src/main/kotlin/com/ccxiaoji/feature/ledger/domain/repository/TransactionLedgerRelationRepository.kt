package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.TransactionLedgerRelation
import com.ccxiaoji.feature.ledger.domain.model.RelationType
import kotlinx.coroutines.flow.Flow

/**
 * 交易记账簿关联关系仓库接口
 */
interface TransactionLedgerRelationRepository {
    
    /**
     * 获取交易在所有记账簿中的关联关系
     */
    fun getRelationsByTransaction(transactionId: String): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 获取指定记账簿中的所有交易关联关系
     */
    fun getRelationsByLedger(ledgerId: String): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 获取指定记账簿中指定类型的交易关联关系
     */
    fun getRelationsByType(ledgerId: String, relationType: RelationType): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 获取交易在指定记账簿中的关联关系
     */
    suspend fun getRelationForTransactionInLedger(transactionId: String, ledgerId: String): BaseResult<TransactionLedgerRelation?>
    
    /**
     * 根据ID获取关联关系
     */
    suspend fun getRelationById(relationId: String): BaseResult<TransactionLedgerRelation?>
    
    /**
     * 获取从指定记账簿同步的所有关联关系
     */
    fun getRelationsSyncedFrom(sourceLedgerId: String): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 获取同步到指定记账簿的所有关联关系
     */
    fun getRelationsSyncedTo(targetLedgerId: String): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 检查交易是否存在于指定记账簿
     */
    suspend fun isTransactionInLedger(transactionId: String, ledgerId: String): BaseResult<Boolean>
    
    /**
     * 获取交易的原始记账簿关联关系
     */
    suspend fun getPrimaryRelation(transactionId: String): BaseResult<TransactionLedgerRelation?>
    
    /**
     * 获取交易的所有同步关联关系
     */
    fun getSyncedRelations(transactionId: String): Flow<List<TransactionLedgerRelation>>
    
    /**
     * 创建关联关系
     */
    suspend fun createRelation(relation: TransactionLedgerRelation): BaseResult<Unit>
    
    /**
     * 批量创建关联关系
     */
    suspend fun createRelations(relations: List<TransactionLedgerRelation>): BaseResult<Unit>
    
    /**
     * 更新关联关系
     */
    suspend fun updateRelation(relation: TransactionLedgerRelation): BaseResult<Unit>
    
    /**
     * 删除关联关系
     */
    suspend fun deleteRelation(relationId: String): BaseResult<Unit>
    
    /**
     * 删除交易的所有关联关系
     */
    suspend fun deleteAllRelationsForTransaction(transactionId: String): BaseResult<Unit>
    
    /**
     * 删除指定记账簿的所有关联关系
     */
    suspend fun deleteAllRelationsForLedger(ledgerId: String): BaseResult<Unit>
    
    /**
     * 删除从指定记账簿同步的所有关联关系
     */
    suspend fun deleteAllRelationsSyncedFrom(sourceLedgerId: String): BaseResult<Unit>
    
    /**
     * 删除交易在指定记账簿中的关联关系
     */
    suspend fun deleteRelationForTransactionInLedger(transactionId: String, ledgerId: String): BaseResult<Unit>
    
    /**
     * 获取交易存在的记账簿数量
     */
    suspend fun getTransactionLedgerCount(transactionId: String): BaseResult<Int>
    
    /**
     * 获取交易存在的所有记账簿ID
     */
    suspend fun getTransactionLedgerIds(transactionId: String): BaseResult<List<String>>
    
    /**
     * 创建PRIMARY关联关系
     */
    suspend fun createPrimaryRelation(
        transactionId: String,
        ledgerId: String,
        createdAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    ): BaseResult<Unit>
    
    /**
     * 创建同步关联关系
     */
    suspend fun createSyncRelation(
        transactionId: String,
        targetLedgerId: String,
        relationType: RelationType,
        sourceLedgerId: String,
        createdAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    ): BaseResult<Unit>
    
    /**
     * 批量创建同步关联关系
     */
    suspend fun createSyncRelations(
        transactionId: String,
        syncTargets: List<SyncTargetInfo>,
        createdAt: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    ): BaseResult<Unit>
}

/**
 * 同步目标信息
 */
data class SyncTargetInfo(
    val targetLedgerId: String,
    val relationType: RelationType,
    val sourceLedgerId: String
)