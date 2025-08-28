package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库迁移 15 -> 16
 * 添加转账功能所需的字段
 * 
 * 扩展表：
 * 1. transactions - 添加转账相关字段
 *    - transferId: 转账批次ID，关联转账的两笔记录
 *    - transferType: 转账类型 (TRANSFER_OUT/TRANSFER_IN)  
 *    - relatedTransactionId: 关联的另一笔转账记录ID
 *    - allowNullCategory: 是否允许空分类（用于转账）
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(database: SupportSQLiteDatabase) {
        
        // 1. 为transactions表添加转账相关字段
        database.execSQL("ALTER TABLE transactions ADD COLUMN transferId TEXT")
        database.execSQL("ALTER TABLE transactions ADD COLUMN transferType TEXT")
        database.execSQL("ALTER TABLE transactions ADD COLUMN relatedTransactionId TEXT")
        
        // 2. 为转账字段创建新索引
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_transferId ON transactions(transferId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_transferType ON transactions(transferType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_relatedTransactionId ON transactions(relatedTransactionId)")
        
        // 3. 为转账查询创建组合索引
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_transfer_lookup ON transactions(transferId, transferType)")
        
        // 4. 插入转账分类（如果不存在）
        val currentTime = System.currentTimeMillis()
        
        // 检查是否存在转账分类，如果不存在则创建
        database.execSQL("""
            INSERT OR IGNORE INTO categories (
                id, name, icon, color, type, userId, parentId, createdAt, updatedAt
            ) VALUES (
                'TRANSFER_CATEGORY', '转账', '💸', '#2196F3', 'TRANSFER', 'system', NULL, ?, ?
            )
        """.trimIndent(), arrayOf(currentTime, currentTime))
    }
}