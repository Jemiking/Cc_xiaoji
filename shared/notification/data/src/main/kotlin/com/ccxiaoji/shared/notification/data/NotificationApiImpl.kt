package com.ccxiaoji.shared.notification.data

import com.ccxiaoji.shared.notification.api.NotificationApi
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NotificationApi的实现类
 * 整合NotificationManager和NotificationScheduler的功能
 */
@Singleton
class NotificationApiImpl @Inject constructor(
    private val notificationManager: NotificationManager,
    private val notificationScheduler: NotificationScheduler
) : NotificationApi {
    
    // ========== 通知发送功能 ==========
    
    override suspend fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String) {
        notificationManager.sendTaskReminder(taskId, taskTitle, dueTime)
    }
    
    override suspend fun sendHabitReminder(habitId: String, habitTitle: String) {
        notificationManager.sendHabitReminder(habitId, habitTitle)
    }
    
    override suspend fun sendBudgetAlert(categoryName: String, percentage: Int) {
        notificationManager.sendBudgetAlert(categoryName, percentage)
    }
    
    override suspend fun sendCreditCardReminder(
        cardId: String,
        cardName: String,
        debtAmount: String,
        daysUntilDue: Int,
        paymentDueDay: Int
    ) {
        notificationManager.sendCreditCardReminder(
            cardId,
            cardName,
            debtAmount,
            daysUntilDue,
            paymentDueDay
        )
    }
    
    override suspend fun sendGeneralNotification(
        title: String,
        message: String,
        notificationId: Int?
    ) {
        if (notificationId != null) {
            notificationManager.sendGeneralNotification(title, message, notificationId)
        } else {
            notificationManager.sendGeneralNotification(title, message)
        }
    }
    
    // ========== 通知调度功能 ==========
    
    override suspend fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        notificationScheduler.scheduleTaskReminder(taskId, taskTitle, dueAt)
    }
    
    override suspend fun cancelTaskReminder(taskId: String) {
        notificationScheduler.cancelTaskReminder(taskId)
    }
    
    override suspend fun scheduleDailyHabitReminder(
        habitId: String,
        habitTitle: String,
        reminderHour: Int,
        reminderMinute: Int
    ) {
        notificationScheduler.scheduleDailyHabitReminder(
            habitId,
            habitTitle,
            reminderHour,
            reminderMinute
        )
    }
    
    override suspend fun cancelHabitReminder(habitId: String) {
        notificationScheduler.cancelHabitReminder(habitId)
    }
    
    override suspend fun scheduleDailyCheck() {
        notificationScheduler.scheduleDailyCheck()
    }
    
    // ========== 通知管理功能 ==========
    
    override suspend fun cancelNotification(notificationId: Int) {
        notificationManager.cancelNotification(notificationId)
    }
    
    override suspend fun cancelAllNotifications() {
        notificationManager.cancelAllNotifications()
    }
    
    override suspend fun cancelAllScheduledNotifications() {
        notificationScheduler.cancelAllScheduledNotifications()
    }
    
    override suspend fun initializeNotificationChannels() {
        // NotificationManager 在初始化时已经创建了通知渠道
        // 这里可以添加额外的初始化逻辑
        notificationManager.createNotificationChannels()
    }
}