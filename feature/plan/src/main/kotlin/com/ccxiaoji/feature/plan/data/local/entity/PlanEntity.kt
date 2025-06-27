package com.ccxiaoji.feature.plan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 计划数据库实体
 */
@Entity(
    tableName = "plan_table",
    foreignKeys = [
        ForeignKey(
            entity = PlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parent_id"]),
        Index(value = ["status"]),
        Index(value = ["start_date", "end_date"])
    ]
)
data class PlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "parent_id")
    val parentId: String? = null,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "start_date")
    val startDate: Long,
    
    @ColumnInfo(name = "end_date")
    val endDate: Long,
    
    @ColumnInfo(name = "status")
    val status: String,
    
    @ColumnInfo(name = "progress")
    val progress: Float = 0f,
    
    @ColumnInfo(name = "color")
    val color: String = "#6650a4",
    
    @ColumnInfo(name = "priority")
    val priority: Int = 0,
    
    @ColumnInfo(name = "tags")
    val tags: String = "[]", // JSON数组
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "LOCAL",
    
    @ColumnInfo(name = "is_template")
    val isTemplate: Boolean = false,
    
    @ColumnInfo(name = "template_id")
    val templateId: String? = null,
    
    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0,
    
    @ColumnInfo(name = "reminder_settings")
    val reminderSettings: String? = null // JSON格式
)