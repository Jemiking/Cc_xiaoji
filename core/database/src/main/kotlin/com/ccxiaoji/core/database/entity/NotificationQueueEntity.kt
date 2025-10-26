package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 通知队列实体（跨模块通用）
 * - 采用字符串枚举持久化（通过 Converters 转换）
 * - 提供必要索引以支撑查询与去重
 */
@Entity(
    tableName = "notification_queue",
    indices = [
        Index(value = ["status", "scheduledAt"]),
        Index(value = ["sourceModule", "sourceId"]) // 后续去重策略可基于此
    ]
)
data class NotificationQueueEntity(
    @PrimaryKey val id: String,
    val type: NotificationType,      // TASK, HABIT, BUDGET, CREDIT_CARD, SCHEDULE, GENERAL
    val sourceModule: String,        // "todo", "habit", "ledger", "schedule", "plan" 等
    val sourceId: String?,           // 原始记录ID（可选）
    val title: String,
    val message: String,
    val scheduledAt: Long,           // 计划发送时间（epoch millis）
    val status: NotificationStatus,  // PENDING, PROCESSING, SENT, FAILED, CANCELLED
    val workerId: String?,           // WorkManager 任务ID（UUID字符串，可选）
    val attempts: Int = 0,
    val createdAt: Long,
    val sentAt: Long? = null,
    val userId: String               // 当前用户ID（本地单用户可使用固定值）
)

enum class NotificationType {
    TASK,
    HABIT,
    BUDGET,
    CREDIT_CARD,
    SCHEDULE,
    GENERAL
}

enum class NotificationStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    CANCELLED
}

