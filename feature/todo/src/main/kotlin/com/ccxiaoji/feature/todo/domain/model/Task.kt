package com.ccxiaoji.feature.todo.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.minutes

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueAt: Instant?,
    val priority: Int, // 0: Low, 1: Medium, 2: High
    val completed: Boolean,
    val completedAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,

    // ===== 新增字段（Phase 2）=====
    val reminderEnabled: Boolean? = null,
    val reminderAt: Instant? = null,
    val reminderMinutesBefore: Int? = null,

    // ===== 新增字段（Phase 3 - 固定时间模式）=====
    /**
     * 固定时间提醒（HH:mm格式字符串）
     * - null：使用相对时间模式
     * - 非null：使用固定时间模式（例如："08:00"）
     */
    val reminderTime: String? = null
) {
    val priorityLevel: Priority
        get() = when (priority) {
            2 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        }

    /**
     * 计算实际提醒时间（核心业务逻辑）
     *
     * @param globalMinutes 全局配置的提前分钟数
     * @param globalEnabled 全局提醒是否启用
     * @return 实际提醒时间，null表示不提醒
     *
     * 优先级：
     * 1. 检查是否启用（单条配置 > 全局配置）
     * 2. 检查截止时间是否存在
     * 3. 使用绝对时间（如果设置）
     * 4. 使用相对时间（单条分钟数 > 全局分钟数）
     */
    fun getEffectiveReminderTime(
        globalMinutes: Int,
        globalEnabled: Boolean
    ): Instant? {
        // 步骤1：检查是否启用提醒
        val isEnabled = reminderEnabled ?: globalEnabled
        if (!isEnabled) return null

        // 步骤2：检查是否有截止时间
        val deadline = dueAt ?: return null

        // 步骤3：优先使用绝对时间
        reminderAt?.let { return it }

        // 步骤4：使用相对时间（单任务配置 > 全局配置）
        val minutesBefore = reminderMinutesBefore ?: globalMinutes
        return deadline.minus(minutesBefore.minutes)
    }
}

enum class Priority(val displayName: String, val color: Long) {
    LOW("低", 0xFF4CAF50),
    MEDIUM("中", 0xFFFF9800),
    HIGH("高", 0xFFF44336)
}