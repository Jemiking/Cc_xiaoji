package com.ccxiaoji.feature.ledger.domain.model

/**
 * 交易同步状态枚举
 */
enum class TransactionSyncType {
    /**
     * 原始交易 - 在当前记账簿中创建的交易
     */
    PRIMARY,
    
    /**
     * 同步交易 - 从其他记账簿同步过来的交易
     */
    SYNCED,
    
    /**
     * 无关交易 - 与当前记账簿无直接关系的交易
     */
    UNRELATED
}

/**
 * 带同步信息的交易模型
 * 用于在UI层显示交易的同步状态和关联信息
 */
data class TransactionWithSyncInfo(
    /**
     * 基础交易信息
     */
    val transaction: Transaction,
    
    /**
     * 在当前记账簿中的同步类型
     */
    val syncType: TransactionSyncType,
    
    /**
     * 源记账簿ID（对于SYNCED类型，表示交易的原始记账簿）
     */
    val sourceLedgerId: String,
    
    /**
     * 目标记账簿ID列表（这笔交易同步到了哪些记账簿）
     */
    val targetLedgerIds: List<String> = emptyList(),
    
    /**
     * 最后同步时间（可选）
     */
    val lastSyncTime: kotlinx.datetime.Instant? = null
) {
    /**
     * 是否为同步交易
     */
    val isSynced: Boolean
        get() = syncType == TransactionSyncType.SYNCED
    
    /**
     * 是否为原始交易
     */
    val isPrimary: Boolean
        get() = syncType == TransactionSyncType.PRIMARY
    
    /**
     * 是否有同步目标
     */
    val hasSyncTargets: Boolean
        get() = targetLedgerIds.isNotEmpty()
    
    /**
     * 同步目标数量
     */
    val syncTargetCount: Int
        get() = targetLedgerIds.size
}