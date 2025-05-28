package com.ccxiaoji.app.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建categories表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                icon TEXT NOT NULL,
                color TEXT NOT NULL,
                type INTEGER NOT NULL,
                parentId TEXT,
                isSystem INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(parentId) REFERENCES categories(id) ON DELETE CASCADE
            )
        """)
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_userId ON categories(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_parentId ON categories(parentId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_type ON categories(type)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_isSystem ON categories(isSystem)")
        
        // 在transactions表中添加categoryId列
        database.execSQL("ALTER TABLE transactions ADD COLUMN categoryId TEXT")
        
        // 创建categoryId的索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
    }
}