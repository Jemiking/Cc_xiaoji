package com.ccxiaoji.app.notification

import com.ccxiaoji.feature.habit.api.HabitReminderScheduler
import com.ccxiaoji.shared.notification.api.NotificationApi
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HabitReminderScheduler的实现
 * 在app模块中实现feature-habit模块的提醒调度接口
 */
@Singleton
class HabitReminderSchedulerImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : HabitReminderScheduler {
    
    override fun scheduleDailyHabitReminder(
        habitId: String,
        habitTitle: String,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        runBlocking {
            notificationApi.scheduleDailyHabitReminder(
                habitId = habitId,
                habitTitle = habitTitle,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute
            )
        }
    }
    
    override fun cancelHabitReminder(habitId: String) {
        runBlocking {
            notificationApi.cancelHabitReminder(habitId)
        }
    }
}