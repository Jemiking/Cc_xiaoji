package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import javax.inject.Inject

/**
 * 删除排班用例
 */
class DeleteScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 删除排班
     */
    suspend operator fun invoke(scheduleId: Long) {
        repository.deleteSchedule(scheduleId)
    }
}