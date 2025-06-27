package com.ccxiaoji.feature.schedule.domain.repository

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.SchedulePattern
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

/**
 * 排班仓库接口
 * 定义数据访问的抽象接口
 */
interface ScheduleRepository {
    
    // ========== 班次相关 ==========
    
    /**
     * 获取所有活跃班次
     */
    fun getAllShifts(): Flow<List<Shift>>
    
    /**
     * 获取所有激活的班次
     */
    fun getActiveShifts(): Flow<List<Shift>>
    
    /**
     * 根据ID获取班次
     */
    suspend fun getShiftById(shiftId: Long): Shift?
    
    /**
     * 创建新班次
     */
    suspend fun createShift(shift: Shift): Long
    
    /**
     * 更新班次
     */
    suspend fun updateShift(shift: Shift)
    
    /**
     * 删除班次
     */
    suspend fun deleteShift(shiftId: Long)
    
    /**
     * 检查班次名称是否已存在
     */
    suspend fun isShiftNameExists(name: String, excludeId: Long = 0): Boolean
    
    // ========== 排班相关 ==========
    
    /**
     * 获取指定月份的排班
     */
    fun getSchedulesByMonth(yearMonth: YearMonth): Flow<List<Schedule>>
    
    /**
     * 获取指定日期的排班
     */
    fun getScheduleByDate(date: LocalDate): Flow<Schedule?>
    
    /**
     * 创建或更新排班
     */
    suspend fun saveSchedule(schedule: Schedule): Long
    
    /**
     * 更新排班
     */
    suspend fun updateSchedule(schedule: Schedule)
    
    /**
     * 删除排班
     */
    suspend fun deleteSchedule(scheduleId: Long)
    
    /**
     * 批量创建排班
     */
    suspend fun saveSchedules(schedules: List<Schedule>)
    
    /**
     * 根据模式创建排班
     */
    suspend fun createSchedulesByPattern(pattern: SchedulePattern)
    
    /**
     * 删除指定日期的排班
     */
    suspend fun deleteScheduleByDate(date: LocalDate)
    
    /**
     * 清除日期范围内的排班
     */
    suspend fun clearSchedules(startDate: LocalDate, endDate: LocalDate)
    
    // ========== 统计相关 ==========
    
    /**
     * 获取月度统计信息
     */
    suspend fun getMonthlyStatistics(yearMonth: YearMonth): ScheduleStatistics
    
    /**
     * 获取日期范围内的统计信息
     */
    suspend fun getStatistics(startDate: LocalDate, endDate: LocalDate): ScheduleStatistics
}