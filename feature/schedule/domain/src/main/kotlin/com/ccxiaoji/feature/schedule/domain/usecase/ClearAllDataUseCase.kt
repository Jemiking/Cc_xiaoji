package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * 清除所有数据用例
 */
class ClearAllDataUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    /**
     * 执行清除所有数据
     * @return 清除结果
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 获取所有班次并逐个删除
            val shifts = scheduleRepository.getAllShifts().first()
            shifts.forEach { shift ->
                scheduleRepository.deleteShift(shift.id)
            }
            
            // 清除所有排班（使用很大的日期范围）
            scheduleRepository.clearSchedules(
                startDate = LocalDate.of(2000, 1, 1),
                endDate = LocalDate.of(2100, 12, 31)
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取数据统计
     * @return 数据统计信息
     */
    suspend fun getDataStatistics(): DataStatistics {
        return try {
            val shiftCount = scheduleRepository.getAllShifts().first().size
            // 获取当前年份的排班数量作为统计
            val currentYear = LocalDate.now().year
            val yearStatistics = scheduleRepository.getStatistics(
                startDate = LocalDate.of(currentYear, 1, 1),
                endDate = LocalDate.of(currentYear, 12, 31)
            )
            DataStatistics(shiftCount, yearStatistics.totalDays)
        } catch (e: Exception) {
            DataStatistics(0, 0)
        }
    }
    
    /**
     * 数据统计信息
     */
    data class DataStatistics(
        val shiftCount: Int,
        val scheduleCount: Int
    )
}