package com.ccxiaoji.feature.habit.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class Habit(
    val id: String,
    val title: String,
    val description: String?,
    val period: String,
    val target: Int,
    val color: String,
    val icon: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class HabitRecord(
    val id: String,
    val habitId: String,
    val recordDate: LocalDate,
    val count: Int,
    val note: String?
)