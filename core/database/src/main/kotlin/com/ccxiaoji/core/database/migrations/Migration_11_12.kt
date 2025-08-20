package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 修复transactions表的外键约束和索引名称问题
        // 这是为了解决Migration_10_11中遗留的外键约束缺失和索引名称不匹配问题
        
        // 1. 备份现有的transactions数据到临时表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `transactions_backup` (
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
        
        // 2. 复制数据到备份表
        database.execSQL("""
            INSERT INTO `transactions_backup` 
            SELECT * FROM `transactions`
        """.trimIndent())
        
        // 3. 删除现有的transactions表
        database.execSQL("DROP TABLE `transactions`")
        
        // 4. 重新创建带有正确外键约束和索引的transactions表
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `transactions` (
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
                PRIMARY KEY(`id`),
                FOREIGN KEY(`userId`) REFERENCES `users`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON DELETE RESTRICT,
                FOREIGN KEY(`ledgerId`) REFERENCES `ledgers`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        
        // 5. 创建正确的索引（使用期望的名称）
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_userId` ON `transactions` (`userId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_accountId` ON `transactions` (`accountId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_ledgerId` ON `transactions` (`ledgerId`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_createdAt` ON `transactions` (`createdAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_updatedAt` ON `transactions` (`updatedAt`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_transactionDate` ON `transactions` (`transactionDate`)")
        
        // 6. 从备份表恢复数据
        database.execSQL("""
            INSERT INTO `transactions` 
            SELECT * FROM `transactions_backup`
        """.trimIndent())
        
        // 7. 删除备份表
        database.execSQL("DROP TABLE `transactions_backup`")
    }
}