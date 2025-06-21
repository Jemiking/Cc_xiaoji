package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 开发环境专用迁移
 * 仅用于开发阶段，发布前必须移除或禁用
 * 
 * 这些迁移允许从旧版本"升级"到版本1，实际上是重置数据库结构
 * 但保留了部分关键数据（如账户、分类等）
 */
object DevMigrations {
    
    /**
     * 从版本7迁移到版本1
     * 保留账户和新分类数据
     */
    val MIGRATION_7_1 = object : Migration(7, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. 备份需要保留的数据
            database.execSQL("""
                CREATE TABLE accounts_backup AS 
                SELECT * FROM accounts
            """)
            
            database.execSQL("""
                CREATE TABLE categories_backup AS 
                SELECT * FROM categories
            """)
            
            // 2. 删除旧表
            database.execSQL("DROP TABLE IF EXISTS transactions")
            database.execSQL("DROP TABLE IF EXISTS accounts")
            database.execSQL("DROP TABLE IF EXISTS categories")
            // ... 其他表
            
            // 3. 创建新表结构（与版本1相同）
            // 这里应该与 Room 自动生成的 Schema 完全一致
            createVersion1Tables(database)
            
            // 4. 恢复数据
            database.execSQL("""
                INSERT INTO accounts 
                SELECT * FROM accounts_backup
            """)
            
            database.execSQL("""
                INSERT INTO categories 
                SELECT * FROM categories_backup  
            """)
            
            // 5. 清理
            database.execSQL("DROP TABLE accounts_backup")
            database.execSQL("DROP TABLE categories_backup")
        }
    }
    
    /**
     * 从版本8迁移到版本1
     */
    val MIGRATION_8_1 = object : Migration(8, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 与 7->1 类似的逻辑
            MIGRATION_7_1.migrate(database)
        }
    }
    
    private fun createVersion1Tables(database: SupportSQLiteDatabase) {
        // 注意：这些 SQL 应该从 app/schemas/1.json 中复制
        // 确保与 Room 生成的完全一致
        
        // users 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS users (
                id TEXT NOT NULL PRIMARY KEY,
                email TEXT NOT NULL,
                nickname TEXT,
                avatarUrl TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // accounts 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS accounts (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                balanceCents INTEGER NOT NULL,
                currency TEXT NOT NULL,
                icon TEXT,
                color TEXT,
                isDefault INTEGER NOT NULL,
                displayOrder INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
            )
        """)
        
        // categories 表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                icon TEXT,
                color TEXT,
                parentId TEXT,
                displayOrder INTEGER NOT NULL DEFAULT 0,
                isSystem INTEGER NOT NULL DEFAULT 0,
                usageCount INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(parentId) REFERENCES categories(id) ON DELETE SET NULL
            )
        """)
        
        // transactions 表 - 注意没有 category 字段
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions (
                id TEXT NOT NULL PRIMARY KEY,
                userId TEXT NOT NULL,
                accountId TEXT NOT NULL,
                amountCents INTEGER NOT NULL,
                categoryId TEXT NOT NULL,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                isDeleted INTEGER NOT NULL DEFAULT 0,
                syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE,
                FOREIGN KEY(categoryId) REFERENCES categories(id) ON DELETE RESTRICT
            )
        """)
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_userId ON accounts(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_userId ON categories(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions(accountId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_createdAt ON transactions(createdAt)")
        
        // ... 其他表和索引
    }
}