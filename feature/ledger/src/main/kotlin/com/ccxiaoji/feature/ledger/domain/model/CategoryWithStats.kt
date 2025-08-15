package com.ccxiaoji.feature.ledger.domain.model

/**
 * 带统计信息的分类
 */
data class CategoryWithStats(
    val category: Category,
    val transactionCount: Int,
    val usageCount: Long
)