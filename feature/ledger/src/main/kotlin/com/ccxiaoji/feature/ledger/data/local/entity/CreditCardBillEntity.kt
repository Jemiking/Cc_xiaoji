package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

/**
 * 信用卡账单实体
 */
@Entity(
    tableName = "credit_card_bills",
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
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["accountId"]),
        Index(value = ["billStartDate"]),
        Index(value = ["billEndDate"])
    ]
)
data class CreditCardBillEntity(
    @PrimaryKey
    val id: String,
    
    // 外键
    val userId: String,
    val accountId: String,
    
    // 账单周期
    val billStartDate: Long, // 账单开始日期（时间戳）
    val billEndDate: Long,   // 账单结束日期（时间戳）
    val paymentDueDate: Long, // 还款到期日（时间戳）
    
    // 账单金额（单位：分）
    val totalAmountCents: Long,      // 账单总金额
    val newChargesCents: Long,       // 本期新增消费
    val previousBalanceCents: Long,  // 上期结余
    val paymentsCents: Long,         // 本期还款
    val adjustmentsCents: Long,      // 调整金额（退款、手续费等）
    val minimumPaymentCents: Long,   // 最低还款额
    
    // 账单状态
    val isGenerated: Boolean = false,   // 是否已生成（账单日到达后生成）
    val isPaid: Boolean = false,        // 是否已还清
    val paidAmountCents: Long = 0,      // 已还金额
    val isOverdue: Boolean = false,     // 是否逾期
    
    // 元数据
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    // 计算属性
    val remainingAmountCents: Long
        get() = totalAmountCents - paidAmountCents
    
    val totalAmountYuan: Double
        get() = totalAmountCents / 100.0
    
    val minimumPaymentYuan: Double
        get() = minimumPaymentCents / 100.0
    
    val remainingAmountYuan: Double
        get() = remainingAmountCents / 100.0
}