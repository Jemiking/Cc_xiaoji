package com.ccxiaoji.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ccxiaoji.app.data.local.migration.MIGRATION_6_7
import java.util.UUID

/**
 * 数据库迁移管理类
 * 记录所有的数据库版本迁移逻辑，确保用户数据不会丢失
 */
object DatabaseMigrations {
    
    /**
     * 版本1到2：添加账户管理功能
     * - 创建accounts表
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建accounts表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    balance INTEGER NOT NULL,
                    icon TEXT,
                    color TEXT,
                    isDefault INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_userId ON accounts(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_createdAt ON accounts(createdAt)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_updatedAt ON accounts(updatedAt)")
        }
    }
    
    /**
     * 版本2到3：添加分类管理功能
     * - 创建categories表
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建categories表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    icon TEXT,
                    color TEXT,
                    parentId TEXT,
                    isSystem INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(parentId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_userId ON categories(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_parentId ON categories(parentId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_categories_type ON categories(type)")
        }
    }
    
    /**
     * 版本3到4：升级交易表结构
     * - 为transactions表添加accountId和categoryId
     * - 创建默认账户和分类
     * - 迁移现有数据
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. 先为每个用户创建默认账户
            val defaultAccountId = UUID.randomUUID().toString()
            val currentTime = System.currentTimeMillis()
            
            database.execSQL("""
                INSERT INTO accounts (id, userId, name, type, balance, icon, color, isDefault, sortOrder, createdAt, updatedAt, isDeleted, syncStatus)
                SELECT 
                    '$defaultAccountId' as id,
                    id as userId,
                    '默认账户' as name,
                    'CASH' as type,
                    0 as balance,
                    '💰' as icon,
                    '#4CAF50' as color,
                    1 as isDefault,
                    0 as sortOrder,
                    $currentTime as createdAt,
                    $currentTime as updatedAt,
                    0 as isDeleted,
                    'SYNCED' as syncStatus
                FROM users
                WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE userId = users.id AND isDefault = 1)
            """.trimIndent())
            
            // 2. 创建临时表存储交易数据
            database.execSQL("""
                CREATE TABLE transactions_temp (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    accountId TEXT NOT NULL,
                    amountCents INTEGER NOT NULL,
                    categoryId TEXT NOT NULL,
                    category TEXT,
                    note TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())
            
            // 3. 为每个用户创建默认分类
            val expenseCategoryId = UUID.randomUUID().toString()
            val incomeCategoryId = UUID.randomUUID().toString()
            
            database.execSQL("""
                INSERT INTO categories (id, userId, name, type, icon, color, parentId, isSystem, sortOrder, createdAt, updatedAt, isDeleted, syncStatus)
                SELECT 
                    '$expenseCategoryId' as id,
                    id as userId,
                    '其他支出' as name,
                    'EXPENSE' as type,
                    '💸' as icon,
                    '#F44336' as color,
                    NULL as parentId,
                    1 as isSystem,
                    999 as sortOrder,
                    $currentTime as createdAt,
                    $currentTime as updatedAt,
                    0 as isDeleted,
                    'SYNCED' as syncStatus
                FROM users
            """.trimIndent())
            
            database.execSQL("""
                INSERT INTO categories (id, userId, name, type, icon, color, parentId, isSystem, sortOrder, createdAt, updatedAt, isDeleted, syncStatus)
                SELECT 
                    '$incomeCategoryId' as id,
                    id as userId,
                    '其他收入' as name,
                    'INCOME' as type,
                    '💰' as icon,
                    '#4CAF50' as color,
                    NULL as parentId,
                    1 as isSystem,
                    999 as sortOrder,
                    $currentTime as createdAt,
                    $currentTime as updatedAt,
                    0 as isDeleted,
                    'SYNCED' as syncStatus
                FROM users
            """.trimIndent())
            
            // 4. 迁移数据到临时表，根据金额正负分配分类
            database.execSQL("""
                INSERT INTO transactions_temp
                SELECT 
                    t.id,
                    t.userId,
                    COALESCE(a.id, '$defaultAccountId') as accountId,
                    t.amountCents,
                    CASE 
                        WHEN t.amountCents < 0 THEN '$expenseCategoryId'
                        ELSE '$incomeCategoryId'
                    END as categoryId,
                    t.category,
                    t.note,
                    t.createdAt,
                    t.updatedAt,
                    t.isDeleted,
                    t.syncStatus
                FROM transactions t
                LEFT JOIN accounts a ON a.userId = t.userId AND a.isDefault = 1
            """.trimIndent())
            
            // 5. 删除原表并重命名临时表
            database.execSQL("DROP TABLE transactions")
            database.execSQL("ALTER TABLE transactions_temp RENAME TO transactions")
            
            // 6. 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_accountId ON transactions(accountId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_categoryId ON transactions(categoryId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_createdAt ON transactions(createdAt)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_updatedAt ON transactions(updatedAt)")
        }
    }
    
    /**
     * 版本4到5：添加预算和循环交易功能
     * - 创建budgets表
     * - 创建recurring_transactions表
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建budgets表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    categoryId TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    period TEXT NOT NULL,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建recurring_transactions表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS recurring_transactions (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    accountId TEXT NOT NULL,
                    categoryId TEXT NOT NULL,
                    amountCents INTEGER NOT NULL,
                    description TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    dayOfMonth INTEGER,
                    dayOfWeek INTEGER,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER,
                    lastProcessedDate INTEGER,
                    nextDueDate INTEGER NOT NULL,
                    isActive INTEGER NOT NULL DEFAULT 1,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(accountId) REFERENCES accounts(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(categoryId) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_userId ON budgets(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_userId ON recurring_transactions(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_transactions_nextDueDate ON recurring_transactions(nextDueDate)")
        }
    }
    
    /**
     * 版本5到6：添加储蓄目标功能
     * - 创建savings_goals表
     * - 创建savings_contributions表
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 创建savings_goals表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS savings_goals (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    targetAmount INTEGER NOT NULL,
                    currentAmount INTEGER NOT NULL DEFAULT 0,
                    targetDate INTEGER,
                    icon TEXT,
                    color TEXT,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    completedAt INTEGER,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(userId) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建savings_contributions表
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS savings_contributions (
                    id TEXT NOT NULL PRIMARY KEY,
                    savingsGoalId TEXT NOT NULL,
                    amount INTEGER NOT NULL,
                    note TEXT,
                    contributedAt INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    isDeleted INTEGER NOT NULL DEFAULT 0,
                    syncStatus TEXT NOT NULL DEFAULT 'SYNCED',
                    FOREIGN KEY(savingsGoalId) REFERENCES savings_goals(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            
            // 创建索引
            database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_goals_userId ON savings_goals(userId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_contributions_savingsGoalId ON savings_contributions(savingsGoalId)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_contributions_contributedAt ON savings_contributions(contributedAt)")
        }
    }
    
    /**
     * 获取所有迁移路径
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7
        )
    }
}