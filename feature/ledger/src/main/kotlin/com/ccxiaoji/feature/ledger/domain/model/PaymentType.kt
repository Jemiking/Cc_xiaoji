package com.ccxiaoji.feature.ledger.domain.model

/**
 * 信用卡还款类型
 */
enum class PaymentType {
    FULL_PAYMENT,    // 全额还款
    PARTIAL_PAYMENT, // 部分还款
    MINIMUM_PAYMENT, // 最低还款
    AUTO_PAYMENT     // 自动还款
}