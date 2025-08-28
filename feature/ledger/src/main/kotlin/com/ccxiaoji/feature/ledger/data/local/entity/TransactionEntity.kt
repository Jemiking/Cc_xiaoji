package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.user.data.local.entity.UserEntity

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
        ),
        ForeignKey(
            entity = LedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ledgerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("userId"), Index("accountId"), Index("categoryId"), Index("ledgerId"), 
        Index("createdAt"), Index("updatedAt"), Index("transactionDate"),
        Index("sourceType"), Index("confidence"), Index(value = ["sourceApp", "postedTime"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val accountId: String,
    val amountCents: Int,
    val categoryId: String, // 外键引用 categories 表
    val note: String?,
    val ledgerId: String, // 外键引用 ledgers 表
    val createdAt: Long, // 记录创建时间
    val updatedAt: Long, // 记录修改时间
    val transactionDate: Long? = null, // 交易实际发生时间
    val locationLatitude: Double? = null, // 位置纬度
    val locationLongitude: Double? = null, // 位置经度
    val locationAddress: String? = null, // 位置地址
    val locationPrecision: Float? = null, // 位置精度
    val locationProvider: String? = null, // 位置提供者
    val isDeleted: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    // 自动记账相关字段 (Migration_14_15)
    val sourceApp: String? = null, // 来源应用包名
    val sourceType: String? = null, // 来源类型（alipay/wechat等）
    val postedTime: Long? = null, // 通知发布时间
    val parserVersion: Int? = 1, // 解析器版本
    val confidence: Double? = null, // 解析置信度
    val accountGuess: String? = null, // 账户推荐
    // 转账相关字段 (Migration_15_16)
    val transferId: String? = null, // 转账批次ID
    val transferType: String? = null, // 转账类型
    val relatedTransactionId: String? = null // 关联的另一笔转账记录ID
)