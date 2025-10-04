package com.ccxiaoji.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    /**
     * 19 -> 20
     * - 补齐历史分类数据的 level/path/isActive 等字段
     * - 将唯一索引从 (userId, name, parentId) 调整为 (userId, type, name, parentId)
     */
    val MIGRATION_19_20: Migration = object : Migration(19, 20) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1) 补齐可能缺失或异常的字段取值
            // isActive：系统分类统一启用
            db.execSQL("UPDATE categories SET isActive = 1 WHERE isSystem = 1 AND isActive != 1")

            // level：父为空视为一级，父非空视为二级（仅修正异常值）
            db.execSQL("UPDATE categories SET level = 1 WHERE parentId IS NULL AND (level IS NULL OR level NOT IN (1,2))")
            db.execSQL("UPDATE categories SET level = 2 WHERE parentId IS NOT NULL AND (level IS NULL OR level NOT IN (1,2))")

            // path：一级用 name；二级用 父/子
            db.execSQL("UPDATE categories SET path = name WHERE (path IS NULL OR path = '') AND level = 1")
            db.execSQL(
                "UPDATE categories SET path = (SELECT p.name || '/' || categories.name FROM categories p WHERE p.id = categories.parentId) " +
                    "WHERE (path IS NULL OR path = '') AND level = 2 AND parentId IS NOT NULL"
            )

            // 2) 调整唯一索引：删除旧索引，创建新索引（包含 type）
            db.execSQL("DROP INDEX IF EXISTS index_categories_userId_name_parentId")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_categories_userId_type_name_parentId ON categories(userId, type, name, parentId)"
            )
        }
    }

    /**
     * 20 -> 21
     * - categories 表新增 isHidden 列（默认0）
     * - 将名称包含"转账"的分类标记为系统+隐藏
     */
    val MIGRATION_20_21: Migration = object : Migration(20, 21) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE categories ADD COLUMN isHidden INTEGER NOT NULL DEFAULT 0")
            // 将名称包含"转账"的父/子分类默认隐藏并标记系统
            db.execSQL("UPDATE categories SET isHidden = 1, isSystem = 1, updatedAt = strftime('%s','now')*1000 WHERE name LIKE '%转账%'")
        }
    }

    /**
     * 21 -> 22
     * - tasks 表新增提醒相关字段：reminderEnabled, reminderAt, reminderMinutesBefore
     * - habits 表新增提醒相关字段：reminderEnabled, reminderTime
     * - 支持单条提醒配置，旧数据自动继承全局配置（字段默认为null）
     */
    val MIGRATION_21_22: Migration = object : Migration(21, 22) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // ===== 迁移 tasks 表 =====
            db.execSQL("ALTER TABLE tasks ADD COLUMN reminderEnabled INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE tasks ADD COLUMN reminderAt INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE tasks ADD COLUMN reminderMinutesBefore INTEGER DEFAULT NULL")

            // ===== 迁移 habits 表 =====
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderEnabled INTEGER DEFAULT NULL")
            db.execSQL("ALTER TABLE habits ADD COLUMN reminderTime TEXT DEFAULT NULL")
        }
    }

    /**
     * 22 -> 23
     * - tasks 表新增固定时间提醒字段：reminderTime
     * - 支持"每天HH:mm提醒"模式（Phase 3）
     */
    val MIGRATION_22_23: Migration = object : Migration(22, 23) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 添加固定时间提醒字段（HH:mm格式字符串）
            db.execSQL("ALTER TABLE tasks ADD COLUMN reminderTime TEXT DEFAULT NULL")
        }
    }
}
