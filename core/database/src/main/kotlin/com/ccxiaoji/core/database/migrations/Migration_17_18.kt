package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v17 -> v18: cards 表增加 holderName / institutionName / institutionType 三个字段
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cards ADD COLUMN holderName TEXT")
        db.execSQL("ALTER TABLE cards ADD COLUMN institutionName TEXT")
        db.execSQL("ALTER TABLE cards ADD COLUMN institutionType TEXT")
    }
}

