package com.ccxiaoji.feature.todo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("dueAt"), Index("updatedAt")]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val dueAt: Long?,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // ===== 新增字段（Phase 2）=====
    /**
     * 是否启用提醒
     * - null：继承全局配置（默认值，旧数据迁移后为null）
     * - true：强制启用（即使全局关闭也提醒）
     * - false：强制禁用（即使全局开启也不提醒）
     */
    val reminderEnabled: Boolean? = null,

    /**
     * 提醒时间（绝对时间，毫秒时间戳）
     * - null：使用相对时间（截止时间 - reminderMinutesBefore）
     * - 非null：使用绝对时间（如提前1天，设置为具体时间戳）
     */
    val reminderAt: Long? = null,

    /**
     * 提前提醒分钟数
     * - null：使用全局配置的分钟数（如30分钟）
     * - 非null：使用单任务配置（如60分钟）
     *
     * 优先级：reminderAt > reminderMinutesBefore > 全局配置
     */
    val reminderMinutesBefore: Int? = null,

    // ===== 新增字段（Phase 3 - 固定时间模式）=====
    /**
     * 固定时间提醒（HH:mm格式字符串）
     * - null：使用相对时间模式
     * - 非null：使用固定时间模式（例如："08:00"表示每天早上8点提醒）
     *
     * 当此字段非空时，reminderAt将根据dueAt动态计算
     * 例如：任务截止日期为2024-12-31，reminderTime为"08:00"
     *       则reminderAt = 2024-12-31 08:00:00的时间戳
     */
    val reminderTime: String? = null
)