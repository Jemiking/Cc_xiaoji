package com.ccxiaoji.shared.notification.data.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ccxiaoji.shared.notification.data.NotificationManager
import com.ccxiaoji.shared.notification.data.NotificationScheduler

/**
 * 任务提醒Worker
 * 在指定时间发送任务提醒通知
 */
class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        val taskId = inputData.getString(NotificationScheduler.KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(NotificationScheduler.KEY_TASK_TITLE) ?: return Result.failure()
        val dueTime = inputData.getString(NotificationScheduler.KEY_DUE_TIME) ?: return Result.failure()
        
        val notificationManager = NotificationManager(applicationContext)
        notificationManager.sendTaskReminder(taskId, taskTitle, dueTime)
        
        return Result.success()
    }
}