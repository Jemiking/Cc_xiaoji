package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import javax.inject.Inject

/**
 * 更新排班用例
 */
class UpdateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 更新排班信息
     */
    suspend operator fun invoke(schedule: Schedule) {
        repository.updateSchedule(schedule)
    }
}