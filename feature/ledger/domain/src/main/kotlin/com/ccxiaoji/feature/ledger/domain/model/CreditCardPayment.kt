package com.ccxiaoji.feature.ledger.domain.model

import com.ccxiaoji.core.database.entity.PaymentType
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 信用卡还款记录领域模型
 */
data class CreditCardPayment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val accountId: String,
    val paymentAmountCents: Long,
    val paymentType: PaymentType,
    val paymentDate: Instant,
    val dueAmountCents: Long,
    val isOnTime: Boolean,
    val note: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: Instant = Instant.DISTANT_PAST,
    val updatedAt: Instant = Instant.DISTANT_PAST
) {
    // 还款金额（元）
    val paymentAmountYuan: Double
        get() = paymentAmountCents / 100.0
    
    // 应还金额（元）
    val dueAmountYuan: Double
        get() = dueAmountCents / 100.0
}