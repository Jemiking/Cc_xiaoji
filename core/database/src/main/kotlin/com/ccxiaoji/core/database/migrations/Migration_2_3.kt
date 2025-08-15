package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移 2 -> 3
 * 添加信用卡还款记录表
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 创建信用卡还款记录表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS credit_card_payments (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                accountId TEXT NOT NULL,
                paymentAmountCents INTEGER NOT NULL,
                paymentType TEXT NOT NULL,
                paymentDate INTEGER NOT NULL,
                dueAmountCents INTEGER NOT NULL,
                isOnTime INTEGER NOT NULL,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL,
                syncStatus TEXT NOT NULL,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE,
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 创建索引
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_payments_accountId ON credit_card_payments(accountId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_payments_userId ON credit_card_payments(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_payments_paymentDate ON credit_card_payments(paymentDate)")
    }
}