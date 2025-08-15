package com.ccxiaoji.app.domain.model

import kotlinx.datetime.*

data class Countdown(
    val id: String,
    val title: String,
    val targetDate: LocalDate,
    val emoji: String?,
    val color: String,
    val showOnWidget: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val daysRemaining: Int
        get() {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            return (targetDate.toEpochDays() - today.toEpochDays()).toInt()
        }
    
    val status: CountdownStatus
        get() = when {
            daysRemaining > 0 -> CountdownStatus.UPCOMING
            daysRemaining == 0 -> CountdownStatus.TODAY
            else -> CountdownStatus.PAST
        }
}

enum class CountdownStatus {
    UPCOMING,
    TODAY,
    PAST
}