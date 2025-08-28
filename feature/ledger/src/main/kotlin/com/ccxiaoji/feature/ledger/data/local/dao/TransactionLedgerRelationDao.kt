package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.TransactionLedgerRelationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 交易与记账簿关联关系数据访问对象
 */
@Dao
interface TransactionLedgerRelationDao {
    
    /**
     * 获取交易在所有记账簿中的关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId 
        ORDER BY created_at ASC
    """)
    fun getRelationsForTransaction(transactionId: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 获取指定记账簿中的所有交易关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE ledger_id = :ledgerId 
        ORDER BY created_at DESC
    """)
    fun getRelationsForLedger(ledgerId: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 获取指定记账簿中指定类型的交易关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE ledger_id = :ledgerId AND relation_type = :relationType 
        ORDER BY created_at DESC
    """)
    fun getRelationsByType(ledgerId: String, relationType: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 获取交易在指定记账簿中的关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId AND ledger_id = :ledgerId 
        LIMIT 1
    """)
    suspend fun getRelationForTransactionInLedger(transactionId: String, ledgerId: String): TransactionLedgerRelationEntity?
    
    /**
     * 根据ID获取关联关系
     */
    @Query("SELECT * FROM transaction_ledger_relations WHERE id = :relationId")
    suspend fun getRelationById(relationId: String): TransactionLedgerRelationEntity?
    
    /**
     * 获取从指定记账簿同步的所有关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE sync_source_ledger_id = :sourceLedgerId 
        ORDER BY created_at DESC
    """)
    fun getRelationsSyncedFrom(sourceLedgerId: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 获取同步到指定记账簿的所有关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE ledger_id = :targetLedgerId AND sync_source_ledger_id IS NOT NULL 
        ORDER BY created_at DESC
    """)
    fun getRelationsSyncedTo(targetLedgerId: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 检查交易是否存在于指定记账簿
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId AND ledger_id = :ledgerId
    """)
    suspend fun isTransactionInLedger(transactionId: String, ledgerId: String): Boolean
    
    /**
     * 获取交易的原始记账簿关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId AND relation_type = 'PRIMARY' 
        LIMIT 1
    """)
    suspend fun getPrimaryRelation(transactionId: String): TransactionLedgerRelationEntity?
    
    /**
     * 获取交易的所有同步关联关系
     */
    @Query("""
        SELECT * FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId AND relation_type != 'PRIMARY' 
        ORDER BY created_at ASC
    """)
    fun getSyncedRelations(transactionId: String): Flow<List<TransactionLedgerRelationEntity>>
    
    /**
     * 插入关联关系
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: TransactionLedgerRelationEntity): Long
    
    /**
     * 批量插入关联关系
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<TransactionLedgerRelationEntity>)
    
    /**
     * 更新关联关系
     */
    @Update
    suspend fun updateRelation(relation: TransactionLedgerRelationEntity)
    
    /**
     * 删除关联关系
     */
    @Delete
    suspend fun deleteRelation(relation: TransactionLedgerRelationEntity)
    
    /**
     * 删除交易的所有关联关系
     */
    @Query("DELETE FROM transaction_ledger_relations WHERE transaction_id = :transactionId")
    suspend fun deleteAllRelationsForTransaction(transactionId: String)
    
    /**
     * 删除指定记账簿的所有关联关系
     */
    @Query("DELETE FROM transaction_ledger_relations WHERE ledger_id = :ledgerId")
    suspend fun deleteAllRelationsForLedger(ledgerId: String)
    
    /**
     * 删除从指定记账簿同步的所有关联关系
     */
    @Query("DELETE FROM transaction_ledger_relations WHERE sync_source_ledger_id = :sourceLedgerId")
    suspend fun deleteAllRelationsSyncedFrom(sourceLedgerId: String)
    
    /**
     * 删除交易在指定记账簿中的关联关系
     */
    @Query("""
        DELETE FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId AND ledger_id = :ledgerId
    """)
    suspend fun deleteRelationForTransactionInLedger(transactionId: String, ledgerId: String)
    
    /**
     * 获取记账簿中的交易数量统计
     */
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN relation_type = 'PRIMARY' THEN 1 END) as primary_count,
            COUNT(CASE WHEN relation_type = 'SYNCED_FROM_PARENT' THEN 1 END) as synced_from_parent,
            COUNT(CASE WHEN relation_type = 'SYNCED_FROM_CHILD' THEN 1 END) as synced_from_child
        FROM transaction_ledger_relations 
        WHERE ledger_id = :ledgerId
    """)
    suspend fun getTransactionStatsForLedger(ledgerId: String): TransactionLedgerStats
    
    /**
     * 获取交易存在的记账簿数量
     */
    @Query("""
        SELECT COUNT(DISTINCT ledger_id) FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId
    """)
    suspend fun getTransactionLedgerCount(transactionId: String): Int
    
    /**
     * 获取交易存在的所有记账簿ID
     */
    @Query("""
        SELECT DISTINCT ledger_id FROM transaction_ledger_relations 
        WHERE transaction_id = :transactionId 
        ORDER BY created_at ASC
    """)
    suspend fun getTransactionLedgerIds(transactionId: String): List<String>
    
    /**
     * 创建PRIMARY关联关系
     */
    suspend fun createPrimaryRelation(
        transactionId: String,
        ledgerId: String,
        createdAt: Long
    ) {
        val relationId = "${transactionId}_${ledgerId}_primary"
        val relation = TransactionLedgerRelationEntity(
            id = relationId,
            transactionId = transactionId,
            ledgerId = ledgerId,
            relationType = "PRIMARY",
            syncSourceLedgerId = null,
            createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt)
        )
        insertRelation(relation)
    }
    
    /**
     * 创建同步关联关系
     */
    suspend fun createSyncRelation(
        transactionId: String,
        targetLedgerId: String,
        relationType: String,
        sourceLedgerId: String,
        createdAt: Long
    ) {
        val relationId = "${transactionId}_${targetLedgerId}_sync"
        val relation = TransactionLedgerRelationEntity(
            id = relationId,
            transactionId = transactionId,
            ledgerId = targetLedgerId,
            relationType = relationType,
            syncSourceLedgerId = sourceLedgerId,
            createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt)
        )
        insertRelation(relation)
    }
    
    /**
     * 批量创建同步关联关系
     */
    @Transaction
    suspend fun createSyncRelations(
        transactionId: String,
        syncTargets: List<SyncTarget>,
        createdAt: Long
    ) {
        val relations = syncTargets.map { target ->
            TransactionLedgerRelationEntity(
                id = "${transactionId}_${target.targetLedgerId}_sync",
                transactionId = transactionId,
                ledgerId = target.targetLedgerId,
                relationType = target.relationType,
                syncSourceLedgerId = target.sourceLedgerId,
                createdAt = kotlinx.datetime.Instant.fromEpochMilliseconds(createdAt)
            )
        }
        insertRelations(relations)
    }
}

/**
 * 交易记账簿统计数据
 */
data class TransactionLedgerStats(
    val total: Int,
    val primary_count: Int,
    val synced_from_parent: Int,
    val synced_from_child: Int
)

/**
 * 同步目标信息
 */
data class SyncTarget(
    val targetLedgerId: String,
    val relationType: String,
    val sourceLedgerId: String
)