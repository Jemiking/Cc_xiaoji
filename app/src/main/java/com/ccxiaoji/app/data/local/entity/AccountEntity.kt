package com.ccxiaoji.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.app.data.sync.SyncStatus

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("createdAt"), Index("updatedAt")]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val type: String, // CASH, BANK_CARD, ALIPAY, WECHAT, CREDIT_CARD, OTHER
    val balanceCents: Long, // Balance in cents
    val currency: String = "CNY",
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)