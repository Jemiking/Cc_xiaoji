package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.core.database.model.SyncStatus
import java.util.UUID

/**
 * 信用卡还款记录实体
 */
@Entity(
    tableName = "credit_card_payments",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["userId"]),
        Index(value = ["paymentDate"])
    ]
)
data class CreditCardPaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val accountId: String,
    val paymentAmountCents: Long,
    val paymentType: PaymentType, // FULL, MINIMUM, CUSTOM
    val paymentDate: Long,
    val dueAmountCents: Long, // 应还金额
    val isOnTime: Boolean,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

enum class PaymentType {
    FULL,     // 全额还款
    MINIMUM,  // 最低还款
    CUSTOM    // 自定义金额
}