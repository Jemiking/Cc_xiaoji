package com.ccxiaoji.feature.ledger.domain.model

data class CategoryStatistic(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Int,
    val transactionCount: Int
)