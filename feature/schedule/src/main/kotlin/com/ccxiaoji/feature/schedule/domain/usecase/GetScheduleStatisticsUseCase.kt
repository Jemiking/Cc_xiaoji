package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 获取排班统计信息用例
 */
class GetScheduleStatisticsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    init {
        android.util.Log.d("GetScheduleStatisticsUseCase", "UseCase initialized")
    }
    
    /**
     * 获取月度统计
     */
    suspend fun getMonthlyStatistics(yearMonth: YearMonth): ScheduleStatistics {
        android.util.Log.d("GetScheduleStatisticsUseCase", "Getting statistics for: $yearMonth")
        return repository.getMonthlyStatistics(yearMonth)
    }
    
    /**
     * 获取当前月统计
     */
    suspend fun getCurrentMonthStatistics(): ScheduleStatistics {
        return getMonthlyStatistics(YearMonth.now())
    }
    
    /**
     * 获取指定日期范围的统计
     */
    suspend fun getStatistics(startDate: LocalDate, endDate: LocalDate): ScheduleStatistics {
        return repository.getStatistics(startDate, endDate)
    }
    
    /**
     * 获取年度统计
     */
    suspend fun getYearlyStatistics(year: Int): ScheduleStatistics {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)
        return getStatistics(startDate, endDate)
    }
}