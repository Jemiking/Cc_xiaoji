package com.ccxiaoji.core.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库从版本5迁移到版本6
 * 主要变更：为排班模块添加性能优化索引
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 为ShiftEntity添加索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_is_active` ON `shifts` (`is_active`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_is_active_start_time` ON `shifts` (`is_active`, `start_time`)")
        
        // 为ScheduleEntity添加复合索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_date_shift_id` ON `schedules` (`date`, `shift_id`)")
        
        // 为ScheduleExportHistoryEntity添加索引
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_export_history_export_time` ON `schedule_export_history` (`export_time`)")
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_export_history_is_deleted_export_time` ON `schedule_export_history` (`is_deleted`, `export_time`)")
    }
}