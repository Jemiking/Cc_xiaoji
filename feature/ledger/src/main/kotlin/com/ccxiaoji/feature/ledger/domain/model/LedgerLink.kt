package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 记账簿联动关系领域模型
 * 
 * 定义记账簿之间的联动同步关系，支持多种同步模式。
 * 当一个记账簿中的交易发生变化时，根据联动规则自动同步到相关记账簿。
 */
data class LedgerLink(
    val id: String,
    val parentLedgerId: String, // 父记账簿ID（通常是总记账簿）
    val childLedgerId: String,  // 子记账簿ID
    val syncMode: SyncMode = SyncMode.BIDIRECTIONAL,
    val autoSyncEnabled: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
    val isActive: Boolean = true
) {
    
    /**
     * 检查是否需要从父记账簿同步到子记账簿
     */
    fun shouldSyncFromParent(): Boolean {
        return isActive && autoSyncEnabled && 
               (syncMode == SyncMode.BIDIRECTIONAL || syncMode == SyncMode.PARENT_TO_CHILD)
    }
    
    /**
     * 检查是否需要从子记账簿同步到父记账簿
     */
    fun shouldSyncFromChild(): Boolean {
        return isActive && autoSyncEnabled && 
               (syncMode == SyncMode.BIDIRECTIONAL || syncMode == SyncMode.CHILD_TO_PARENT)
    }
    
    /**
     * 获取其他关联的记账簿ID
     */
    fun getOtherLedgerId(currentLedgerId: String): String? {
        return when (currentLedgerId) {
            parentLedgerId -> childLedgerId
            childLedgerId -> parentLedgerId
            else -> null
        }
    }
    
    /**
     * 检查指定记账簿是否是父记账簿
     */
    fun isParentLedger(ledgerId: String): Boolean = ledgerId == parentLedgerId
    
    /**
     * 检查指定记账簿是否是子记账簿
     */
    fun isChildLedger(ledgerId: String): Boolean = ledgerId == childLedgerId
}

/**
 * 同步模式枚举
 */
enum class SyncMode(val displayName: String, val description: String) {
    /**
     * 双向同步：父子记账簿之间的交易会相互同步
     */
    BIDIRECTIONAL(
        "双向同步",
        "父子记账簿之间的交易会相互同步，保持数据一致"
    ),
    
    /**
     * 仅父到子：只从父记账簿同步到子记账簿
     */
    PARENT_TO_CHILD(
        "仅父到子",
        "只将父记账簿的交易同步到子记账簿，子记账簿的变化不影响父记账簿"
    ),
    
    /**
     * 仅子到父：只从子记账簿同步到父记账簿
     */
    CHILD_TO_PARENT(
        "仅子到父",
        "只将子记账簿的交易同步到父记账簿，父记账簿的变化不影响子记账簿"
    );
    
    companion object {
        fun fromString(value: String): SyncMode {
            return values().find { it.name == value } ?: BIDIRECTIONAL
        }
    }
}

/**
 * 记账簿联动状态
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