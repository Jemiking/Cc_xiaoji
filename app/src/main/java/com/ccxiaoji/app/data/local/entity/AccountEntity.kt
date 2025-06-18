package com.ccxiaoji.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

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
    val balanceCents: Long, // Balance in cents (for credit cards, negative means debt)
    val currency: String = "CNY",
    val icon: String? = null,
    val color: String? = null,
    val isDefault: Boolean = false,
    
    // Credit card specific fields
    val creditLimitCents: Long? = null, // Credit limit in cents
    val billingDay: Int? = null, // Day of month for billing (1-28)
    val paymentDueDay: Int? = null, // Day of month for payment due (1-28)
    val gracePeriodDays: Int? = null, // Grace period days after billing
    
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)