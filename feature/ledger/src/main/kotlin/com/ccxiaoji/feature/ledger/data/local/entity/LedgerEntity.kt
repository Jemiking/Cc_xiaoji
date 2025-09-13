package com.ccxiaoji.feature.ledger.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * 账本数据库实体
 */
@Entity(
    tableName = "ledgers",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "isDefault"]),
        Index(value = ["userId", "displayOrder"])
    ]
)
data class LedgerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "userId")
    val userId: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String? = null,
    
    @ColumnInfo(name = "color")
    val color: String = "#3A7AFE",
    
    @ColumnInfo(name = "icon")
    val icon: String = "book",
    
    @ColumnInfo(name = "isDefault")
    val isDefault: Boolean = false,
    
    @ColumnInfo(name = "displayOrder")
    val displayOrder: Int = 0,
    
    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "createdAt")
    val createdAt: Instant,
    
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Instant
) {
    fun toDomain(): Ledger = Ledger(
        id = id,
        userId = userId,
        name = name,
        description = description,
        color = color,
        icon = icon,
        isDefault = isDefault,
        displayOrder = displayOrder,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    companion object {
        fun fromDomain(ledger: Ledger): LedgerEntity = LedgerEntity(
            id = ledger.id,
            userId = ledger.userId,
            name = ledger.name,
            description = ledger.description,
            color = ledger.color,
            icon = ledger.icon,
            isDefault = ledger.isDefault,
            displayOrder = ledger.displayOrder,
            isActive = ledger.isActive,
            createdAt = ledger.createdAt,
            updatedAt = ledger.updatedAt
        )
        
        fun createDefault(userId: String): LedgerEntity {
            val now = Clock.System.now()
            return LedgerEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = Ledger.DEFAULT_LEDGER_NAME,
                description = Ledger.DEFAULT_LEDGER_DESCRIPTION,
                color = "#3A7AFE",
                icon = "book",
                isDefault = true,
                displayOrder = 0,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * 账本统计数据查询结果
 */
data class LedgerWithStatsEntity(
    @Embedded val ledger: LedgerEntity,
    val transactionCount: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val lastTransactionDate: Instant?
)
