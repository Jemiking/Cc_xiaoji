package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 账本联动关系领域模型
 *
 * 定义账本之间的联动同步关系，支持多种同步模式。
 * 当一个账本中的交易发生变化时，根据联动规则自动同步到相关账本。
 */
data class LedgerLink(
    val id: String,
    val parentLedgerId: String, // 父账本 ID（通常是总账本）
    val childLedgerId: String,  // 子账本 ID
    val syncMode: SyncMode = SyncMode.BIDIRECTIONAL,
    val autoSyncEnabled: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isActive: Boolean = true
) {
    
    /**
     * 检查是否需要从父账本同步到子账本
     */
    fun shouldSyncFromParent(): Boolean {
        return isActive && autoSyncEnabled && 
               (syncMode == SyncMode.BIDIRECTIONAL || syncMode == SyncMode.PARENT_TO_CHILD)
    }
    
    /**
     * 检查是否需要从子账本同步到父账本
     */
    fun shouldSyncFromChild(): Boolean {
        return isActive && autoSyncEnabled && 
               (syncMode == SyncMode.BIDIRECTIONAL || syncMode == SyncMode.CHILD_TO_PARENT)
    }
    
    /**
     * 获取其他关联的账本 ID
     */
    fun getOtherLedgerId(currentLedgerId: String): String? {
        return when (currentLedgerId) {
            parentLedgerId -> childLedgerId
            childLedgerId -> parentLedgerId
            else -> null
        }
    }
    
    /**
     * 检查指定账本是否是父账本
     */
    fun isParentLedger(ledgerId: String): Boolean = ledgerId == parentLedgerId
    
    /**
     * 检查指定账本是否是子账本
     */
    fun isChildLedger(ledgerId: String): Boolean = ledgerId == childLedgerId
}

/**
 * 同步模式枚举
 */
enum class SyncMode(val displayName: String, val description: String) {
    /**
     * 双向同步：父子账本之间的交易会相互同步
     */
    BIDIRECTIONAL(
        "双向同步",
        "父子账本之间的交易会相互同步，保持数据一致"
    ),
    
    /**
     * 仅父到子：只从父账本同步到子账本
     */
    PARENT_TO_CHILD(
        "仅父到子",
        "只将父账本的交易同步到子账本，子账本的变化不影响父账本"
    ),
    
    /**
     * 仅子到父：只从子账本同步到父账本
     */
    CHILD_TO_PARENT(
        "仅子到父",
        "只将子账本的交易同步到父账本，父账本的变化不影响子账本"
    );
    
    companion object {
        fun fromString(value: String): SyncMode {
            return values().find { it.name == value } ?: BIDIRECTIONAL
        }
    }
}

/**
 * 账本联动状态
 */
data class LedgerLinkStatus(
    val link: LedgerLink,
    val parentLedger: Ledger,
    val childLedger: Ledger,
    val linkedTransactionCount: Int = 0,
    val lastSyncTime: Instant? = null
) {
    /**
     * 获取联动关系的显示标题
     */
    fun getDisplayTitle(): String {
        return "${parentLedger.name} ↔ ${childLedger.name}"
    }
    
    /**
     * 获取同步状态描述
     */
    fun getSyncStatusDescription(): String {
        return when {
            !link.isActive -> "已禁用"
            !link.autoSyncEnabled -> "手动同步"
            lastSyncTime == null -> "未同步"
            else -> "已同步"
        }
    }
}
