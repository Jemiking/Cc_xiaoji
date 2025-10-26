package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.shared.notification.api.NotificationApi
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Todo 模块通知用例：封装任务提醒的统一入口
 */
class TodoNotificationUseCase @Inject constructor(
    private val notificationApi: NotificationApi
) {
    /**
     * 安排任务提醒（传入提醒触发时间）
     */
    fun scheduleTaskReminder(taskId: String, taskTitle: String, remindAt: Instant) {
        notificationApi.scheduleTaskReminder(taskId, taskTitle, remindAt)
    }

    /** 取消某任务的提醒 */
    fun cancelTaskReminder(taskId: String) {
        notificationApi.cancelTaskReminder(taskId)
    }
}

