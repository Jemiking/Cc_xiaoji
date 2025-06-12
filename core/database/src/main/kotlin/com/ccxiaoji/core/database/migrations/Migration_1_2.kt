package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 1 to 2
 * Adds credit card specific fields to accounts table
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add credit card specific columns to accounts table
        db.execSQL("ALTER TABLE accounts ADD COLUMN creditLimitCents INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN billingDay INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN paymentDueDay INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN gracePeriodDays INTEGER DEFAULT NULL")
    }
}