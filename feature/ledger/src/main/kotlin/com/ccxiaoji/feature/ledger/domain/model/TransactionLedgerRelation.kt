package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 交易与记账簿关联关系领域模型
 * 
 * 记录一个交易在多个记账簿中的关联关系，支持:
 * - 原始交易（PRIMARY）
 * - 从父记账簿同步的交易（SYNCED_FROM_PARENT）
 * - 从子记账簿同步的交易（SYNCED_FROM_CHILD）
 */
data class TransactionLedgerRelation(
    val id: String,
    val transactionId: String,
    val ledgerId: String,
    val relationType: RelationType = RelationType.PRIMARY,
    val syncSourceLedgerId: String? = null, // 同步来源记账簿ID，null表示原始交易
    val createdAt: Instant
) {
    
    /**
     * 检查是否是原始交易关系
     */
    fun isPrimaryRelation(): Boolean = relationType == RelationType.PRIMARY
    
    /**
     * 检查是否是同步交易关系
     */
    fun isSyncedRelation(): Boolean = relationType != RelationType.PRIMARY
    
    /**
     * 检查是否从指定记账簿同步而来
     */
    fun isSyncedFrom(sourceLedgerId: String): Boolean {
        return isSyncedRelation() && syncSourceLedgerId == sourceLedgerId
    }
    
    /**
     * 获取关系类型的显示名称
     */
    fun getRelationDisplayName(): String = relationType.displayName
    
    /**
     * 获取同步信息描述
     */
    fun getSyncDescription(): String {
        return when {
            isPrimaryRelation() -> "原始交易"
            syncSourceLedgerId != null -> "同步自其他记账簿"
            else -> "同步交易"
        }
    }
}

/**
 * 交易关系类型枚举
 */
enum class RelationType(val displayName: String, val description: String) {
    /**
     * 主要关系：交易的原始记账簿
     */
    PRIMARY(
        "原始交易",
        "交易最初创建的记账簿，具有完整的编辑权限"
    ),
    
    /**
     * 从父记账簿同步：从父记账簿同步到子记账簿的交易
     */
    SYNCED_FROM_PARENT(
        "从父记账簿同步",
        "从父记账簿自动同步到当前记账簿的交易副本"
    ),
    
    /**
     * 从子记账簿同步：从子记账簿同步到父记账簿的交易
     */
    SYNCED_FROM_CHILD(
        "从子记账簿同步",
        "从子记账簿自动同步到当前记账簿的交易副本"
    );
    
    companion object {
        fun fromString(value: String): RelationType {
            return values().find { it.name == value } ?: PRIMARY
        }
    }
}

/**
 * 交易在多记账簿中的完整关联信息
 */
data class TransactionMultiLedgerInfo(
    val transactionId: String,
    val relations: List<TransactionLedgerRelation>
) {
    
    /**
     * 获取原始记账簿关系
     */
    fun getPrimaryRelation(): TransactionLedgerRelation? {
        return relations.find { it.isPrimaryRelation() }
    }
    
    /**
     * 获取同步关系列表
     */
    fun getSyncedRelations(): List<TransactionLedgerRelation> {
        return relations.filter { it.isSyncedRelation() }
    }
    
    /**
     * 获取交易存在的所有记账簿ID
     */
    fun getAllLedgerIds(): Set<String> {
        return relations.map { it.ledgerId }.toSet()
    }
    
    /**
     * 检查交易是否存在于指定记账簿
     */
    fun existsInLedger(ledgerId: String): Boolean {
        return relations.any { it.ledgerId == ledgerId }
    }
    
    /**
     * 获取指定记账簿中的关系
     */
    fun getRelationInLedger(ledgerId: String): TransactionLedgerRelation? {
        return relations.find { it.ledgerId == ledgerId }
    }
    
    /**
     * 获取从指定记账簿同步的关系列表
     */
    fun getRelationsSyncedFrom(sourceLedgerId: String): List<TransactionLedgerRelation> {
        return relations.filter { it.isSyncedFrom(sourceLedgerId) }
    }
}

/**
 * 记账簿同步统计信息
 */
data class LedgerSyncStats(
    val ledgerId: String,
    val totalTransactions: Int = 0,
    val primaryTransactions: Int = 0, // 原始交易数量
    val syncedFromParent: Int = 0,    // 从父记账簿同步的交易数量
    val syncedFromChild: Int = 0,     // 从子记账簿同步的交易数量
    val lastSyncTime: Instant? = null
) {
    
    /**
     * 获取同步交易总数
     */
    fun getTotalSyncedTransactions(): Int = syncedFromParent + syncedFromChild
    
    /**
     * 获取同步比例
     */
    fun getSyncPercentage(): Float {
        return if (totalTransactions > 0) {
            getTotalSyncedTransactions().toFloat() / totalTransactions.toFloat()
        } else 0f
    }
}