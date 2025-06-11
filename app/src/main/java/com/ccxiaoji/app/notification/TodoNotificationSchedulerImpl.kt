package com.ccxiaoji.app.notification

import com.ccxiaoji.feature.todo.api.TodoNotificationScheduler
import com.ccxiaoji.shared.notification.api.NotificationApi
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TodoNotificationScheduler的实现
 * 在app模块中实现feature-todo模块的通知调度接口
 */
@Singleton
class TodoNotificationSchedulerImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : TodoNotificationScheduler {
    
    override fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant) {
        runBlocking {
            notificationApi.scheduleTaskReminder(taskId, taskTitle, dueAt)
        }
    }
    
    override fun cancelTaskReminder(taskId: String) {
        runBlocking {
            notificationApi.cancelTaskReminder(taskId)
        }
    }
}