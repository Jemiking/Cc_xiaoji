package com.ccxiaoji.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("parentId"),
        Index("type"),
        Index("displayOrder")
    ]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val icon: String = "📝",
    val color: String = "#6200EE", // Hex color
    val parentId: String? = null,
    val displayOrder: Int = 0,
    val isSystem: Boolean = false, // System categories cannot be deleted
    val usageCount: Long = 0, // Track how many times this category is used
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)