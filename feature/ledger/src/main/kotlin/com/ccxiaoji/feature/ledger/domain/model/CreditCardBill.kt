package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 信用卡账单领域模型
 */
data class CreditCardBill(
    val id: String,
    val userId: String,
    val accountId: String,
    
    // 账单周期
    val billStartDate: Instant,
    val billEndDate: Instant,
    val paymentDueDate: Instant,
    
    // 账单金额（单位：分）
    val totalAmountCents: Long,
    val newChargesCents: Long,
    val previousBalanceCents: Long,
    val paymentsCents: Long,
    val adjustmentsCents: Long,
    val minimumPaymentCents: Long,
    
    // 账单状态
    val isGenerated: Boolean = false,
    val isPaid: Boolean = false,
    val paidAmountCents: Long = 0,
    val isOverdue: Boolean = false,
    
    // 元数据
    val createdAt: Instant,
    val updatedAt: Instant
) {
    // 计算属性
    val remainingAmountCents: Long
        get() = totalAmountCents - paidAmountCents
    
    val totalAmountYuan: Double
        get() = totalAmountCents / 100.0
    
    val minimumPaymentYuan: Double
        get() = minimumPaymentCents / 100.0
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
    
    val paymentProgress: Float
        get() = if (totalAmountCents > 0) {
            (paidAmountCents.toFloat() / totalAmountCents.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}