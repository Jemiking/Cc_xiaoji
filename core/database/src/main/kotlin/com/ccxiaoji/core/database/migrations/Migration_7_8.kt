package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为 savings_goals 表添加 userId 列
        // 首先创建新表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `savings_goals_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `targetAmount` REAL NOT NULL,
                `currentAmount` REAL NOT NULL,
                `targetDate` TEXT,
                `description` TEXT,
                `color` TEXT NOT NULL,
                `iconName` TEXT NOT NULL,
                `isActive` INTEGER NOT NULL,
                `createdAt` TEXT NOT NULL,
                `updatedAt` TEXT NOT NULL,
                `syncStatus` TEXT NOT NULL,
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_savings_goals_new_userId` ON `savings_goals_new` (`userId`)")
        
        // 从旧表复制数据，为现有记录使用默认用户ID
        database.execSQL("""
            INSERT INTO `savings_goals_new` (
                `id`, `userId`, `name`, `targetAmount`, `currentAmount`, 
                `targetDate`, `description`, `color`, `iconName`, `isActive`, 
                `createdAt`, `updatedAt`, `syncStatus`
            )
            SELECT 
                `id`, 
                'default_user' as `userId`,
                `name`, 
                `targetAmount`, 
                `currentAmount`, 
                `targetDate`, 
                `description`, 
                `color`, 
                `iconName`, 
                `isActive`, 
                `createdAt`, 
                `updatedAt`, 
                `syncStatus`
            FROM `savings_goals`
        """.trimIndent())
        
        // 删除旧表
        database.execSQL("DROP TABLE `savings_goals`")
        
        // 重命名新表
        database.execSQL("ALTER TABLE `savings_goals_new` RENAME TO `savings_goals`")
    }
}