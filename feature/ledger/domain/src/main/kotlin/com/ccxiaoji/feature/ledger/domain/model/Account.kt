package com.ccxiaoji.feature.ledger.domain.model

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
    
    // Credit card specific fields
    val creditLimitCents: Long? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null,
    val gracePeriodDays: Int? = null,
    
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val balanceYuan: Double
        get() = balanceCents / 100.0
    
    val creditLimitYuan: Double?
        get() = creditLimitCents?.let { it / 100.0 }
    
    val availableCreditCents: Long?
        get() = if (type == AccountType.CREDIT_CARD && creditLimitCents != null) {
            creditLimitCents + balanceCents // balanceCents is negative for debt
        } else null
    
    val availableCreditYuan: Double?
        get() = availableCreditCents?.let { it / 100.0 }
    
    val utilizationRate: Double?
        get() = if (type == AccountType.CREDIT_CARD && creditLimitCents != null && creditLimitCents > 0) {
            (-balanceCents.toDouble() / creditLimitCents) * 100
        } else null
}

enum class AccountType(val displayName: String, val icon: String) {
    CASH("现金", "💵"),
    BANK_CARD("银行卡", "💳"),
    ALIPAY("支付宝", "📱"),
    WECHAT("微信", "💬"),
    CREDIT_CARD("信用卡", "💳"),
    OTHER("其他", "📋")
}