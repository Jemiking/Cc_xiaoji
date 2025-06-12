package com.ccxiaoji.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 排班导出历史实体类
 * 记录用户的排班数据导出操作历史
 */
@Entity(tableName = "schedule_export_history")
data class ScheduleExportHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "file_name")
    val fileName: String,
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "format")
    val format: String, // CSV, JSON, REPORT
    
    @ColumnInfo(name = "start_date")
    val startDate: Long, // 导出数据的开始日期
    
    @ColumnInfo(name = "end_date")
    val endDate: Long, // 导出数据的结束日期
    
    @ColumnInfo(name = "export_time")
    val exportTime: Long, // 导出时间
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long? = null, // 文件大小（字节）
    
    @ColumnInfo(name = "record_count")
    val recordCount: Int? = null, // 导出的记录数
    
    @ColumnInfo(name = "include_statistics")
    val includeStatistics: Boolean = false, // 是否包含统计信息
    
    @ColumnInfo(name = "include_actual_time")
    val includeActualTime: Boolean = false, // 是否包含实际工作时间
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false, // 软删除标记
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)