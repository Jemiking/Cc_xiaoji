package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus

@Entity(
    tableName = "change_log",
    indices = [Index("timestamp"), Index("tableName"), Index("syncStatus")]
)
data class ChangeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tableName: String,
    val rowId: String,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val payload: String, // JSON representation of the change
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
)