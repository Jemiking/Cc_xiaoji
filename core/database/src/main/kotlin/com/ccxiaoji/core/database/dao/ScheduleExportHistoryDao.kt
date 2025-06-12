package com.ccxiaoji.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ccxiaoji.core.database.entity.ScheduleExportHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 排班导出历史数据访问对象
 */
@Dao
interface ScheduleExportHistoryDao {
    
    /**
     * 插入导出历史记录
     */
    @Insert
    suspend fun insertExportHistory(history: ScheduleExportHistoryEntity): Long
    
    /**
     * 获取所有未删除的导出历史（按时间倒序）
     */
    @Query("SELECT * FROM schedule_export_history WHERE is_deleted = 0 ORDER BY export_time DESC")
    fun getAllExportHistory(): Flow<List<ScheduleExportHistoryEntity>>
    
    /**
     * 获取最近的导出历史（限制数量）
     */
    @Query("SELECT * FROM schedule_export_history WHERE is_deleted = 0 ORDER BY export_time DESC LIMIT :limit")
    fun getRecentExportHistory(limit: Int = 20): Flow<List<ScheduleExportHistoryEntity>>
    
    /**
     * 根据ID获取导出历史
     */
    @Query("SELECT * FROM schedule_export_history WHERE id = :id")
    suspend fun getExportHistoryById(id: Long): ScheduleExportHistoryEntity?
    
    /**
     * 软删除导出历史
     */
    @Query("UPDATE schedule_export_history SET is_deleted = 1 WHERE id = :id")
    suspend fun deleteExportHistory(id: Long)
    
    /**
     * 批量软删除导出历史
     */
    @Query("UPDATE schedule_export_history SET is_deleted = 1 WHERE id IN (:ids)")
    suspend fun deleteExportHistories(ids: List<Long>)
    
    /**
     * 清理旧的导出历史（删除指定天数之前的记录）
     */
    @Query("DELETE FROM schedule_export_history WHERE export_time < :timestamp")
    suspend fun cleanOldHistory(timestamp: Long)
    
    /**
     * 获取导出历史总数
     */
    @Query("SELECT COUNT(*) FROM schedule_export_history WHERE is_deleted = 0")
    suspend fun getExportHistoryCount(): Int
    
    /**
     * 按格式类型统计导出次数
     */
    @Query("SELECT format, COUNT(*) as count FROM schedule_export_history WHERE is_deleted = 0 GROUP BY format")
    suspend fun getExportStatisticsByFormat(): List<FormatStatistics>
    
    /**
     * 更新文件大小
     */
    @Query("UPDATE schedule_export_history SET file_size = :fileSize WHERE id = :id")
    suspend fun updateFileSize(id: Long, fileSize: Long)
    
    /**
     * 获取所有导出历史（非Flow版本）
     */
    @Query("SELECT * FROM schedule_export_history ORDER BY export_time DESC")
    suspend fun getAllHistory(): List<ScheduleExportHistoryEntity>
    
    /**
     * 删除所有导出历史
     */
    @Query("DELETE FROM schedule_export_history")
    suspend fun deleteAllHistory(): Int
}

/**
 * 格式统计数据
 */
data class FormatStatistics(
    val format: String,
    val count: Int
)