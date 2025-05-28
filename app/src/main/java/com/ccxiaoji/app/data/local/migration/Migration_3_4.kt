package com.ccxiaoji.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建budgets表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS budgets (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                year INTEGER NOT NULL,
                month INTEGER NOT NULL,
                categoryId TEXT,
                budgetAmountCents INTEGER NOT NULL,
                alertThreshold REAL NOT NULL DEFAULT 0.8,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE CASCADE
            )
        """)
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_userId ON budgets(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_userId_year_month_categoryId ON budgets(userId, year, month, categoryId)")
    }
}