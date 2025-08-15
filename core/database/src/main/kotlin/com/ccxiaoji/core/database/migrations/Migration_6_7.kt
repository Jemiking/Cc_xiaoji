package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库版本6到7的迁移
 * 为账户表添加信用卡相关字段
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 为accounts表添加信用卡相关字段
        db.execSQL("ALTER TABLE accounts ADD COLUMN annualFeeAmountCents INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN annualFeeWaiverThresholdCents INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN cashAdvanceLimitCents INTEGER DEFAULT NULL")
        db.execSQL("ALTER TABLE accounts ADD COLUMN interestRate REAL DEFAULT NULL")
    }
}