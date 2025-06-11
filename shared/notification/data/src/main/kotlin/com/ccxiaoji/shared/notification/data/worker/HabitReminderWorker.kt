package com.ccxiaoji.shared.notification.data.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ccxiaoji.shared.notification.data.NotificationManager
import com.ccxiaoji.shared.notification.data.NotificationScheduler

/**
 * 习惯提醒Worker
 * 每日定时发送习惯打卡提醒
 */
class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        val habitId = inputData.getString(NotificationScheduler.KEY_HABIT_ID) ?: return Result.failure()
        val habitTitle = inputData.getString(NotificationScheduler.KEY_HABIT_TITLE) ?: return Result.failure()
        
        val notificationManager = NotificationManager(applicationContext)
        notificationManager.sendHabitReminder(habitId, habitTitle)
        
        return Result.success()
    }
}