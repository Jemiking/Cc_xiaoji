package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus

@Entity(
    tableName = "habit_records",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habitId"),
        Index("recordDate"),
        Index(value = ["habitId", "recordDate"], unique = true)
    ]
)
data class HabitRecordEntity(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val recordDate: Long, // Date in epoch millis (normalized to start of day)
    val count: Int = 1, // Number of times completed that day
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)