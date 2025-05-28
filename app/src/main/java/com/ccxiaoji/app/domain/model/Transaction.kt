package com.ccxiaoji.app.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 新的分类ID
    val category: TransactionCategory? = null, // 保留用于向后兼容
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

enum class TransactionCategory(val displayName: String, val icon: String) {
    FOOD("餐饮", "🍜"),
    TRANSPORT("交通", "🚇"),
    SHOPPING("购物", "🛍️"),
    ENTERTAINMENT("娱乐", "🎮"),
    MEDICAL("医疗", "🏥"),
    EDUCATION("教育", "📚"),
    HOUSING("居住", "🏠"),
    UTILITIES("水电", "💡"),
    COMMUNICATION("通讯", "📱"),
    INCOME("收入", "💰"),
    OTHER("其他", "📌")
}