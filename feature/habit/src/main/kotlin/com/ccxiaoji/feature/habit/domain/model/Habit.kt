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
    val updatedAt: Instant,

    // ===== 新增字段（Phase 2）=====
    val reminderEnabled: Boolean? = null,
    val reminderTime: String? = null  // "HH:mm" 格式
) {
    /**
     * 获取实际提醒时间（小时:分钟）
     *
     * @param globalTime 全局配置的提醒时间（如"20:00"）
     * @param globalEnabled 全局提醒是否启用
     * @return Pair<hour, minute>，null表示不提醒
     */
    fun getEffectiveReminderTime(
        globalTime: String,
        globalEnabled: Boolean
    ): Pair<Int, Int>? {
        val isEnabled = reminderEnabled ?: globalEnabled
        if (!isEnabled) return null

        val timeStr = reminderTime ?: globalTime
        val parts = timeStr.split(":")
        return Pair(parts[0].toInt(), parts[1].toInt())
    }
}

data class HabitRecord(
    val id: String,
    val habitId: String,
    val recordDate: LocalDate,
    val count: Int,
    val note: String?
)

data class HabitWithStreak(
    val habit: Habit,
    val currentStreak: Int,
    val completedCount: Int = 0,
    val longestStreak: Int = 0
)