package com.ccxiaoji.feature.schedule.data.repository

import com.ccxiaoji.core.database.dao.ScheduleDao
import com.ccxiaoji.core.database.dao.ScheduleWithShift
import com.ccxiaoji.core.database.dao.ShiftDao
import com.ccxiaoji.core.database.entity.ScheduleEntity
import com.ccxiaoji.core.database.entity.ShiftEntity
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.SchedulePattern
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 排班仓库实现
 * 负责数据层和领域层之间的数据转换
 */
class ScheduleRepositoryImpl @Inject constructor(
    private val shiftDao: ShiftDao,
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {
    
    init {
        android.util.Log.d("ScheduleRepositoryImpl", "Repository initialized")
    }
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    // ========== 班次相关实现 ==========
    
    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getActiveShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.filter { it.isActive }.map { it.toDomainModel() }
        }
    }
    
    override suspend fun getShiftById(shiftId: Long): Shift? {
        return shiftDao.getShiftById(shiftId)?.toDomainModel()
    }
    
    override suspend fun createShift(shift: Shift): Long {
        return shiftDao.insertShift(shift.toEntity())
    }
    
    override suspend fun updateShift(shift: Shift) {
        shiftDao.updateShift(shift.toEntity(shift.id))
    }
    
    override suspend fun deleteShift(shiftId: Long) {
        shiftDao.deleteShift(shiftId)
    }
    
    override suspend fun isShiftNameExists(name: String, excludeId: Long): Boolean {
        return shiftDao.checkShiftNameExists(name, excludeId) > 0
    }
    
    // ========== 排班相关实现 ==========
    
    override fun getSchedulesByMonth(yearMonth: YearMonth): Flow<List<Schedule>> {
        val startDate = yearMonth.atDay(1).toEpochMillis()
        val endDate = yearMonth.atEndOfMonth().toEpochMillis()
        
        return scheduleDao.getSchedulesWithShiftByMonth(startDate, endDate).map { list ->
            list.map { it.toDomainModel() }
        }
    }
    
    override fun getScheduleByDate(date: LocalDate): Flow<Schedule?> {
        val dateMillis = date.toEpochMillis()
        return scheduleDao.getScheduleByDateFlow(dateMillis).map { entity ->
            if (entity != null) {
                val shift = shiftDao.getShiftById(entity.shiftId)?.toDomainModel()
                shift?.let { Schedule(entity.id, date, it, entity.note) }
            } else {
                null
            }
        }
    }
    
    override suspend fun saveSchedule(schedule: Schedule): Long {
        return scheduleDao.insertOrUpdateSchedule(schedule.toEntity())
    }
    
    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDao.updateSchedule(schedule.toEntity())
    }
    
    override suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDao.deleteSchedule(scheduleId)
    }
    
    override suspend fun saveSchedules(schedules: List<Schedule>) {
        val entities = schedules.map { it.toEntity() }
        scheduleDao.insertSchedules(entities)
    }
    
    override suspend fun createSchedulesByPattern(pattern: SchedulePattern) {
        // 此方法的实现由 CreateScheduleUseCase 处理
        // Repository 只负责基础的数据存储
    }
    
    override suspend fun deleteScheduleByDate(date: LocalDate) {
        scheduleDao.deleteScheduleByDate(date.toEpochMillis())
    }
    
    override suspend fun clearSchedules(startDate: LocalDate, endDate: LocalDate) {
        scheduleDao.clearSchedulesByDateRange(
            startDate.toEpochMillis(),
            endDate.toEpochMillis()
        )
    }
    
    // ========== 统计相关实现 ==========
    
    override suspend fun getMonthlyStatistics(yearMonth: YearMonth): ScheduleStatistics {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        return getStatistics(startDate, endDate)
    }
    
    override suspend fun getStatistics(startDate: LocalDate, endDate: LocalDate): ScheduleStatistics {
        val startMillis = startDate.toEpochMillis()
        val endMillis = endDate.toEpochMillis()
        
        val schedulesList = scheduleDao.getSchedulesWithShiftByMonth(startMillis, endMillis).first()
        val schedules = schedulesList.map { it.toDomainModel() }
        
        val totalDays = schedules.size
        val workDays = schedules.count { it.shift.id != 0L } // 假设 0 表示休息
        val restDays = totalDays - workDays
        
        val shiftDistribution = schedules
            .groupBy { it.shift.name }
            .mapValues { it.value.size }
        
        val totalHours = schedules.sumOf { it.shift.duration }
        
        return ScheduleStatistics(
            totalDays = totalDays,
            workDays = workDays,
            restDays = restDays,
            shiftDistribution = shiftDistribution,
            totalHours = totalHours
        )
    }
    
    // ========== 数据转换扩展函数 ==========
    
    private fun ShiftEntity.toDomainModel(): Shift {
        return Shift(
            id = id,
            name = name,
            startTime = LocalTime.parse(startTime, timeFormatter),
            endTime = LocalTime.parse(endTime, timeFormatter),
            color = color,
            description = description,
            isActive = isActive
        )
    }
    
    private fun Shift.toEntity(id: Long = 0): ShiftEntity {
        return ShiftEntity(
            id = id,
            name = name,
            startTime = startTime.format(timeFormatter),
            endTime = endTime.format(timeFormatter),
            color = color,
            description = description,
            isActive = isActive
        )
    }
    
    private fun ScheduleWithShift.toDomainModel(): Schedule {
        return Schedule(
            id = schedule.id,
            date = LocalDate.ofEpochDay(schedule.date / (24 * 60 * 60 * 1000)),
            shift = shift.toDomainModel(),
            note = schedule.note,
            actualStartTime = schedule.actualStartTime?.let { 
                LocalTime.parse(it, timeFormatter) 
            },
            actualEndTime = schedule.actualEndTime?.let { 
                LocalTime.parse(it, timeFormatter) 
            }
        )
    }
    
    private fun Schedule.toEntity(): ScheduleEntity {
        return ScheduleEntity(
            id = id,
            date = date.toEpochMillis(),
            shiftId = shift.id,
            note = note,
            actualStartTime = actualStartTime?.format(timeFormatter),
            actualEndTime = actualEndTime?.format(timeFormatter)
        )
    }
    
    private fun LocalDate.toEpochMillis(): Long {
        return atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}