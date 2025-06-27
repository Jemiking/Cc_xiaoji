package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * 根据日期获取排班用例
 */
class GetScheduleByDateUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 获取指定日期的排班信息
     */
    operator fun invoke(date: LocalDate): Flow<Schedule?> {
        return repository.getScheduleByDate(date)
    }
}