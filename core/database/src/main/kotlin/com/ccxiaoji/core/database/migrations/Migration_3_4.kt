package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 从版本3迁移到版本4
 * - 添加信用卡账单表 (credit_card_bills)
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 创建信用卡账单表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS credit_card_bills (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                accountId TEXT NOT NULL,
                billStartDate INTEGER NOT NULL,
                billEndDate INTEGER NOT NULL,
                paymentDueDate INTEGER NOT NULL,
                totalAmountCents INTEGER NOT NULL,
                newChargesCents INTEGER NOT NULL,
                previousBalanceCents INTEGER NOT NULL,
                paymentsCents INTEGER NOT NULL,
                adjustmentsCents INTEGER NOT NULL,
                minimumPaymentCents INTEGER NOT NULL,
                isGenerated INTEGER NOT NULL DEFAULT 0,
                isPaid INTEGER NOT NULL DEFAULT 0,
                paidAmountCents INTEGER NOT NULL DEFAULT 0,
                isOverdue INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE
            )
        """)
        
        // 创建索引
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_bills_userId ON credit_card_bills(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_bills_accountId ON credit_card_bills(accountId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_bills_billStartDate ON credit_card_bills(billStartDate)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_bills_billEndDate ON credit_card_bills(billEndDate)")
    }
}