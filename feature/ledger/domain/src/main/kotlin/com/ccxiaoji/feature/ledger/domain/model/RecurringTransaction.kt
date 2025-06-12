package com.ccxiaoji.feature.ledger.domain.model

import com.ccxiaoji.core.database.model.RecurringFrequency

/**
 * 定期交易领域模型
 */
data class RecurringTransaction(
    val id: String,
    val userId: String,
    val name: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String,
    val note: String? = null,
    val frequency: RecurringFrequency,
    val dayOfWeek: Int? = null, // 1-7 for weekly
    val dayOfMonth: Int? = null, // 1-31 for monthly/yearly
    val monthOfYear: Int? = null, // 1-12 for yearly
    val startDate: Long,
    val endDate: Long? = null,
    val isEnabled: Boolean = true,
    val lastExecutionDate: Long? = null,
    val nextExecutionDate: Long,
    val createdAt: Long,
    val updatedAt: Long
)