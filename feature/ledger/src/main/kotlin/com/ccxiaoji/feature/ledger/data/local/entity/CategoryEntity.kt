package com.ccxiaoji.feature.ledger.data.local.entity

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
        Index("displayOrder"),
        Index("level"),
        Index(value = ["userId", "name", "parentId"], unique = true)
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
    val level: Int = 1, // 分类层级：1-一级分类，2-二级分类
    val path: String = "", // 完整路径，如"餐饮/早餐"
    val displayOrder: Int = 0,
    val isDefault: Boolean = false, // 是否为系统预设分类
    val isActive: Boolean = true, // 是否启用
    val isSystem: Boolean = false, // System categories cannot be deleted
    val usageCount: Long = 0, // Track how many times this category is used
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)