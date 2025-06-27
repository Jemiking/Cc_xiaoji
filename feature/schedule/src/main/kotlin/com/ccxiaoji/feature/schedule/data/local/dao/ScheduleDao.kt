package com.ccxiaoji.feature.schedule.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.schedule.data.local.entity.ScheduleEntity
import com.ccxiaoji.feature.schedule.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

/**
 * 排班数据访问对象
 * 提供排班相关的数据库操作方法
 */
@Dao
interface ScheduleDao {
    
    /**
     * 获取指定日期范围内的排班
     */
    @Query("""
        SELECT * FROM schedules 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date ASC
    """)
    fun getSchedulesByDateRange(startDate: Long, endDate: Long): Flow<List<ScheduleEntity>>
    
    /**
     * 获取指定日期的排班
     */
    @Query("SELECT * FROM schedules WHERE date = :date")
    suspend fun getScheduleByDate(date: Long): ScheduleEntity?
    
    /**
     * 获取指定日期的排班（Flow版本）
     */
    @Query("SELECT * FROM schedules WHERE date = :date")
    fun getScheduleByDateFlow(date: Long): Flow<ScheduleEntity?>
    
    /**
     * 获取指定月份的排班（包含班次信息）
     */
    @Transaction
    @Query("""
        SELECT * FROM schedules
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date ASC
    """)
    fun getSchedulesWithShiftByMonth(startDate: Long, endDate: Long): Flow<List<ScheduleWithShift>>
    
    /**
     * 插入或更新排班
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSchedule(schedule: ScheduleEntity): Long
    
    /**
     * 批量插入排班
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)
    
    /**
     * 更新排班
     */
    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)
    
    /**
     * 删除排班
     */
    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)
    
    /**
     * 根据ID删除排班
     */
    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    suspend fun deleteSchedule(scheduleId: Long)
    
    /**
     * 删除指定日期的排班
     */
    @Query("DELETE FROM schedules WHERE date = :date")
    suspend fun deleteScheduleByDate(date: Long)
    
    /**
     * 获取工时统计
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT date) as totalDays,
            COUNT(DISTINCT CASE WHEN shift_id = :shiftId THEN date END) as shiftDays
        FROM schedules
        WHERE date >= :startDate AND date <= :endDate
    """)
    suspend fun getWorkStatistics(startDate: Long, endDate: Long, shiftId: Long): WorkStatistics
    
    /**
     * 清除指定日期范围的排班
     */
    @Query("DELETE FROM schedules WHERE date >= :startDate AND date <= :endDate")
    suspend fun clearSchedulesByDateRange(startDate: Long, endDate: Long)
    
    /**
     * 获取所有排班
     */
    @Query("SELECT * FROM schedules ORDER BY date ASC")
    suspend fun getAllSchedules(): List<ScheduleEntity>
    
    /**
     * 删除所有排班
     */
    @Query("DELETE FROM schedules")
    suspend fun deleteAllSchedules(): Int
}

/**
 * 排班与班次关联数据
 */
data class ScheduleWithShift(
    @Embedded val schedule: ScheduleEntity,
    @Relation(
        parentColumn = "shift_id",
        entityColumn = "id"
    )
    val shift: ShiftEntity
)

/**
 * 工时统计数据
 */
data class WorkStatistics(
    val totalDays: Int,
    val shiftDays: Int
)