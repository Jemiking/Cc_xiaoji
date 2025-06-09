package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: LocalDate?,
    val description: String? = null,
    val color: String = "#4CAF50", // Default green color
    val iconName: String = "savings", // Icon identifier
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val syncStatus: SyncStatus = SyncStatus.PENDING
)