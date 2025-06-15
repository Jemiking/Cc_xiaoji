package com.ccxiaoji.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus

/**
 * 班次实体类
 * 用于定义不同的工作班次，如早班、中班、晚班等
 */
@Entity(
    tableName = "shifts",
    indices = [
        Index(value = ["is_active"]),  // 用于快速查询活跃班次
        Index(value = ["is_active", "start_time"])  // 用于查询活跃班次并按时间排序
    ]
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "start_time")
    val startTime: String,
    
    @ColumnInfo(name = "end_time") 
    val endTime: String,
    
    @ColumnInfo(name = "color")
    val color: Int,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)