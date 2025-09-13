package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// v18 -> v19: 将 schedule 模块中的 sync_status 从 INTEGER 迁移为 TEXT（使用全局 SyncStatus 枚举）
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) 迁移 shifts 表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shifts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `start_time` TEXT NOT NULL,
                `end_time` TEXT NOT NULL,
                `color` INTEGER NOT NULL,
                `description` TEXT,
                `is_active` INTEGER NOT NULL,
                `sync_status` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // 将旧的 INTEGER 值映射为 TEXT：0->PENDING, 1->SYNCED, 2->PENDING_SYNC
        db.execSQL(
            """
            INSERT INTO shifts_new (
                id, name, start_time, end_time, color, description, is_active, sync_status, created_at, updated_at
            )
            SELECT 
                id, name, start_time, end_time, color, description, is_active,
                CASE sync_status 
                    WHEN 1 THEN 'SYNCED' 
                    WHEN 2 THEN 'PENDING_SYNC' 
                    ELSE 'PENDING' 
                END AS sync_status,
                created_at, updated_at
            FROM shifts
            """.trimIndent()
        )

        db.execSQL("DROP TABLE shifts")
        db.execSQL("ALTER TABLE shifts_new RENAME TO shifts")

        // 2) 迁移 schedules 表（包含外键与索引）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `schedules_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `date` INTEGER NOT NULL,
                `shift_id` INTEGER NOT NULL,
                `note` TEXT,
                `actual_start_time` TEXT,
                `actual_end_time` TEXT,
                `sync_status` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                FOREIGN KEY(`shift_id`) REFERENCES `shifts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO schedules_new (
                id, date, shift_id, note, actual_start_time, actual_end_time, sync_status, created_at, updated_at
            )
            SELECT 
                id, date, shift_id, note, actual_start_time, actual_end_time,
                CASE sync_status 
                    WHEN 1 THEN 'SYNCED' 
                    WHEN 2 THEN 'PENDING_SYNC' 
                    ELSE 'PENDING' 
                END AS sync_status,
                created_at, updated_at
            FROM schedules
            """.trimIndent()
        )

        db.execSQL("DROP TABLE schedules")
        db.execSQL("ALTER TABLE schedules_new RENAME TO schedules")

        // 重建索引（与 Room 期望保持一致）
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_schedules_date` ON `schedules` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_shift_id` ON `schedules` (`shift_id`)")
    }
}

