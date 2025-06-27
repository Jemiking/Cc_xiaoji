package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceCents: Long,
    val currency: String = "CNY",
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    
    // Credit card specific fields
    val creditLimitCents: Long? = null,
    val billingDay: Int? = null,
    val paymentDueDay: Int? = null,
    val gracePeriodDays: Int? = null,
    val annualFeeAmountCents: Long? = null,
    val annualFeeWaiverThresholdCents: Long? = null,
    val cashAdvanceLimitCents: Long? = null,
    val interestRate: Double? = null,
    
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val balanceYuan: Double
        get() = balanceCents / 100.0
    
    val creditLimitYuan: Double?
        get() = creditLimitCents?.let { it / 100.0 }
    
    val availableCreditCents: Long?
        get() = if (type == AccountType.CREDIT_CARD && creditLimitCents != null) {
            creditLimitCents + balanceCents // balanceCents is negative for debt
        } else null
    
    val availableCreditYuan: Double?
        get() = availableCreditCents?.let { it / 100.0 }
    
    val utilizationRate: Double?
        get() = if (type == AccountType.CREDIT_CARD && creditLimitCents != null && creditLimitCents > 0) {
            (-balanceCents.toDouble() / creditLimitCents) * 100
        } else null
    
    val annualFeeAmountYuan: Double?
        get() = annualFeeAmountCents?.let { it / 100.0 }
    
    val annualFeeWaiverThresholdYuan: Double?
        get() = annualFeeWaiverThresholdCents?.let { it / 100.0 }
    
    val cashAdvanceLimitYuan: Double?
        get() = cashAdvanceLimitCents?.let { it / 100.0 }
    
    val dailyInterestRatePercent: Double?
        get() = interestRate?.let { it * 100 }
}