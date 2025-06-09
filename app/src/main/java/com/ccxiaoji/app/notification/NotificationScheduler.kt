package com.ccxiaoji.app.notification

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.plus
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        private const val TAG = "CcXiaoJi"
        const val TASK_REMINDER_WORK_TAG = "task_reminder"
        const val HABIT_REMINDER_WORK_TAG = "habit_reminder"
        const val DAILY_CHECK_WORK_TAG = "daily_check"
        
        const val KEY_TASK_ID = "task_id"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_DUE_TIME = "due_time"
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_TITLE = "habit_title"
    }
    
    // 安排任务提醒
    fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        val now = Clock.System.now()
        val delay = dueAt.toEpochMilliseconds() - now.toEpochMilliseconds()
        
        // 如果任务已经过期，不安排提醒
        if (delay <= 0) return
        
        // 提前30分钟提醒
        val reminderDelay = (delay - TimeUnit.MINUTES.toMillis(30)).coerceAtLeast(0)
        
        val data = workDataOf(
            KEY_TASK_ID to taskId,
            KEY_TASK_TITLE to taskTitle,
            KEY_DUE_TIME to dueAt.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        )
        
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(reminderDelay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(TASK_REMINDER_WORK_TAG)
            .addTag(taskId)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "task_reminder_$taskId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    // 取消任务提醒
    fun cancelTaskReminder(taskId: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(taskId)
    }
    
    // 安排每日习惯提醒
    fun scheduleDailyHabitReminder(habitId: String, habitTitle: String, reminderHour: Int, reminderMinute: Int) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var reminderTime = LocalDateTime(
            now.date.year,
            now.date.monthNumber,
            now.date.dayOfMonth,
            reminderHour,
            reminderMinute,
            0,
            0
        ).toInstant(TimeZone.currentSystemDefault())
        
        // 如果今天的提醒时间已过，安排明天的
        if (reminderTime <= Clock.System.now()) {
            reminderTime = reminderTime.plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        }
        
        val delay = reminderTime.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()
        
        val data = workDataOf(
            KEY_HABIT_ID to habitId,
            KEY_HABIT_TITLE to habitTitle
        )
        
        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(HABIT_REMINDER_WORK_TAG)
            .addTag(habitId)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "habit_reminder_$habitId",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
    
    // 取消习惯提醒
    fun cancelHabitReminder(habitId: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(habitId)
    }
    
    // 安排每日检查（用于检查预算等）
    fun scheduleDailyCheck() {
        Log.d(TAG, "Scheduling daily check")
        try {
            val workRequest = PeriodicWorkRequestBuilder<DailyCheckWorker>(
                1, TimeUnit.DAYS
        )
            .addTag(DAILY_CHECK_WORK_TAG)
            .build()
        
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "daily_check",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
            Log.d(TAG, "Daily check scheduled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily check", e)
            throw e
        }
    }
    
    // 取消所有通知
    fun cancelAllScheduledNotifications() {
        WorkManager.getInstance(context).cancelAllWork()
    }
}

// 任务提醒Worker
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

// 习惯提醒Worker
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