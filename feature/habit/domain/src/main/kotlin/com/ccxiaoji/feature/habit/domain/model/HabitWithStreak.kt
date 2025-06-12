package com.ccxiaoji.feature.habit.domain.model

/**
 * 习惯及其连续打卡信息
 */
data class HabitWithStreak(
    val habit: Habit,
    val currentStreak: Int,
    val completedCount: Int = 0,
    val longestStreak: Int = 0
)