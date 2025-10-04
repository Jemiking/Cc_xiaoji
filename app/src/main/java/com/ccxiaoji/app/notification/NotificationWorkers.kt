package com.ccxiaoji.app.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.app.notification.NotificationScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import android.util.Log
import com.ccxiaoji.app.presentation.widget.ledger.WidgetUpdateScheduler

// 任务提醒Worker
@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationApi: NotificationApi
) : Worker(context, params) {

    companion object {
        private const val TAG = "TaskReminderWorker"
    }

    override fun doWork(): Result {
        val taskId = inputData.getString(NotificationScheduler.KEY_TASK_ID) ?: return Result.failure()
        val taskTitle = inputData.getString(NotificationScheduler.KEY_TASK_TITLE) ?: return Result.failure()
        val dueTime = inputData.getString(NotificationScheduler.KEY_DUE_TIME) ?: return Result.failure()

        return try {
            Log.d(TAG, "Sending task reminder: taskId=$taskId, title=$taskTitle, dueTime=$dueTime")
            notificationApi.sendTaskReminder(taskId, taskTitle, dueTime)
            Log.d(TAG, "Task reminder sent successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send task reminder", e)
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
        // TODO: 未来在此实现预算检查等每日任务
        // 兜底：进行一次小部件全量刷新（不展示通知）
        return try {
            Log.i("CCXJ/WIDGET", "DailyCheck: enqueueAll widget refresh fallback")
            WidgetUpdateScheduler.enqueueAll(applicationContext)
            Result.success()
        } catch (t: Throwable) {
            Log.e("CCXJ/WIDGET", "DailyCheck: widget refresh fallback failed: ${t.message}", t)
            // 不因兜底刷新失败而重试
            Result.success()
        }
    }
}
