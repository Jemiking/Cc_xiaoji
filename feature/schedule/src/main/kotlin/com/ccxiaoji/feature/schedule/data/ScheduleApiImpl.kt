package com.ccxiaoji.feature.schedule.data

import com.ccxiaoji.feature.schedule.api.*
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ScheduleApi的实现类
 */
@Singleton
class ScheduleApiImpl @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ScheduleApi {
    
    override suspend fun getScheduleByDate(date: LocalDate): ScheduleInfo? {
        val schedule = scheduleRepository.getScheduleByDate(date).first() ?: return null
        
        return ScheduleInfo(
            id = schedule.id,
            date = schedule.date,
            shiftId = schedule.shift.id,
            shiftName = schedule.shift.name,
            shiftColor = schedule.shift.color,
            startTime = schedule.shift.startTime.toString(),
            endTime = schedule.shift.endTime.toString(),
            note = schedule.note
        )
    }
    
    override suspend fun getSchedulesByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<ScheduleInfo> {
        // Use monthly queries to cover the date range
        val schedules = mutableListOf<ScheduleInfo>()
        var current = startDate
        
        while (!current.isAfter(endDate)) {
            val yearMonth = YearMonth.from(current)
            val monthSchedules = scheduleRepository.getSchedulesByMonth(yearMonth).first()
            
            monthSchedules.forEach { schedule ->
                if (!schedule.date.isBefore(startDate) && !schedule.date.isAfter(endDate)) {
                    schedules.add(
                        ScheduleInfo(
                            id = schedule.id,
                            date = schedule.date,
                            shiftId = schedule.shift.id,
                            shiftName = schedule.shift.name,
                            shiftColor = schedule.shift.color,
                            startTime = schedule.shift.startTime.toString(),
                            endTime = schedule.shift.endTime.toString(),
                            note = schedule.note
                        )
                    )
                }
            }
            
            current = current.plusMonths(1).withDayOfMonth(1)
        }
        
        return schedules
    }
    
    override fun observeScheduleChanges(): Flow<List<ScheduleChange>> {
        // Observe changes from the current month
        val currentMonth = YearMonth.now()
        return scheduleRepository.getSchedulesByMonth(currentMonth).map { schedules ->
            schedules.map { schedule ->
                ScheduleChange(
                    date = schedule.date,
                    changeType = ChangeType.UPDATED,
                    scheduleInfo = ScheduleInfo(
                        id = schedule.id,
                        date = schedule.date,
                        shiftId = schedule.shift.id,
                        shiftName = schedule.shift.name,
                        shiftColor = schedule.shift.color,
                        startTime = schedule.shift.startTime.toString(),
                        endTime = schedule.shift.endTime.toString(),
                        note = schedule.note
                    )
                )
            }
        }
    }
    
    override suspend fun getAllShifts(): List<ShiftInfo> {
        val shifts = scheduleRepository.getAllShifts().first()
        return shifts.map { shift ->
            ShiftInfo(
                id = shift.id,
                name = shift.name,
                color = shift.color,
                startTime = shift.startTime.toString(),
                endTime = shift.endTime.toString(),
                isActive = shift.isActive
            )
        }
    }
    
    override suspend fun getShiftStatistics(
        shiftId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): ShiftStatistics {
        val shift = scheduleRepository.getShiftById(shiftId) 
            ?: throw IllegalArgumentException("Shift not found")
        
        // Count days with this shift in the date range
        var totalDays = 0
        var current = startDate
        
        while (!current.isAfter(endDate)) {
            val yearMonth = YearMonth.from(current)
            val monthSchedules = scheduleRepository.getSchedulesByMonth(yearMonth).first()
            
            totalDays += monthSchedules.count { schedule ->
                schedule.shift.id == shiftId &&
                !schedule.date.isBefore(startDate) && 
                !schedule.date.isAfter(endDate)
            }
            
            current = current.plusMonths(1).withDayOfMonth(1)
        }
        
        val totalMinutes = totalDays * calculateWorkMinutes(shift.startTime.toString(), shift.endTime.toString())
        val totalHours = totalMinutes / 60.0
        
        return ShiftStatistics(
            shiftId = shiftId,
            shiftName = shift.name,
            totalDays = totalDays,
            totalHours = totalHours,
            dateRange = startDate to endDate
        )
    }
    
    override suspend fun hasScheduleOnDate(date: LocalDate): Boolean {
        return scheduleRepository.getScheduleByDate(date).first() != null
    }
    
    override suspend fun getNextScheduleDate(fromDate: LocalDate): LocalDate? {
        // Check the next few months for schedules
        var current = fromDate
        val maxMonthsToCheck = 12
        
        for (i in 0 until maxMonthsToCheck) {
            val yearMonth = YearMonth.from(current)
            val monthSchedules = scheduleRepository.getSchedulesByMonth(yearMonth).first()
            
            val nextDate = monthSchedules
                .map { it.date }
                .filter { it.isAfter(fromDate) }
                .minOrNull()
            
            if (nextDate != null) {
                return nextDate
            }
            
            current = current.plusMonths(1).withDayOfMonth(1)
        }
        
        return null
    }
    
    private fun calculateWorkMinutes(startTime: String, endTime: String): Int {
        // 简单的时间计算，假设格式为 "HH:mm"
        val start = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val end = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        return if (end > start) end - start else (24 * 60 - start + end)
    }
}