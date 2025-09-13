package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 账本领域模型
 *
 * 账本是交易记录的逻辑分组单位，用于将交易按用途进行分类管理。
 * 账户、分类等基础数据在所有账本间共享。
 */
data class Ledger(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val color: String = "#3A7AFE", // 默认蓝色
    val icon: String = "book", // 默认图标
    val isDefault: Boolean = false,
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        const val DEFAULT_LEDGER_NAME = "总账本"
        const val DEFAULT_LEDGER_DESCRIPTION = "默认账本，包含所有基本记账数据"
        
        // 预定义图标
        val PREDEFINED_ICONS = listOf(
            "book", "home", "baby", "school", "work", 
            "travel", "car", "health", "shopping", "food"
        )
        
        // 预定义颜色
        val PREDEFINED_COLORS = listOf(
            "#3A7AFE", "#66BB6A", "#FFB74D", "#AB47BC", 
            "#FF7043", "#26C6DA", "#FFA726", "#EC407A"
        )
    }
}

/**
 * 账本与统计数据
 */
data class LedgerWithStats(
    val ledger: Ledger,
    val transactionCount: Int = 0,
    val totalIncome: Long = 0, // 分为单位
    val totalExpense: Long = 0, // 分为单位
    val lastTransactionDate: Instant? = null
)
