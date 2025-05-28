package com.ccxiaoji.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.app.data.sync.SyncStatus

@Entity(
    tableName = "recurring_transactions",
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
    indices = [Index("userId"), Index("accountId"), Index("categoryId"), Index("isEnabled"), Index("nextExecutionDate")]
)
data class RecurringTransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String, // 定期交易名称
    val accountId: String,
    val amountCents: Int,
    val categoryId: String,
    val note: String?,
    val frequency: RecurringFrequency,
    val dayOfWeek: Int? = null, // 1-7 for Monday-Sunday (when frequency is WEEKLY)
    val dayOfMonth: Int? = null, // 1-31 (when frequency is MONTHLY)
    val monthOfYear: Int? = null, // 1-12 (when frequency is YEARLY)
    val startDate: Long, // 开始日期
    val endDate: Long? = null, // 结束日期，null表示永不结束
    val isEnabled: Boolean = true,
    val lastExecutionDate: Long? = null,
    val nextExecutionDate: Long, // 下次执行日期
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class RecurringFrequency {
    DAILY,    // 每天
    WEEKLY,   // 每周
    MONTHLY,  // 每月
    YEARLY    // 每年
}