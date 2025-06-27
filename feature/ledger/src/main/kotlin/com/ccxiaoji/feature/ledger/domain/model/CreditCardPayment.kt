package com.ccxiaoji.feature.ledger.domain.model

import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import kotlinx.datetime.Instant

/**
 * 信用卡还款记录领域模型
 */
data class CreditCardPayment(
    val id: String,
    val userId: String,
    val accountId: String,
    val paymentAmountCents: Long,
    val paymentType: PaymentType,
    val paymentDate: Instant,
    val dueAmountCents: Long,
    val isOnTime: Boolean,
    val note: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val paymentAmountYuan: Double
        get() = paymentAmountCents / 100.0
    
    val dueAmountYuan: Double
        get() = dueAmountCents / 100.0
    
    val paymentPercentage: Float
        get() = if (dueAmountCents > 0) {
            (paymentAmountCents.toFloat() / dueAmountCents.toFloat()).coerceIn(0f, Float.MAX_VALUE)
        } else {
            0f
        }
    
    val isFullPayment: Boolean
        get() = paymentType == PaymentType.FULL || paymentAmountCents >= dueAmountCents
    
    val isPartialPayment: Boolean
        get() = paymentAmountCents > 0 && paymentAmountCents < dueAmountCents
}