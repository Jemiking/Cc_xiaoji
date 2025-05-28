package com.ccxiaoji.app.domain.model

import kotlinx.datetime.Instant

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceCents: Long,
    val currency: String = "CNY",
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val balanceYuan: Double
        get() = balanceCents / 100.0
}

enum class AccountType(val displayName: String, val icon: String) {
    CASH("现金", "💵"),
    BANK_CARD("银行卡", "💳"),
    ALIPAY("支付宝", "📱"),
    WECHAT("微信", "💬"),
    CREDIT_CARD("信用卡", "💳"),
    OTHER("其他", "📋")
}