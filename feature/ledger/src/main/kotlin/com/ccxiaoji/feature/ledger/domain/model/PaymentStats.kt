package com.ccxiaoji.feature.ledger.domain.model

/**
 * 支付统计
 */
data class PaymentStats(
    val totalPaidAmount: Long,
    val totalPaidCount: Int,
    val lastPaymentDate: Long?
)