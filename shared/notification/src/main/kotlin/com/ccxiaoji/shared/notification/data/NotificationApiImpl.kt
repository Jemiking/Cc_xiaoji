package com.ccxiaoji.shared.notification.data

import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.manager.NotificationManager
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationApi的实现类
 * 注意：调度相关的方法由app模块的NotificationScheduler实现
 */
@Singleton
class NotificationApiImpl @Inject constructor(
    private val notificationManager: NotificationManager
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
    
    // 以下调度相关方法暂时抛出异常，实际调度由app模块的NotificationScheduler处理
    override fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
    
    override fun cancelTaskReminder(taskId: String) {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
    
    override fun scheduleDailyHabitReminder(habitId: String, habitTitle: String, reminderHour: Int, reminderMinute: Int) {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
    
    override fun cancelHabitReminder(habitId: String) {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
    
    override fun scheduleDailyCheck() {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
    
    override fun cancelAllScheduledNotifications() {
        throw UnsupportedOperationException("Scheduling should be handled by app module")
    }
}