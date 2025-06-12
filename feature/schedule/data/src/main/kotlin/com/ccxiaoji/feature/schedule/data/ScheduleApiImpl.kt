package com.ccxiaoji.feature.schedule.data

import com.ccxiaoji.feature.schedule.api.ScheduleApi
import com.ccxiaoji.feature.schedule.api.ScheduleInfo
import com.ccxiaoji.feature.schedule.api.ScheduleNavigator
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 排班API实现类
 * 提供排班模块对外的功能接口
 */
@Singleton
class ScheduleApiImpl @Inject constructor(
    private val repository: ScheduleRepository,
    private val navigator: ScheduleNavigator
) : ScheduleApi {
    
    override suspend fun getScheduleByDate(date: LocalDate): ScheduleInfo? {
        return repository.getScheduleByDate(date).first()?.let { schedule ->
            ScheduleInfo(
                date = schedule.date,
                shiftName = schedule.shift.name,
                startTime = schedule.shift.startTime.toString(),
                endTime = schedule.shift.endTime.toString(),
                color = schedule.shift.color.toString(),
                isRestDay = false
            )
        }
    }
    
    override suspend fun getTodaySchedule(): ScheduleInfo? {
        return getScheduleByDate(LocalDate.now())
    }
    
    override suspend fun getCurrentMonthWorkDays(): Int {
        val currentMonth = YearMonth.now()
        val statistics = repository.getMonthlyStatistics(currentMonth)
        return statistics.workDays
    }
    
    override suspend fun getCurrentMonthWorkHours(): Double {
        val currentMonth = YearMonth.now()
        val statistics = repository.getMonthlyStatistics(currentMonth)
        return statistics.totalHours
    }
    
    override fun navigateToScheduleHome() {
        navigator.navigateToScheduleHome()
    }
    
    override fun navigateToAddSchedule(date: LocalDate?) {
        if (date != null) {
            navigator.navigateToScheduleEdit(date)
        } else {
            navigator.navigateToScheduleEdit(LocalDate.now())
        }
    }
}