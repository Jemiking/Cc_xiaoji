package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 从版本4迁移到版本5
 * - 添加班次表 (shifts)
 * - 添加排班表 (schedules)
 * - 添加排班导出历史表 (schedule_export_history)
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 创建班次表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS shifts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT NOT NULL,
                color INTEGER NOT NULL,
                description TEXT,
                is_active INTEGER NOT NULL DEFAULT 1,
                sync_status TEXT NOT NULL DEFAULT 'LOCAL',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)
        
        // 创建排班表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date INTEGER NOT NULL,
                shift_id INTEGER NOT NULL,
                note TEXT,
                actual_start_time TEXT,
                actual_end_time TEXT,
                sync_status TEXT NOT NULL DEFAULT 'LOCAL',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                FOREIGN KEY(shift_id) REFERENCES shifts(id) ON DELETE CASCADE
            )
        """)
        
        // 创建排班导出历史表
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS schedule_export_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                file_name TEXT NOT NULL,
                file_path TEXT NOT NULL,
                format TEXT NOT NULL,
                start_date INTEGER NOT NULL,
                end_date INTEGER NOT NULL,
                export_time INTEGER NOT NULL,
                file_size INTEGER,
                record_count INTEGER,
                include_statistics INTEGER NOT NULL DEFAULT 0,
                include_actual_time INTEGER NOT NULL DEFAULT 0,
                is_deleted INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
        """)
        
        // 创建索引
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_schedules_date ON schedules(date)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_schedules_shift_id ON schedules(shift_id)")
    }
}