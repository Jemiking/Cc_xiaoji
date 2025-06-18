package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 新的分类ID
    val categoryDetails: CategoryDetails? = null, // 分类详情
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
}

data class CategoryDetails(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: String // "INCOME" or "EXPENSE"
)