package com.ccxiaoji.app.notification

import com.ccxiaoji.feature.todo.api.TodoNotificationScheduler
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TodoNotificationScheduler的实现
 * 在app模块中实现feature-todo模块的通知调度接口
 */
@Singleton
class TodoNotificationSchedulerImpl @Inject constructor(
    private val notificationScheduler: NotificationScheduler
) : TodoNotificationScheduler {
    
    override fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        notificationScheduler.scheduleTaskReminder(taskId, taskTitle, dueAt)
    }
    
    override fun cancelTaskReminder(taskId: String) {
        notificationScheduler.cancelTaskReminder(taskId)
    }
}