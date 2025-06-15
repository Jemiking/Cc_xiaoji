package com.ccxiaoji.feature.schedule.domain.usecase

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository

/**
 * 获取月度排班用例
 */
class GetMonthScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 获取指定月份的排班列表
     * @param yearMonth 年月
     * @return 排班列表流
     */
    operator fun invoke(yearMonth: YearMonth): Flow<List<Schedule>> {
        return repository.getSchedulesByMonth(yearMonth)
    }
    
    /**
     * 获取当前月份的排班
     * @return 排班列表流
     */
    fun getCurrentMonth(): Flow<List<Schedule>> {
        return invoke(YearMonth.now())
    }
}