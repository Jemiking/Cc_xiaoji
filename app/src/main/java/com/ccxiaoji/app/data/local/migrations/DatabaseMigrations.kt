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
     * 
     * 当前返回空数组，因为应用从版本1开始，
     * 没有需要迁移的历史版本。
     */
    fun getAllMigrations(): Array<Migration> {
        return emptyArray()
    }
}