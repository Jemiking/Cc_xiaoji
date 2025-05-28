package com.ccxiaoji.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class SavingsGoal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: LocalDate? = null,
    val description: String? = null,
    val color: String = "#4CAF50",
    val iconName: String = "savings",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val progress: Float 
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    val progressPercentage: Int
        get() = (progress * 100).toInt()
    
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
    
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
    
    val daysRemaining: Long?
        get() = targetDate?.let {
            val today = LocalDate.now()
            if (it.isAfter(today)) {
                java.time.temporal.ChronoUnit.DAYS.between(today, it)
            } else null
        }
}