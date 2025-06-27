package com.ccxiaoji.feature.ledger.domain.model

enum class AccountType(val icon: String, val displayName: String) {
    CASH("💵", "现金"),          // 现金账户
    BANK("🏦", "银行卡"),        // 银行账户
    CREDIT_CARD("💳", "信用卡"), // 信用卡
    ALIPAY("📱", "支付宝"),      // 支付宝
    WECHAT("💬", "微信支付"),    // 微信支付
    OTHER("📂", "其他")          // 其他账户
}