package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 包含多记账簿关联信息的交易模型
 * 
 * 这个模型扩展了基础Transaction，提供多记账簿联动的完整信息。
 * 支持查询交易在所有记账簿中的存在状态和同步关系。
 */
data class TransactionWithLedgers(
    val transaction: Transaction,
    val ledgerRelations: List<TransactionLedgerRelation> = emptyList(),
    val linkedLedgers: List<Ledger> = emptyList() // 关联的记账簿列表
) {
    
    /**
     * 获取原始记账簿ID（交易首次创建的记账簿）
     */
    fun getPrimaryLedgerId(): String? {
        return ledgerRelations.find { it.isPrimaryRelation() }?.ledgerId
    }
    
    /**
     * 获取原始记账簿
     */
    fun getPrimaryLedger(): Ledger? {
        val primaryLedgerId = getPrimaryLedgerId()
        return linkedLedgers.find { it.id == primaryLedgerId }
    }
    
    /**
     * 获取所有关联的记账簿ID
     */
    fun getAllLinkedLedgerIds(): Set<String> {
        return ledgerRelations.map { it.ledgerId }.toSet()
    }
    
    /**
     * 检查交易是否存在于指定记账簿
     */
    fun existsInLedger(ledgerId: String): Boolean {
        return ledgerRelations.any { it.ledgerId == ledgerId }
    }
    
    /**
     * 获取在指定记账簿中的关系类型
     */
    fun getRelationTypeInLedger(ledgerId: String): RelationType? {
        return ledgerRelations.find { it.ledgerId == ledgerId }?.relationType
    }
    
    /**
     * 检查交易是否是原始交易（在指定记账簿中）
     */
    fun isPrimaryInLedger(ledgerId: String): Boolean {
        return getRelationTypeInLedger(ledgerId) == RelationType.PRIMARY
    }
    
    /**
     * 检查交易是否是同步交易（在指定记账簿中）
     */
    fun isSyncedInLedger(ledgerId: String): Boolean {
        val relationType = getRelationTypeInLedger(ledgerId)
        return relationType != null && relationType != RelationType.PRIMARY
    }
    
    /**
     * 获取同步来源记账簿ID（在指定记账簿中）
     */
    fun getSyncSourceLedgerId(ledgerId: String): String? {
        return ledgerRelations.find { it.ledgerId == ledgerId }?.syncSourceLedgerId
    }
    
    /**
     * 获取同步的记账簿列表（不包括原始记账簿）
     */
    fun getSyncedLedgers(): List<Ledger> {
        val syncedLedgerIds = ledgerRelations
            .filter { it.isSyncedRelation() }
            .map { it.ledgerId }
            .toSet()
        return linkedLedgers.filter { it.id in syncedLedgerIds }
    }
    
    /**
     * 获取交易的完整同步信息
     */
    fun getSyncInfo(): TransactionSyncInfo {
        val primaryLedger = getPrimaryLedger()
        val syncedLedgers = getSyncedLedgers()
        
        return TransactionSyncInfo(
            transactionId = transaction.id,
            primaryLedger = primaryLedger,
            syncedLedgers = syncedLedgers,
            totalLedgerCount = linkedLedgers.size,
            syncedCount = syncedLedgers.size
        )
    }
    
    /**
     * 检查是否可以在指定记账簿中编辑
     * 通常只有原始记账簿允许编辑
     */
    fun canEditInLedger(ledgerId: String): Boolean {
        return isPrimaryInLedger(ledgerId)
    }
    
    /**
     * 检查是否可以在指定记账簿中删除
     */
    fun canDeleteInLedger(ledgerId: String): Boolean {
        return isPrimaryInLedger(ledgerId)
    }
}

/**
 * 交易同步信息汇总
 */
data class TransactionSyncInfo(
    val transactionId: String,
    val primaryLedger: Ledger?,
    val syncedLedgers: List<Ledger>,
    val totalLedgerCount: Int,
    val syncedCount: Int
) {
    /**
     * 获取同步状态描述
     */
    fun getSyncStatusDescription(): String {
        return when {
            syncedCount == 0 -> "未同步"
            syncedCount == 1 -> "已同步到 1 个记账簿"
            else -> "已同步到 $syncedCount 个记账簿"
        }
    }
    
    /**
     * 检查是否有同步
     */
    fun hasSyncedLedgers(): Boolean = syncedCount > 0
    
    /**
     * 获取同步记账簿名称列表
     */
    fun getSyncedLedgerNames(): List<String> {
        return syncedLedgers.map { it.name }
    }
}

/**
 * 记账簿视角的交易信息
 * 
 * 从特定记账簿的角度查看交易，包含该记账簿特有的关系信息
 */
data class LedgerViewTransaction(
    val transaction: Transaction,
    val relationInThisLedger: TransactionLedgerRelation,
    val syncSourceLedger: Ledger? = null, // 如果是同步交易，这是来源记账簿
    val allLinkedLedgerCount: Int = 0 // 交易存在的总记账簿数量
) {
    
    /**
     * 获取在当前记账簿中的显示标题
     */
    fun getDisplayTitle(): String {
        return when (relationInThisLedger.relationType) {
            RelationType.PRIMARY -> transaction.note ?: "交易记录"
            RelationType.SYNCED_FROM_PARENT -> "[从父记账簿同步] ${transaction.note ?: "交易记录"}"
            RelationType.SYNCED_FROM_CHILD -> "[从子记账簿同步] ${transaction.note ?: "交易记录"}"
        }
    }
    
    /**
     * 获取同步标识符
     */
    fun getSyncBadge(): String? {
        return when (relationInThisLedger.relationType) {
            RelationType.PRIMARY -> null
            RelationType.SYNCED_FROM_PARENT -> "父→子"
            RelationType.SYNCED_FROM_CHILD -> "子→父"
        }
    }
    
    /**
     * 检查是否可以编辑
     */
    fun canEdit(): Boolean = relationInThisLedger.isPrimaryRelation()
    
    /**
     * 检查是否可以删除
     */
    fun canDelete(): Boolean = relationInThisLedger.isPrimaryRelation()
}