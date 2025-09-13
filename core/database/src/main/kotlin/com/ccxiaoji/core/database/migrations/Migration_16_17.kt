package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v16 -> v17: 新增卡片管理表 cards
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cards` (
                `id` TEXT NOT NULL,
                `userId` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `cardType` TEXT NOT NULL,
                `maskedNumber` TEXT,
                `frontImagePath` TEXT,
                `backImagePath` TEXT,
                `expiryMonth` INTEGER,
                `expiryYear` INTEGER,
                `note` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_cards_userId_name ON cards(userId, name)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cards_userId_cardType ON cards(userId, cardType)")
    }
}

