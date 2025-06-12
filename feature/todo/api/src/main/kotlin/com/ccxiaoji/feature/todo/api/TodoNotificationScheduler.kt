package com.ccxiaoji.feature.todo.api

import kotlinx.datetime.Instant

/**
 * 通知调度器接口，需要在app模块中实现
 */
interface TodoNotificationScheduler {
    fun scheduleTaskReminder(taskId: String, taskTitle: String, dueAt: Instant)
    fun cancelTaskReminder(taskId: String)
}