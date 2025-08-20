package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为transactions表添加时间和位置字段，支持自定义交易时间和地理位置标记
        
        // 1. 创建新表，包含所有新字段
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `transactions_new` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `accountId` TEXT NOT NULL,
                `amountCents` INTEGER NOT NULL,
                `categoryId` TEXT NOT NULL,
                `note` TEXT,
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
                PRIMARY KEY(`id`),
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
        """.trimIndent())
        
        // 2. 创建索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_userId` ON `transactions_new` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_accountId` ON `transactions_new` (`accountId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_categoryId` ON `transactions_new` (`categoryId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_createdAt` ON `transactions_new` (`createdAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_updatedAt` ON `transactions_new` (`updatedAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_new_transactionDate` ON `transactions_new` (`transactionDate`)")
        
        // 3. 从旧表复制数据，transactionDate默认使用createdAt的值
        database.execSQL("""
            INSERT INTO `transactions_new` (
                `id`, `userId`, `accountId`, `amountCents`, `categoryId`, 
                `note`, `createdAt`, `updatedAt`, `transactionDate`,
                `locationLatitude`, `locationLongitude`, `locationAddress`, 
                `locationPrecision`, `locationProvider`, `isDeleted`, `syncStatus`
            )
            SELECT 
                `id`, `userId`, `accountId`, `amountCents`, `categoryId`, 
                `note`, `createdAt`, `updatedAt`, `createdAt` as `transactionDate`,
                NULL as `locationLatitude`, NULL as `locationLongitude`, NULL as `locationAddress`,
                NULL as `locationPrecision`, NULL as `locationProvider`, `isDeleted`, `syncStatus`
            FROM `transactions`
        """.trimIndent())
        
        // 4. 删除旧表
        database.execSQL("DROP TABLE `transactions`")
        
        // 5. 重命名新表
        database.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
    }
}