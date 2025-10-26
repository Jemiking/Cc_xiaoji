package com.ccxiaoji.app.notification

import android.content.Context
import androidx.work.*
import com.ccxiaoji.shared.notification.api.NotificationApi
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val notificationApi: NotificationApi
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
    @Deprecated("已迁移到队列架构，请使用 NotificationApi.scheduleTaskReminder")
    fun scheduleTaskReminder(
        taskId: String,
        taskTitle: String,
        dueAt: Instant,
        reminderMinutes: Int = 30  // 提前提醒分钟数（默认30分钟）
    ) {
        Log.d(TAG, "Scheduling task reminder: taskId=$taskId, title=$taskTitle, dueAt=$dueAt, reminderMinutes=$reminderMinutes")
        try {
            val now = Clock.System.now()
            val delay = dueAt.toEpochMilliseconds() - now.toEpochMilliseconds()

            // 如果任务已经过期，不安排提醒
            if (delay <= 0) {
                Log.w(TAG, "Task already overdue, skipping reminder: taskId=$taskId")
                return
            }

            // 提前指定分钟数提醒（读取用户配置而非硬编码）
            val reminderDelay = (delay - TimeUnit.MINUTES.toMillis(reminderMinutes.toLong())).coerceAtLeast(0)

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

            Log.d(TAG, "Task reminder scheduled successfully: taskId=$taskId, delay=${reminderDelay}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling task reminder: taskId=$taskId", e)
            throw e
        }
    }
    
    // 取消任务提醒
    @Deprecated("已迁移到队列架构，请使用 NotificationApi.cancelTaskReminder")
    fun cancelTaskReminder(taskId: String) {
        Log.d(TAG, "Cancelling task reminder: taskId=$taskId")
        try {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(taskId)
            Log.d(TAG, "Task reminder cancelled successfully: taskId=$taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling task reminder: taskId=$taskId", e)
        }
    }
    
    // 安排每日习惯提醒
    @Deprecated("已迁移到队列架构，请使用 NotificationApi.scheduleDailyHabitReminder")
    fun scheduleDailyHabitReminder(habitId: String, habitTitle: String, reminderHour: Int, reminderMinute: Int) {
        Log.d(TAG, "Scheduling daily habit reminder: habitId=$habitId, title=$habitTitle, time=$reminderHour:$reminderMinute")
        try {
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
                Log.d(TAG, "Reminder time already passed today, scheduling for tomorrow: $reminderTime")
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

            Log.d(TAG, "Habit reminder scheduled successfully: habitId=$habitId, delay=${delay}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling habit reminder: habitId=$habitId", e)
            throw e
        }
    }
    
    // 取消习惯提醒
    @Deprecated("已迁移到队列架构，请使用 NotificationApi.cancelHabitReminder")
    fun cancelHabitReminder(habitId: String) {
        Log.d(TAG, "Cancelling habit reminder: habitId=$habitId")
        try {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(habitId)
            Log.d(TAG, "Habit reminder cancelled successfully: habitId=$habitId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling habit reminder: habitId=$habitId", e)
        }
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
