package com.ccxiaoji.app.data.local.migrations

import androidx.room.migration.Migration

/**
 * 数据库迁移管理类
 * 
 * 由于应用尚未发布，数据库版本已重置为1。
 * 所有历史迁移已清理，以最终正确的Schema重新开始。
 */
object DatabaseMigrations {
    
    /**
     * 获取所有迁移路径
     */
    fun getAllMigrations(): Array<Migration> {
        return arrayOf(
            MIGRATION_1_2, // Add credit card fields
            MIGRATION_2_3, // Add credit card payment history
            MIGRATION_3_4  // Add credit card bill management
        )
    }
}