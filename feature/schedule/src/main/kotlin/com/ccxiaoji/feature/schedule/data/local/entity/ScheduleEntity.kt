package com.ccxiaoji.feature.schedule.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 排班实体类
 * 记录某一天的排班信息
 */
@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shift_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["shift_id"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: Long,
    
    @ColumnInfo(name = "shift_id")
    val shiftId: Long,
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "actual_start_time")
    val actualStartTime: String? = null,
    
    @ColumnInfo(name = "actual_end_time")
    val actualEndTime: String? = null,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)