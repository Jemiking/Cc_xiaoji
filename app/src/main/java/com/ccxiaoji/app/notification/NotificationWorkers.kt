package com.ccxiaoji.app.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.app.notification.NotificationScheduler
import javax.inject.Inject

// 任务提醒Worker
class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    @Inject
    lateinit var notificationApi: NotificationApi
    
    override fun doWork(): Result {
        val taskId = inputData.getString(NotificationScheduler.KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(NotificationScheduler.KEY_TASK_TITLE) ?: return Result.failure()
        val dueTime = inputData.getString(NotificationScheduler.KEY_DUE_TIME) ?: return Result.failure()
        
        // TODO: 注入NotificationApi实例
        // 暂时直接创建实例，后续需要使用HiltWorker
        return try {
            // notificationApi.sendTaskReminder(taskId, taskTitle, dueTime)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

// 习惯提醒Worker
class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    @Inject
    lateinit var notificationApi: NotificationApi
    
    override fun doWork(): Result {
        val habitId = inputData.getString(NotificationScheduler.KEY_HABIT_ID) ?: return Result.failure()
        val habitTitle = inputData.getString(NotificationScheduler.KEY_HABIT_TITLE) ?: return Result.failure()
        
        // TODO: 注入NotificationApi实例
        return try {
            // notificationApi.sendHabitReminder(habitId, habitTitle)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

// 每日检查Worker
class DailyCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // TODO: 实现预算检查等每日任务
        // 这里可以注入Repository来检查预算使用情况等
        
        return Result.success()
    }
}