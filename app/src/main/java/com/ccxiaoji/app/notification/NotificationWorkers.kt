package com.ccxiaoji.app.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
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
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*

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
@HiltWorker
class HabitReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationApi: NotificationApi
) : Worker(context, params) {

    companion object {
        private const val TAG = "HabitReminderWorker"
    }

    override fun doWork(): Result {
        val habitId = inputData.getString(NotificationScheduler.KEY_HABIT_ID) ?: return Result.failure()
        val habitTitle = inputData.getString(NotificationScheduler.KEY_HABIT_TITLE) ?: return Result.failure()

        return try {
            Log.d(TAG, "Sending habit reminder: habitId=$habitId, title=$habitTitle")
            notificationApi.sendHabitReminder(habitId, habitTitle)
            Log.d(TAG, "Habit reminder sent successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send habit reminder", e)
            Result.failure()
        }
    }
}

// 每日检查Worker
@HiltWorker
class DailyCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val budgetRepository: com.ccxiaoji.feature.ledger.domain.repository.BudgetRepository,
    private val notificationApi: NotificationApi,
    private val dataStore: DataStore<Preferences>
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DailyCheckWorker"
        private val KEY_BUDGET_ALERT_THRESHOLD = intPreferencesKey("budget_alert_threshold")
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting daily check")

            // 执行预算检查
            checkBudgets()

            // 兜底：进行一次小部件全量刷新（不展示通知）
            Log.i("CCXJ/WIDGET", "DailyCheck: enqueueAll widget refresh fallback")
            WidgetUpdateScheduler.enqueueAll(applicationContext)

            Log.d(TAG, "Daily check completed successfully")
            Result.success()
        } catch (t: Throwable) {
            Log.e(TAG, "Daily check failed: ${t.message}", t)
            // 不因失败而重试
            Result.success()
        }
    }

    private suspend fun checkBudgets() {
        try {
            // 获取阈值设置
            val threshold = dataStore.data.first()[KEY_BUDGET_ALERT_THRESHOLD] ?: 80
            Log.d(TAG, "Budget alert threshold: $threshold%")

            // 获取当前年月
            val now = kotlinx.datetime.Clock.System.now()
            val localDate = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            val year = localDate.year
            val month = localDate.monthNumber

            Log.d(TAG, "Checking budgets for $year-$month")

            // 获取本月所有预算及支出
            budgetRepository.getBudgetsWithSpent(year, month).first()
                .forEach { budget ->
                    if (budget.budgetAmountCents > 0) {
                        val usagePercent = (budget.spentAmountCents * 100 / budget.budgetAmountCents)
                        Log.d(TAG, "Budget usage: $usagePercent% (spent: ${budget.spentAmountCents}, budget: ${budget.budgetAmountCents})")

                        if (usagePercent >= threshold) {
                            // 获取分类名称（如果是总预算则显示"总预算"）
                            val categoryName = budget.categoryId?.let { "某分类" } ?: "总预算"
                            Log.d(TAG, "Sending budget alert for $categoryName: $usagePercent%")
                            notificationApi.sendBudgetAlert(categoryName, usagePercent)
                        }
                    }
                }

            Log.d(TAG, "Budget check completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check budgets", e)
            // 不抛出异常，让整体任务继续执行
        }
    }
}
