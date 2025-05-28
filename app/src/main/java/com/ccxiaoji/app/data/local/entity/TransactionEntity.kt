package com.ccxiaoji.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.app.data.sync.SyncStatus

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("userId"), Index("accountId"), Index("categoryId"), Index("createdAt"), Index("updatedAt")]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 外键引用 categories 表
    val category: String? = null, // 保留用于向后兼容，将来移除
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)