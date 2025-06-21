package com.ccxiaoji.feature.ledger.domain.repository

/**
 * 信用卡还款统计信息
 */
data class PaymentStats(
    val totalPayments: Long,          // 总还款金额（分）
    val paymentCount: Int,            // 还款次数
    val averagePayment: Long,         // 平均还款金额（分）
    val fullPaymentCount: Int,        // 全额还款次数
    val minPaymentCount: Int,         // 最低还款次数
    val customPaymentCount: Int,      // 自定义还款次数
    val onTimePaymentRate: Float      // 按时还款率
)