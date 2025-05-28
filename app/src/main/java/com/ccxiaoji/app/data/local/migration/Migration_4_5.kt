package com.ccxiaoji.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建定期交易表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS recurring_transactions (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                accountId TEXT NOT NULL,
                amountCents INTEGER NOT NULL,
                categoryId TEXT NOT NULL,
                note TEXT,
                frequency TEXT NOT NULL,
                dayOfWeek INTEGER,
                dayOfMonth INTEGER,
                monthOfYear INTEGER,
                startDate INTEGER NOT NULL,
                endDate INTEGER,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                lastExecutionDate INTEGER,
                nextExecutionDate INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
            )
        """)
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_userId ON recurring_transactions(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_accountId ON recurring_transactions(accountId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_categoryId ON recurring_transactions(categoryId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_isEnabled ON recurring_transactions(isEnabled)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_nextExecutionDate ON recurring_transactions(nextExecutionDate)")
    }
}