package com.ccxiaoji.feature.schedule.domain.usecase

import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository

/**
 * 获取排班统计信息用例
 */
class GetScheduleStatisticsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 获取月度统计
     * @param yearMonth 年月
     * @return 排班统计信息
     */
    suspend fun getMonthlyStatistics(yearMonth: YearMonth): ScheduleStatistics {
        return repository.getMonthlyStatistics(yearMonth)
    }
    
    /**
     * 获取当前月统计
     * @return 排班统计信息
     */
    suspend fun getCurrentMonthStatistics(): ScheduleStatistics {
        return getMonthlyStatistics(YearMonth.now())
    }
    
    /**
     * 获取指定日期范围的统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排班统计信息
     */
    suspend fun getStatistics(startDate: LocalDate, endDate: LocalDate): ScheduleStatistics {
        return repository.getStatistics(startDate, endDate)
    }
    
    /**
     * 获取年度统计
     * @param year 年份
     * @return 排班统计信息
     */
    suspend fun getYearlyStatistics(year: Int): ScheduleStatistics {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)
        return getStatistics(startDate, endDate)
    }
}