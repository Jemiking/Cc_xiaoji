package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取班次列表用例
 */
class GetShiftsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * 获取所有活跃班次
     */
    operator fun invoke(): Flow<List<Shift>> {
        return repository.getAllShifts()
    }
    
    /**
     * 根据ID获取单个班次
     */
    suspend fun getShiftById(shiftId: Long): Shift? {
        return repository.getShiftById(shiftId)
    }
}