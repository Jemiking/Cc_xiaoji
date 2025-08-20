package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 新的分类ID
    val categoryDetails: CategoryDetails? = null, // 分类详情
    val note: String?,
    val ledgerId: String, // 所属记账簿ID
    val createdAt: Instant, // 记录创建时间
    val updatedAt: Instant, // 记录修改时间
    val transactionDate: Instant? = null, // 交易实际发生时间
    val location: LocationData? = null // 交易发生地点
) {
    val amountYuan: Double
        get() = amountCents / 100.0
    
    /**
     * 获取交易的实际时间，如果没有设置则使用创建时间
     */
    val actualTransactionTime: Instant
        get() = transactionDate ?: createdAt
}

data class CategoryDetails(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String // "INCOME" or "EXPENSE"
)