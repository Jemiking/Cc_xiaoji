package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

/**
 * 获取月度排班用例
 */
class GetMonthScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    init {
        android.util.Log.d("GetMonthScheduleUseCase", "UseCase initialized")
    }
    
    /**
     * 获取指定月份的排班列表
     */
    operator fun invoke(yearMonth: YearMonth): Flow<List<Schedule>> {
        android.util.Log.d("GetMonthScheduleUseCase", "Getting schedules for: $yearMonth")
        return repository.getSchedulesByMonth(yearMonth)
    }
    
    /**
     * 获取当前月份的排班
     */
    fun getCurrentMonth(): Flow<List<Schedule>> {
        return invoke(YearMonth.now())
    }
}