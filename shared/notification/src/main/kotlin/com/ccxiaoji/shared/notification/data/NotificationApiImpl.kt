package com.ccxiaoji.shared.notification.data

import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.manager.NotificationManager
import com.ccxiaoji.shared.notification.data.manager.NotificationQueueManager
import com.ccxiaoji.core.database.entity.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationApi的实现类
 * 注意：调度相关的方法由app模块的NotificationScheduler实现
 */
@Singleton
class NotificationApiImpl @Inject constructor(
    private val notificationManager: NotificationManager,
    private val queueManager: NotificationQueueManager
) : NotificationApi {
    
    override fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String) {
        notificationManager.sendTaskReminder(taskId, taskTitle, dueTime)
    }
    
    override fun sendHabitReminder(habitId: String, habitTitle: String) {
        notificationManager.sendHabitReminder(habitId, habitTitle)
    }
    
    override fun sendBudgetAlert(categoryName: String, percentage: Int) {
        notificationManager.sendBudgetAlert(categoryName, percentage)
    }
    
    override fun sendCreditCardReminder(
        cardId: String,
        cardName: String,
        debtAmount: String,
        daysUntilDue: Int,
        paymentDueDay: Int
    ) {
        notificationManager.sendCreditCardReminder(cardId, cardName, debtAmount, daysUntilDue, paymentDueDay)
    }
    
    override fun sendGeneralNotification(title: String, message: String, notificationId: Int?) {
        if (notificationId != null) {
            notificationManager.sendGeneralNotification(title, message, notificationId)
        } else {
            notificationManager.sendGeneralNotification(title, message)
        }
    }
    
    override fun cancelNotification(notificationId: Int) {
        notificationManager.cancelNotification(notificationId)
    }
    
    override fun cancelAllNotifications() {
        notificationManager.cancelAllNotifications()
    }
    
    // 以下调度相关方法改为写入统一通知队列 + 同步创建 WorkManager 任务
    override fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        // 将任务标题与到期时间分别存入标题与消息，便于Worker按type调用专用渠道
        val title = taskTitle
        val message = dueAt.toLocalDateTime(TimeZone.currentSystemDefault()).toString()
        // 队列调度：到期时间触发
        GlobalScope.launch(Dispatchers.IO) {
            queueManager.enqueue(
                type = NotificationType.TASK,
                sourceModule = "todo",
                sourceId = taskId,
                title = title,
                message = message,
                scheduledAt = dueAt.toEpochMilliseconds(),
                userId = "local"
            )
        }
    }
    
    override fun cancelTaskReminder(taskId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                queueManager.cancelBySource(NotificationType.TASK, "todo", taskId)
            }
        }
    }
    
    override fun scheduleDailyHabitReminder(habitId: String, habitTitle: String, reminderHour: Int, reminderMinute: Int) {
        // Phase1：简化为安排下一次提醒（非周期）
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        var next = kotlinx.datetime.LocalDateTime(
            now.date.year,
            now.date.monthNumber,
            now.date.dayOfMonth,
            reminderHour,
            reminderMinute,
            0, 0
        ).toInstant(kotlinx.datetime.TimeZone.currentSystemDefault())
        if (next <= Clock.System.now()) {
            next = next.plus(1, kotlinx.datetime.DateTimeUnit.DAY, TimeZone.currentSystemDefault())
        }
        // 存入习惯标题，Worker按type调用专用渠道
        val title = habitTitle
        val message = ""
        GlobalScope.launch(Dispatchers.IO) {
            queueManager.enqueue(
                type = NotificationType.HABIT,
                sourceModule = "habit",
                sourceId = habitId,
                title = title,
                message = message,
                scheduledAt = next.toEpochMilliseconds(),
                userId = "local"
            )
        }
    }
    
    override fun cancelHabitReminder(habitId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                queueManager.cancelBySource(NotificationType.HABIT, "habit", habitId)
            }
        }
    }
    
    override fun scheduleDailyCheck() {
        // Phase1：占位（每日检查建议保持 app 现有调度器，后续统一迁移）
    }
    
    override fun cancelAllScheduledNotifications() {
        // Phase1：占位（批量取消需扩展DAO/Manager 查询与循环取消）
    }
}
