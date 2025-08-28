package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

/**
 * 记账簿联动关系数据库实体
 */
@Entity(
    tableName = "ledger_links",
    foreignKeys = [
        ForeignKey(
            entity = LedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_ledger_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["child_ledger_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["parent_ledger_id"]),
        Index(value = ["child_ledger_id"]),
        Index(value = ["is_active"]),
        Index(value = ["parent_ledger_id", "child_ledger_id"], unique = true)
    ]
)
data class LedgerLinkEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "parent_ledger_id")
    val parentLedgerId: String,
    
    @ColumnInfo(name = "child_ledger_id")
    val childLedgerId: String,
    
    /**
     * 同步模式:
     * - BIDIRECTIONAL: 双向同步
     * - PARENT_TO_CHILD: 仅父记账簿到子记账簿
     * - CHILD_TO_PARENT: 仅子记账簿到父记账簿
     */
    @ColumnInfo(name = "sync_mode")
    val syncMode: String = "BIDIRECTIONAL",
    
    @ColumnInfo(name = "auto_sync_enabled")
    val autoSyncEnabled: Boolean = true,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant,
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
) {
    companion object
}

/**
 * 同步模式枚举
 */
enum class SyncMode(val value: String) {
    BIDIRECTIONAL("BIDIRECTIONAL"),
    PARENT_TO_CHILD("PARENT_TO_CHILD"),
    CHILD_TO_PARENT("CHILD_TO_PARENT");
    
    companion object {
        fun fromString(value: String): SyncMode {
            return values().find { it.value == value } ?: BIDIRECTIONAL
        }
    }
}