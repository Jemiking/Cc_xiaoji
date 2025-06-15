package com.ccxiaoji.feature.schedule.data.local.dao

import com.ccxiaoji.core.database.dao.ScheduleDao
import com.ccxiaoji.core.database.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * ScheduleDao扩展函数，提供排班模块特定的查询方法
 */

/**
 * 获取指定日期范围内的排班（使用LocalDate）
 */
suspend fun ScheduleDao.getSchedulesBetweenDates(
    startDate: LocalDate,
    endDate: LocalDate
): List<ScheduleEntity> {
    return getSchedulesByDateRange(
        startDate.toEpochDay() * 86400000,
        endDate.toEpochDay() * 86400000
    ).first()
}

/**
 * 获取指定月份的排班统计信息
 */
suspend fun ScheduleDao.getMonthStatistics(
    year: Int,
    month: Int
): MonthStatistics {
    val startDate = LocalDate.of(year, month, 1)
    val endDate = startDate.plusMonths(1).minusDays(1)
    
    val schedules = getSchedulesBetweenDates(startDate, endDate)
    
    return MonthStatistics(
        totalDays = schedules.size,
        workDays = schedules.count { it.shiftId > 0 },
        restDays = schedules.count { it.shiftId == 0L }
    )
}

/**
 * 月度统计数据
 */
data class MonthStatistics(
    val totalDays: Int,
    val workDays: Int,
    val restDays: Int
)