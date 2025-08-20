package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 添加记账簿支持，允许用户创建多本记账簿来分类管理交易记录
        
        // 1. 创建ledgers表（记账簿表）
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `ledgers` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `color` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `isDefault` INTEGER NOT NULL,
                `displayOrder` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        
        // 2. 创建ledgers表的索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledgers_userId` ON `ledgers` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledgers_userId_isDefault` ON `ledgers` (`userId`, `isDefault`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_ledgers_userId_displayOrder` ON `ledgers` (`userId`, `displayOrder`)")
        
        // 3. 创建带有ledgerId字段的新transactions表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `transactions_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `accountId` TEXT NOT NULL,
                `amountCents` INTEGER NOT NULL,
                `categoryId` TEXT NOT NULL,
                `note` TEXT,
                `ledgerId` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `transactionDate` INTEGER,
                `locationLatitude` REAL,
                `locationLongitude` REAL,
                `locationAddress` TEXT,
                `locationPrecision` REAL,
                `locationProvider` TEXT,
                `isDeleted` INTEGER NOT NULL,
                `syncStatus` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """.trimIndent())
        
        // 4. 创建新transactions表的索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_userId` ON `transactions_new` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_accountId` ON `transactions_new` (`accountId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_categoryId` ON `transactions_new` (`categoryId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_ledgerId` ON `transactions_new` (`ledgerId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_createdAt` ON `transactions_new` (`createdAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_updatedAt` ON `transactions_new` (`updatedAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_transactionDate` ON `transactions_new` (`transactionDate`)")
        
        // 5. 为每个用户创建默认记账簿
        database.execSQL("""
            INSERT INTO `ledgers` (
                `id`, `userId`, `name`, `description`, `color`, `icon`, 
                `isDefault`, `displayOrder`, `isActive`, `createdAt`, `updatedAt`
            )
            SELECT 
                LOWER(HEX(RANDOMBLOB(16))) as `id`,
                `id` as `userId`,
                '总记账簿' as `name`,
                '默认记账簿，包含所有基本记账数据' as `description`,
                '#3A7AFE' as `color`,
                'book' as `icon`,
                1 as `isDefault`,
                0 as `displayOrder`,
                1 as `isActive`,
                strftime('%s', 'now') * 1000 as `createdAt`,
                strftime('%s', 'now') * 1000 as `updatedAt`
            FROM `users`
        """.trimIndent())
        
        // 6. 将现有交易记录关联到默认记账簿
        database.execSQL("""
            INSERT INTO `transactions_new` (
                `id`, `userId`, `accountId`, `amountCents`, `categoryId`, `note`, `ledgerId`,
                `createdAt`, `updatedAt`, `transactionDate`,
                `locationLatitude`, `locationLongitude`, `locationAddress`, 
                `locationPrecision`, `locationProvider`, `isDeleted`, `syncStatus`
            )
            SELECT 
                t.`id`, t.`userId`, t.`accountId`, t.`amountCents`, t.`categoryId`, t.`note`,
                l.`id` as `ledgerId`,
                t.`createdAt`, t.`updatedAt`, t.`transactionDate`,
                t.`locationLatitude`, t.`locationLongitude`, t.`locationAddress`,
                t.`locationPrecision`, t.`locationProvider`, t.`isDeleted`, t.`syncStatus`
            FROM `transactions` t
            JOIN `ledgers` l ON t.`userId` = l.`userId` AND l.`isDefault` = 1
        """.trimIndent())
        
        // 7. 删除旧的transactions表
        database.execSQL("DROP TABLE `transactions`")
        
        // 8. 重命名新的transactions表
        database.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
    }
}