package com.ccxiaoji.feature.ledger.domain.model

import com.ccxiaoji.common.model.RecurringFrequency
import kotlinx.datetime.Instant

data class RecurringTransaction(
    val id: String,
    val name: String, // 定期交易名称
    val accountId: String,
    val amountCents: Int,
    val categoryId: String,
    val note: String?,
    val frequency: RecurringFrequency,
    val dayOfWeek: Int? = null, // 1-7 for Monday-Sunday (when frequency is WEEKLY)
    val dayOfMonth: Int? = null, // 1-31 (when frequency is MONTHLY)
    val monthOfYear: Int? = null, // 1-12 (when frequency is YEARLY)
    val startDate: Instant, // 开始日期
    val endDate: Instant? = null, // 结束日期，null表示永不结束
    val isEnabled: Boolean = true,
    val lastExecutionDate: Instant? = null,
    val nextExecutionDate: Instant, // 下次执行日期
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
}