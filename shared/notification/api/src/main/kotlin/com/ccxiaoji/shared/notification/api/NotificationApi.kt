package com.ccxiaoji.shared.notification.api

import kotlinx.datetime.Instant

/**
 * 通知管理API接口
 * 提供通知相关功能的对外接口
 */
interface NotificationApi {
    
    // ========== 通知发送功能 ==========
    
    /**
     * 发送任务提醒通知
     */
    suspend fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String)
    
    /**
     * 发送习惯打卡提醒
     */
    suspend fun sendHabitReminder(habitId: String, habitTitle: String)
    
    /**
     * 发送预算超支提醒
     */
    suspend fun sendBudgetAlert(categoryName: String, percentage: Int)
    
    /**
     * 发送信用卡还款提醒
     */
    suspend fun sendCreditCardReminder(
        cardId: String,
        cardName: String,
        debtAmount: String,
        daysUntilDue: Int,
        paymentDueDay: Int
    )
    
    /**
     * 发送通用通知
     */
    suspend fun sendGeneralNotification(title: String, message: String, notificationId: Int? = null)
    
    // ========== 通知调度功能 ==========
    
    /**
     * 安排任务提醒
     */
    suspend fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant)
    
    /**
     * 取消任务提醒
     */
    suspend fun cancelTaskReminder(taskId: String)
    
    /**
     * 安排每日习惯提醒
     */
    suspend fun scheduleDailyHabitReminder(
        habitId: String,
        habitTitle: String,
        reminderHour: Int,
        reminderMinute: Int
    )
    
    /**
     * 取消习惯提醒
     */
    suspend fun cancelHabitReminder(habitId: String)
    
    /**
     * 安排每日检查（用于检查预算等）
     */
    suspend fun scheduleDailyCheck()
    
    // ========== 通知管理功能 ==========
    
    /**
     * 取消指定通知
     */
    suspend fun cancelNotification(notificationId: Int)
    
    /**
     * 取消所有通知
     */
    suspend fun cancelAllNotifications()
    
    /**
     * 取消所有计划的通知
     */
    suspend fun cancelAllScheduledNotifications()
    
    /**
     * 初始化通知通道
     * 在应用启动时调用
     */
    suspend fun initializeNotificationChannels()
}