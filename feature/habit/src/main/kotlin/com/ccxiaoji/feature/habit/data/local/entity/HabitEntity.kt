package com.ccxiaoji.feature.habit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("updatedAt")]
)
data class HabitEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val period: String, // "daily", "weekly", "monthly"
    val target: Int = 1, // times per period
    val color: String = "#3A7AFE",
    val icon: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // ===== 新增字段（Phase 2）=====
    /**
     * 是否启用提醒
     * - null：继承全局配置
     * - true/false：单习惯配置
     */
    val reminderEnabled: Boolean? = null,

    /**
     * 提醒时间（HH:mm格式字符串）
     * - null：使用全局配置时间（如"20:00"）
     * - 非null：使用单习惯配置（如"07:00"）
     *
     * 示例：
     *   - 晨间跑步：reminderTime = "07:00"
     *   - 睡前阅读：reminderTime = "21:00"
     */
    val reminderTime: String? = null
)