package com.ccxiaoji.shared.notification.api

import kotlinx.datetime.Instant

/**
 * 通知模块对外API接口
 */
interface NotificationApi {
    /**
     * 发送任务提醒通知
     */
    fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String)
    
    /**
     * 发送习惯打卡提醒
     */
    fun sendHabitReminder(habitId: String, habitTitle: String)
    
    /**
     * 发送预算超支提醒
     */
    fun sendBudgetAlert(categoryName: String, percentage: Int)
    
    /**
     * 发送信用卡还款提醒
     */
    fun sendCreditCardReminder(
        cardId: String,
        cardName: String,
        debtAmount: String,
        daysUntilDue: Int,
        paymentDueDay: Int
    )
    
    /**
     * 发送通用通知
     */
    fun sendGeneralNotification(title: String, message: String, notificationId: Int? = null)
    
    /**
     * 取消通知
     */
    fun cancelNotification(notificationId: Int)
    
    /**
     * 取消所有通知
     */
    fun cancelAllNotifications()
    
    /**
     * 安排任务提醒
     */
    fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant)
    
    /**
     * 取消任务提醒
     */
    fun cancelTaskReminder(taskId: String)
    
    /**
     * 安排每日习惯提醒
     */
    fun scheduleDailyHabitReminder(habitId: String, habitTitle: String, reminderHour: Int, reminderMinute: Int)
    
    /**
     * 取消习惯提醒
     */
    fun cancelHabitReminder(habitId: String)
    
    /**
     * 安排每日检查
     */
    fun scheduleDailyCheck()
    
    /**
     * 取消所有定时通知
     */
    fun cancelAllScheduledNotifications()
}