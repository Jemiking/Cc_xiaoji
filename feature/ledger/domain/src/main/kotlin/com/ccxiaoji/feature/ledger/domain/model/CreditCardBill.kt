package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 信用卡账单领域模型
 */
data class CreditCardBill(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val accountId: String,
    val billStartDate: Instant,
    val billEndDate: Instant,
    val paymentDueDate: Instant,
    val totalAmountCents: Long,          // 账单总额（分）
    val newChargesCents: Long,           // 新增消费（分）
    val previousBalanceCents: Long,      // 上期余额（分）
    val paymentsCents: Long,             // 本期还款（分）
    val adjustmentsCents: Long = 0,      // 调整金额（分）
    val minimumPaymentCents: Long,       // 最低还款额（分）
    val isGenerated: Boolean = true,
    val isPaid: Boolean = false,
    val paidAmountCents: Long = 0,       // 已还金额（分）
    val remainingAmountCents: Long = 0,  // 剩余应还（分）
    val isOverdue: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    // 账单总额（元）
    val totalAmountYuan: Double
        get() = totalAmountCents / 100.0
    
    // 新增消费（元）
    val newChargesYuan: Double
        get() = newChargesCents / 100.0
    
    // 最低还款额（元）
    val minimumPaymentYuan: Double
        get() = minimumPaymentCents / 100.0
    
    // 已还金额（元）
    val paidAmountYuan: Double
        get() = paidAmountCents / 100.0
}