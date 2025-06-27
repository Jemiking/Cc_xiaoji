package com.ccxiaoji.feature.ledger.domain.model

import java.time.LocalDateTime

data class SavingsContribution(
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val isDeposit: Boolean
        get() = amount > 0
    
    val isWithdrawal: Boolean
        get() = amount < 0
}