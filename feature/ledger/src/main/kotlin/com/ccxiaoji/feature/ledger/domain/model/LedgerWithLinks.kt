package com.ccxiaoji.feature.ledger.domain.model

/**
 * 包含联动关系信息的记账簿模型
 * 
 * 这个模型扩展了基础Ledger，提供联动关系的完整信息。
 * 支持查询记账簿的父子关系、联动配置和同步状态。
 */
data class LedgerWithLinks(
    val ledger: Ledger,
    val parentLinks: List<LedgerLink> = emptyList(), // 作为子记账簿的联动关系
    val childLinks: List<LedgerLink> = emptyList(),  // 作为父记账簿的联动关系
    val linkedLedgers: List<Ledger> = emptyList(),   // 所有相关联的记账簿
    val syncStats: LedgerSyncStats? = null           // 同步统计信息
) {
    
    /**
     * 检查是否有联动关系
     */
    fun hasLinks(): Boolean = parentLinks.isNotEmpty() || childLinks.isNotEmpty()
    
    /**
     * 检查是否是父记账簿（有子记账簿）
     */
    fun isParentLedger(): Boolean = childLinks.isNotEmpty()
    
    /**
     * 检查是否是子记账簿（有父记账簿）
     */
    fun isChildLedger(): Boolean = parentLinks.isNotEmpty()
    
    /**
     * 检查是否是独立记账簿（无联动关系）
     */
    fun isIndependentLedger(): Boolean = !hasLinks()
    
    /**
     * 获取所有活跃的联动关系
     */
    fun getActiveLinks(): List<LedgerLink> {
        return (parentLinks + childLinks).filter { it.isActive }
    }
    
    /**
     * 获取父记账簿列表
     */
    fun getParentLedgers(): List<Ledger> {
        val parentLedgerIds = parentLinks.map { it.parentLedgerId }.toSet()
        return linkedLedgers.filter { it.id in parentLedgerIds }
    }
    
    /**
     * 获取子记账簿列表
     */
    fun getChildLedgers(): List<Ledger> {
        val childLedgerIds = childLinks.map { it.childLedgerId }.toSet()
        return linkedLedgers.filter { it.id in childLedgerIds }
    }
    
    /**
     * 获取指定记账簿的联动关系
     */
    fun getLinkWith(otherLedgerId: String): LedgerLink? {
        return getActiveLinks().find { link ->
            link.getOtherLedgerId(ledger.id) == otherLedgerId
        }
    }
    
    /**
     * 检查与指定记账簿是否有联动关系
     */
    fun isLinkedWith(otherLedgerId: String): Boolean {
        return getLinkWith(otherLedgerId) != null
    }
    
    /**
     * 检查是否需要同步到指定记账簿
     */
    fun shouldSyncTo(targetLedgerId: String): Boolean {
        val link = getLinkWith(targetLedgerId) ?: return false
        
        return when {
            link.isParentLedger(ledger.id) -> link.shouldSyncFromParent()
            link.isChildLedger(ledger.id) -> link.shouldSyncFromChild()
            else -> false
        }
    }
    
    /**
     * 检查是否可以从指定记账簿接收同步
     */
    fun canReceiveSyncFrom(sourceLedgerId: String): Boolean {
        val link = getLinkWith(sourceLedgerId) ?: return false
        
        return when {
            link.isParentLedger(sourceLedgerId) -> link.shouldSyncFromParent()
            link.isChildLedger(sourceLedgerId) -> link.shouldSyncFromChild()
            else -> false
        }
    }
    
    /**
     * 获取所有需要同步的目标记账簿ID
     */
    fun getSyncTargetLedgerIds(): List<String> {
        return getActiveLinks().mapNotNull { link ->
            val otherLedgerId = link.getOtherLedgerId(ledger.id)
            if (otherLedgerId != null && shouldSyncTo(otherLedgerId)) {
                otherLedgerId
            } else null
        }
    }
    
    /**
     * 获取联动关系摘要信息
     */
    fun getLinksSummary(): LedgerLinksSummary {
        val activeLinks = getActiveLinks()
        val parentCount = getParentLedgers().size
        val childCount = getChildLedgers().size
        
        return LedgerLinksSummary(
            ledgerId = ledger.id,
            ledgerName = ledger.name,
            totalLinks = activeLinks.size,
            parentLedgerCount = parentCount,
            childLedgerCount = childCount,
            autoSyncEnabled = activeLinks.any { it.autoSyncEnabled },
            syncStats = syncStats
        )
    }
    
    /**
     * 获取联动状态描述
     */
    fun getLinkStatusDescription(): String {
        return when {
            isIndependentLedger() -> "独立记账簿"
            isParentLedger() && isChildLedger() -> "既是父记账簿又是子记账簿"
            isParentLedger() -> "父记账簿（有 ${getChildLedgers().size} 个子记账簿）"
            isChildLedger() -> "子记账簿（有 ${getParentLedgers().size} 个父记账簿）"
            else -> "未知状态"
        }
    }
}

/**
 * 记账簿联动关系摘要
 */
data class LedgerLinksSummary(
    val ledgerId: String,
    val ledgerName: String,
    val totalLinks: Int,
    val parentLedgerCount: Int,
    val childLedgerCount: Int,
    val autoSyncEnabled: Boolean,
    val syncStats: LedgerSyncStats? = null
) {
    
    /**
     * 获取联动类型描述
     */
    fun getLinkTypeDescription(): String {
        return when {
            totalLinks == 0 -> "独立记账簿"
            parentLedgerCount > 0 && childLedgerCount > 0 -> "中转记账簿"
            parentLedgerCount > 0 -> "子记账簿"
            childLedgerCount > 0 -> "父记账簿"
            else -> "未知类型"
        }
    }
    
    /**
     * 获取同步状态描述
     */
    fun getSyncStatusDescription(): String {
        return when {
            !autoSyncEnabled -> "手动同步"
            syncStats?.lastSyncTime != null -> "自动同步"
            else -> "等待同步"
        }
    }
}

/**
 * 记账簿联动网络信息
 * 
 * 用于显示整个联动网络的结构和状态
 */
data class LedgerLinkNetwork(
    val allLedgers: List<LedgerWithLinks>,
    val allLinks: List<LedgerLink>
) {
    
    /**
     * 获取所有根记账簿（只作为父记账簿，不作为子记账簿）
     */
    fun getRootLedgers(): List<LedgerWithLinks> {
        return allLedgers.filter { it.isParentLedger() && !it.isChildLedger() }
    }
    
    /**
     * 获取所有叶子记账簿（只作为子记账簿，不作为父记账簿）
     */
    fun getLeafLedgers(): List<LedgerWithLinks> {
        return allLedgers.filter { it.isChildLedger() && !it.isParentLedger() }
    }
    
    /**
     * 获取所有独立记账簿（无任何联动关系）
     */
    fun getIndependentLedgers(): List<LedgerWithLinks> {
        return allLedgers.filter { it.isIndependentLedger() }
    }
    
    /**
     * 获取联动网络统计信息
     */
    fun getNetworkStats(): LedgerNetworkStats {
        val totalLedgers = allLedgers.size
        val linkedLedgers = allLedgers.count { it.hasLinks() }
        val independentLedgers = allLedgers.count { it.isIndependentLedger() }
        val activeLinks = allLinks.count { it.isActive }
        val autoSyncLinks = allLinks.count { it.isActive && it.autoSyncEnabled }
        
        return LedgerNetworkStats(
            totalLedgers = totalLedgers,
            linkedLedgers = linkedLedgers,
            independentLedgers = independentLedgers,
            totalLinks = allLinks.size,
            activeLinks = activeLinks,
            autoSyncLinks = autoSyncLinks
        )
    }
}

/**
 * 联动网络统计信息
 */
data class LedgerNetworkStats(
    val totalLedgers: Int,
    val linkedLedgers: Int,
    val independentLedgers: Int,
    val totalLinks: Int,
    val activeLinks: Int,
    val autoSyncLinks: Int
) {
    /**
     * 获取联动比例
     */
    fun getLinkPercentage(): Float {
        return if (totalLedgers > 0) {
            linkedLedgers.toFloat() / totalLedgers.toFloat()
        } else 0f
    }
    
    /**
     * 获取自动同步比例
     */
    fun getAutoSyncPercentage(): Float {
        return if (activeLinks > 0) {
            autoSyncLinks.toFloat() / activeLinks.toFloat()
        } else 0f
    }
}