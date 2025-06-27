package com.ccxiaoji.feature.schedule.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 班次实体类
 * 用于定义不同的工作班次，如早班、中班、晚班等
 */
@Entity(tableName = "shifts")
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
    val syncStatus: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SYNC_STATUS_LOCAL = 0
        const val SYNC_STATUS_SYNCED = 1
        const val SYNC_STATUS_PENDING = 2
    }
}