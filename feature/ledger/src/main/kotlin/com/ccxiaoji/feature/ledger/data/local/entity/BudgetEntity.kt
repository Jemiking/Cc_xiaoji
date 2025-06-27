package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

@Entity(
    tableName = "budgets",
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
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"),
        Index("categoryId"),
        Index(value = ["userId", "year", "month", "categoryId"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val year: Int,
    val month: Int,
    val categoryId: String?, // null for total budget
    val budgetAmountCents: Int,
    val alertThreshold: Float = 0.8f, // Alert when budget usage reaches 80%
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)