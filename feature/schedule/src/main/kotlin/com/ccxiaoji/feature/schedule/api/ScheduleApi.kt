package com.ccxiaoji.feature.schedule.api

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * 排班模块对外暴露的核心接口
 * 
 * 该接口定义了排班模块的所有对外功能，供其他模块调用。
 * 在未来集成到 CC小记主项目时，其他模块通过此接口与排班模块交互。
 */
interface ScheduleApi {
    
    /**
     * 获取指定日期的排班信息
     * 
     * @param date 查询日期
     * @return 该日期的排班信息，如果没有排班则返回 null
     */
    suspend fun getScheduleByDate(date: LocalDate): ScheduleInfo?
    
    /**
     * 获取日期范围内的所有排班信息
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的排班信息列表
     */
    suspend fun getSchedulesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ScheduleInfo>
    
    /**
     * 监听排班变化
     * 
     * @return 排班变化的 Flow
     */
    fun observeScheduleChanges(): Flow<List<ScheduleChange>>
    
    /**
     * 获取所有班次信息
     * 
     * @return 所有可用的班次列表
     */
    suspend fun getAllShifts(): List<ShiftInfo>
    
    /**
     * 获取指定班次的统计信息
     * 
     * @param shiftId 班次ID
     * @param startDate 统计开始日期
     * @param endDate 统计结束日期
     * @return 班次统计信息
     */
    suspend fun getShiftStatistics(
        shiftId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): ShiftStatistics
    
    /**
     * 检查指定日期是否有排班
     * 
     * @param date 检查日期
     * @return 是否有排班
     */
    suspend fun hasScheduleOnDate(date: LocalDate): Boolean
    
    /**
     * 获取下一个排班日期
     * 
     * @param fromDate 从哪个日期开始查找
     * @return 下一个有排班的日期，如果没有则返回 null
     */
    suspend fun getNextScheduleDate(fromDate: LocalDate): LocalDate?
}

/**
 * 排班信息数据类
 * 
 * 对外暴露的排班信息，只包含必要的字段
 */
data class ScheduleInfo(
    val id: Long,
    val date: LocalDate,
    val shiftId: Long,
    val shiftName: String,
    val shiftColor: Int,
    val startTime: String,
    val endTime: String,
    val note: String? = null
)

/**
 * 班次信息数据类
 * 
 * 对外暴露的班次信息
 */
data class ShiftInfo(
    val id: Long,
    val name: String,
    val color: Int,
    val startTime: String,
    val endTime: String,
    val isActive: Boolean = true
)

/**
 * 排班变化数据类
 * 
 * 用于通知其他模块排班的变化
 */
data class ScheduleChange(
    val date: LocalDate,
    val changeType: ChangeType,
    val scheduleInfo: ScheduleInfo?
)

/**
 * 变化类型枚举
 */
enum class ChangeType {
    ADDED,      // 新增排班
    UPDATED,    // 更新排班
    DELETED     // 删除排班
}

/**
 * 班次统计信息
 * 
 * 用于展示班次的使用统计
 */
data class ShiftStatistics(
    val shiftId: Long,
    val shiftName: String,
    val totalDays: Int,
    val totalHours: Double,
    val dateRange: Pair<LocalDate, LocalDate>
)