package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 交易与记账簿关联关系数据库实体
 */
@Entity(
    tableName = "transaction_ledger_relations",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ledger_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["sync_source_ledger_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["ledger_id"]),
        Index(value = ["relation_type"]),
        Index(value = ["sync_source_ledger_id"]),
        Index(value = ["transaction_id", "ledger_id"], unique = true)
    ]
)
data class TransactionLedgerRelationEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    
    @ColumnInfo(name = "ledger_id")
    val ledgerId: String,
    
    /**
     * 关系类型:
     * - PRIMARY: 主要关系（原始交易所在的记账簿）
     * - SYNCED_FROM_PARENT: 从父记账簿同步过来的交易
     * - SYNCED_FROM_CHILD: 从子记账簿同步过来的交易
     */
    @ColumnInfo(name = "relation_type")
    val relationType: String = "PRIMARY",
    
    /**
     * 同步来源记账簿ID
     * 如果为空，表示这是原始交易
     * 如果不为空，表示这是从其他记账簿同步来的交易
     */
    @ColumnInfo(name = "sync_source_ledger_id")
    val syncSourceLedgerId: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Instant
) {
    companion object
}

/**
 * 交易关系类型枚举
 */
enum class TransactionRelationType(val value: String) {
    PRIMARY("PRIMARY"),
    SYNCED_FROM_PARENT("SYNCED_FROM_PARENT"),
    SYNCED_FROM_CHILD("SYNCED_FROM_CHILD");
    
    companion object {
        fun fromString(value: String): TransactionRelationType {
            return values().find { it.value == value } ?: PRIMARY
        }
    }
}