package com.ccxiaoji.feature.habit.api

/**
 * Habit模块提醒调度接口
 * 在app模块中实现具体的提醒逻辑
 */
interface HabitReminderScheduler {
    /**
     * 安排每日习惯提醒
     */
    fun scheduleDailyHabitReminder(
        habitId: String,
        habitTitle: String,
        reminderHour: Int,
        reminderMinute: Int
    )
    
    /**
     * 取消习惯提醒
     */
    fun cancelHabitReminder(habitId: String)
}