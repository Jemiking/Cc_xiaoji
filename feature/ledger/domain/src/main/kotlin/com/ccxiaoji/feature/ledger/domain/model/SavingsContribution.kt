package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class SavingsContribution(
    val id: Long = 0,
    val goalId: Long,
    val amountCents: Long,
    val note: String? = null,
    val createdAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
    
    val isDeposit: Boolean
        get() = amountCents > 0
    
    val isWithdrawal: Boolean
        get() = amountCents < 0
}