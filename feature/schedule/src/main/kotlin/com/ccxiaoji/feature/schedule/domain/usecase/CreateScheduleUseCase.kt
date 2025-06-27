package com.ccxiaoji.feature.schedule.domain.usecase

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.SchedulePattern
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * 创建排班用例
 * 处理单个或批量排班的创建
 */
class CreateScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    
    /**
     * 创建或更新单个排班
     */
    suspend fun createOrUpdateSchedule(schedule: Schedule): Long {
        return repository.saveSchedule(schedule)
    }
    
    /**
     * 删除指定日期的排班
     */
    suspend fun deleteSchedule(date: LocalDate) {
        repository.deleteScheduleByDate(date)
    }
    
    /**
     * 根据模式批量创建排班
     */
    suspend fun createSchedulesByPattern(pattern: SchedulePattern) {
        when (pattern) {
            is SchedulePattern.Single -> {
                handleSinglePattern(pattern)
            }
            is SchedulePattern.Cycle -> {
                handleCyclePattern(pattern)
            }
            is SchedulePattern.Weekly -> {
                // 将旧的 Weekly 转换为 Cycle 处理
                handleCyclePattern(pattern.toCycle())
            }
            is SchedulePattern.Rotation -> {
                handleRotationPattern(pattern)
            }
            is SchedulePattern.Custom -> {
                handleCustomPattern(pattern)
            }
        }
    }
    
    /**
     * 清除日期范围内的排班
     */
    suspend fun clearSchedules(startDate: LocalDate, endDate: LocalDate) {
        repository.clearSchedules(startDate, endDate)
    }
    
    private suspend fun handleSinglePattern(pattern: SchedulePattern.Single) {
        val shift = repository.getShiftById(pattern.shiftId)
            ?: throw IllegalArgumentException("班次不存在")
        
        val schedule = Schedule(
            date = pattern.date,
            shift = shift
        )
        repository.saveSchedule(schedule)
    }
    
    /**
     * 处理循环排班模式（支持任意天数周期）
     */
    private suspend fun handleCyclePattern(pattern: SchedulePattern.Cycle) {
        if (pattern.cycleDays < 2 || pattern.cycleDays > 365) {
            throw IllegalArgumentException("循环天数必须在2-365之间")
        }
        
        val schedules = mutableListOf<Schedule>()
        var currentDate = pattern.startDate
        var dayInCycle = 0
        
        while (!currentDate.isAfter(pattern.endDate)) {
            val shiftId = pattern.cyclePattern[dayInCycle]
            if (shiftId != null) {
                val shift = repository.getShiftById(shiftId)
                if (shift != null) {
                    schedules.add(Schedule(date = currentDate, shift = shift))
                }
            }
            
            currentDate = currentDate.plusDays(1)
            dayInCycle = (dayInCycle + 1) % pattern.cycleDays
        }
        
        repository.saveSchedules(schedules)
    }
    
    private suspend fun handleRotationPattern(pattern: SchedulePattern.Rotation) {
        if (pattern.shiftIds.isEmpty()) return
        
        val shifts = pattern.shiftIds.mapNotNull { repository.getShiftById(it) }
        if (shifts.isEmpty()) return
        
        val schedules = mutableListOf<Schedule>()
        var currentDate = pattern.startDate
        var shiftIndex = 0
        var restDayCount = 0
        
        while (!currentDate.isAfter(pattern.endDate)) {
            if (restDayCount > 0) {
                // 休息日
                restDayCount--
            } else {
                // 工作日
                schedules.add(Schedule(date = currentDate, shift = shifts[shiftIndex]))
                shiftIndex = (shiftIndex + 1) % shifts.size
                
                // 完成一个循环后的休息日
                if (shiftIndex == 0 && pattern.restDays > 0) {
                    restDayCount = pattern.restDays
                }
            }
            currentDate = currentDate.plusDays(1)
        }
        
        repository.saveSchedules(schedules)
    }
    
    private suspend fun handleCustomPattern(pattern: SchedulePattern.Custom) {
        val schedules = mutableListOf<Schedule>()
        var currentDate = pattern.startDate
        var patternIndex = 0
        
        while (!currentDate.isAfter(pattern.endDate) && patternIndex < pattern.pattern.size) {
            val shiftId = pattern.pattern[patternIndex]
            if (shiftId != null) {
                val shift = repository.getShiftById(shiftId)
                if (shift != null) {
                    schedules.add(Schedule(date = currentDate, shift = shift))
                }
            }
            currentDate = currentDate.plusDays(1)
            patternIndex++
        }
        
        repository.saveSchedules(schedules)
    }
}