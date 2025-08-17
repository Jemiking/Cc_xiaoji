package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为categories表添加level、path、isDefault、isActive字段，用于支持二级分类
        
        // 1. 创建新表，包含所有新字段
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `categories_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `color` TEXT NOT NULL,
                `parentId` TEXT,
                `level` INTEGER NOT NULL DEFAULT 1,
                `path` TEXT NOT NULL DEFAULT '',
                `displayOrder` INTEGER NOT NULL,
                `isDefault` INTEGER NOT NULL DEFAULT 0,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `isSystem` INTEGER NOT NULL,
                `usageCount` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `syncStatus` TEXT NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`parentId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 2. 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_new_userId` ON `categories_new` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_new_parentId` ON `categories_new` (`parentId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_new_type` ON `categories_new` (`type`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_new_displayOrder` ON `categories_new` (`displayOrder`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_categories_new_level` ON `categories_new` (`level`)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_new_userId_name_parentId` ON `categories_new` (`userId`, `name`, `parentId`)")
        
        // 3. 从旧表复制数据
        database.execSQL("""
            INSERT INTO `categories_new` (
                `id`, `userId`, `name`, `type`, `icon`, `color`, 
                `parentId`, `level`, `path`, `displayOrder`, 
                `isDefault`, `isActive`, `isSystem`, `usageCount`, 
                `createdAt`, `updatedAt`, `isDeleted`, `syncStatus`
            )
            SELECT 
                `id`, 
                `userId`, 
                `name`, 
                `type`, 
                `icon`, 
                `color`, 
                `parentId`,
                CASE 
                    WHEN `parentId` IS NULL THEN 1
                    ELSE 2
                END as `level`,
                CASE 
                    WHEN `parentId` IS NULL THEN `name`
                    ELSE ''
                END as `path`,
                `displayOrder`,
                0 as `isDefault`,
                1 as `isActive`,
                `isSystem`, 
                `usageCount`, 
                `createdAt`, 
                `updatedAt`, 
                `isDeleted`, 
                `syncStatus`
            FROM `categories`
        """.trimIndent())
        
        // 4. 更新二级分类的path字段（需要连接父分类获取完整路径）
        database.execSQL("""
            UPDATE `categories_new`
            SET `path` = (
                SELECT p.name || '/' || c.name
                FROM `categories_new` c
                JOIN `categories_new` p ON c.parentId = p.id
                WHERE c.id = `categories_new`.id
            )
            WHERE `parentId` IS NOT NULL
        """.trimIndent())
        
        // 5. 删除旧表
        database.execSQL("DROP TABLE `categories`")
        
        // 6. 重命名新表
        database.execSQL("ALTER TABLE `categories_new` RENAME TO `categories`")
        
        // 7. 为现有分类创建默认的"一般"子分类（用于兼容旧数据）
        database.execSQL("""
            INSERT INTO `categories` (
                `id`, `userId`, `name`, `type`, `icon`, `color`, 
                `parentId`, `level`, `path`, `displayOrder`, 
                `isDefault`, `isActive`, `isSystem`, `usageCount`, 
                `createdAt`, `updatedAt`, `isDeleted`, `syncStatus`
            )
            SELECT 
                'general_' || c.id as `id`,
                c.userId,
                '一般' as `name`,
                c.type,
                c.icon,
                c.color,
                c.id as `parentId`,
                2 as `level`,
                c.name || '/一般' as `path`,
                0 as `displayOrder`,
                1 as `isDefault`,
                1 as `isActive`,
                0 as `isSystem`,
                0 as `usageCount`,
                c.createdAt,
                c.updatedAt,
                0 as `isDeleted`,
                c.syncStatus
            FROM `categories` c
            WHERE c.level = 1 AND c.isDeleted = 0
        """.trimIndent())
        
        // 8. 更新所有现有交易记录，将原来的一级分类ID改为对应的"一般"子分类ID
        database.execSQL("""
            UPDATE `transactions`
            SET `categoryId` = 'general_' || `categoryId`
            WHERE `categoryId` IN (
                SELECT `id` FROM `categories` WHERE `level` = 1
            )
        """.trimIndent())
        
        // 9. 更新预算表中的分类引用
        database.execSQL("""
            UPDATE `budgets`
            SET `categoryId` = 'general_' || `categoryId`
            WHERE `categoryId` IN (
                SELECT `id` FROM `categories` WHERE `level` = 1
            )
        """.trimIndent())
        
        // 10. 更新定期交易表中的分类引用
        database.execSQL("""
            UPDATE `recurring_transactions`
            SET `categoryId` = 'general_' || `categoryId`
            WHERE `categoryId` IN (
                SELECT `id` FROM `categories` WHERE `level` = 1
            )
        """.trimIndent())
    }
}