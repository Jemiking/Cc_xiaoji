package com.ccxiaoji.feature.ledger.domain.model

/**
 * 信用卡支付统计信息
 */
data class PaymentStats(
    val onTimeRate: Double,       // 准时还款率（百分比）
    val totalPayments: Int,       // 总还款次数
    val totalAmountYuan: Double   // 总还款金额（元）
)