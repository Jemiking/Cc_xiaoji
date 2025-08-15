package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库版本5到6的迁移
 * 添加计划管理模块的数据表
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 创建plan_table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS plan_table (
                id TEXT PRIMARY KEY NOT NULL,
                parent_id TEXT,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                start_date INTEGER NOT NULL,
                end_date INTEGER NOT NULL,
                status TEXT NOT NULL,
                progress REAL NOT NULL DEFAULT 0,
                color TEXT NOT NULL DEFAULT '#6650a4',
                priority INTEGER NOT NULL DEFAULT 0,
                tags TEXT NOT NULL DEFAULT '[]',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'LOCAL',
                is_template INTEGER NOT NULL DEFAULT 0,
                template_id TEXT,
                order_index INTEGER NOT NULL DEFAULT 0,
                reminder_settings TEXT,
                FOREIGN KEY(parent_id) REFERENCES plan_table(id) ON DELETE CASCADE
            )
        """)
        
        // 创建milestone_table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS milestone_table (
                id TEXT PRIMARY KEY NOT NULL,
                plan_id TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                target_date INTEGER NOT NULL,
                is_completed INTEGER NOT NULL DEFAULT 0,
                completed_date INTEGER,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'LOCAL',
                FOREIGN KEY(plan_id) REFERENCES plan_table(id) ON DELETE CASCADE
            )
        """)
        
        // 创建template_table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS template_table (
                id TEXT PRIMARY KEY NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                category TEXT NOT NULL,
                tags TEXT NOT NULL DEFAULT '[]',
                template_data TEXT NOT NULL,
                usage_count INTEGER NOT NULL DEFAULT 0,
                is_system INTEGER NOT NULL DEFAULT 0,
                is_public INTEGER NOT NULL DEFAULT 0,
                created_by TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                sync_status TEXT NOT NULL DEFAULT 'LOCAL'
            )
        """)
        
        // 创建索引
        db.execSQL("CREATE INDEX IF NOT EXISTS index_plan_table_parent_id ON plan_table(parent_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_plan_table_status ON plan_table(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_plan_table_sync_status ON plan_table(sync_status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_milestone_table_plan_id ON milestone_table(plan_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_milestone_table_is_completed ON milestone_table(is_completed)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_milestone_table_sync_status ON milestone_table(sync_status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_template_table_category ON template_table(category)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_template_table_is_system ON template_table(is_system)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_template_table_is_public ON template_table(is_public)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_template_table_sync_status ON template_table(sync_status)")
    }
}