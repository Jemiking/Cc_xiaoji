package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import com.ccxiaoji.core.common.utils.daysUntil

data class SavingsGoal(
    val id: Long = 0,
    val name: String,
    val targetAmountCents: Long,
    val currentAmountCents: Long = 0L,
    val targetDate: LocalDate? = null,
    val description: String? = null,
    val color: String = "#4CAF50",
    val iconName: String = "savings",
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val targetAmountYuan: Double
        get() = targetAmountCents / 100.0
    
    val currentAmountYuan: Double
        get() = currentAmountCents / 100.0
    
    val progress: Float 
        get() = if (targetAmountCents > 0) {
            (currentAmountCents.toFloat() / targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f
    
    val progressPercentage: Int
        get() = (progress * 100).toInt()
    
    val remainingAmountCents: Long
        get() = (targetAmountCents - currentAmountCents).coerceAtLeast(0L)
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
    
    val isCompleted: Boolean
        get() = currentAmountCents >= targetAmountCents
    
    val daysRemaining: Int?
        get() = targetDate?.let { target ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            today.daysUntil(target).takeIf { it > 0 }
        }
}