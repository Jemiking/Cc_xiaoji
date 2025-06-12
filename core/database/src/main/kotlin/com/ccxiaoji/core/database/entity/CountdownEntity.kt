package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus

@Entity(
    tableName = "countdowns",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("targetDate"), Index("updatedAt")]
)
data class CountdownEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val title: String,
    val targetDate: Long, // Date in epoch millis
    val emoji: String? = "📅",
    val color: String = "#3A7AFE",
    val showOnWidget: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)