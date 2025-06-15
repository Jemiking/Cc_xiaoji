package com.ccxiaoji.feature.schedule.data.repository

// 项目内部 - Core模块
import com.ccxiaoji.core.database.dao.ScheduleDao
import com.ccxiaoji.core.database.dao.ScheduleWithShift
import com.ccxiaoji.core.database.dao.ShiftDao
import com.ccxiaoji.core.database.dao.ShiftStatistics
import com.ccxiaoji.core.database.entity.ScheduleEntity
import com.ccxiaoji.core.database.entity.ShiftEntity
import com.ccxiaoji.core.database.model.SyncStatus

// 项目内部 - Data层
import com.ccxiaoji.feature.schedule.data.local.dao.getSchedulesBetweenDates

// 项目内部 - Domain层
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.SchedulePattern
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.domain.repository.ScheduleRepository

// 第三方库
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Java/Kotlin标准库
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 排班仓库实现类
 * 负责排班和班次数据的持久化操作
 */
@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    private val shiftDao: ShiftDao
) : ScheduleRepository {
    
    // ========== 转换方法 ==========
    
    private fun ShiftEntity.toDomainModel(): Shift {
        return Shift(
            id = id,
            name = name,
            startTime = parseTime(startTime),
            endTime = parseTime(endTime),
            color = color,
            description = description,
            isActive = isActive
        )
    }
    
    private fun Shift.toEntity(): ShiftEntity {
        return ShiftEntity(
            id = id,
            name = name,
            startTime = formatTime(startTime),
            endTime = formatTime(endTime),
            color = color,
            description = description,
            isActive = isActive,
            syncStatus = SyncStatus.SYNCED, // Domain层不包含syncStatus，默认使用SYNCED
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun ScheduleEntity.toDomainModel(shift: Shift): Schedule {
        return Schedule(
            id = id,
            date = LocalDate.ofEpochDay(date / 86400000),
            shift = shift,
            note = note,
            actualStartTime = null, // 实际打卡时间，如果需要可以从其他地方获取
            actualEndTime = null
        )
    }
    
    private fun Schedule.toEntity(): ScheduleEntity {
        return ScheduleEntity(
            id = id,
            date = date.toEpochDay() * 86400000,
            shiftId = shift.id,
            note = note,
            syncStatus = SyncStatus.SYNCED, // Domain层不包含syncStatus，默认使用SYNCED
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 解析时间字符串为LocalTime
     * @param timeStr 格式为 "HH:mm" 的时间字符串
     */
    private fun parseTime(timeStr: String): LocalTime {
        val parts = timeStr.split(":")
        return LocalTime.of(parts[0].toInt(), parts[1].toInt())
    }
    
    /**
     * 格式化LocalTime为字符串
     * @return 格式为 "HH:mm" 的时间字符串
     */
    private fun formatTime(time: LocalTime): String {
        return String.format("%02d:%02d", time.hour, time.minute)
    }
    
    // ========== 班次相关 ==========
    
    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override fun getActiveShifts(): Flow<List<Shift>> {
        return getAllShifts() // 因为DAO已经过滤了isActive
    }
    
    override suspend fun getShiftById(shiftId: Long): Shift? {
        return shiftDao.getShiftById(shiftId)?.toDomainModel()
    }
    
    override suspend fun createShift(shift: Shift): Long {
        return shiftDao.insertShift(shift.toEntity())
    }
    
    override suspend fun updateShift(shift: Shift) {
        shiftDao.updateShift(shift.toEntity())
    }
    
    override suspend fun deleteShift(shiftId: Long) {
        shiftDao.deleteShift(shiftId)
    }
    
    override suspend fun isShiftNameExists(name: String, excludeId: Long): Boolean {
        return shiftDao.checkShiftNameExists(name, excludeId) > 0
    }
    
    // ========== 排班相关 ==========
    
    override fun getSchedulesByMonth(yearMonth: YearMonth): Flow<List<Schedule>> {
        val startDate = yearMonth.atDay(1).toEpochDay() * 86400000
        val endDate = yearMonth.atEndOfMonth().toEpochDay() * 86400000
        
        return scheduleDao.getSchedulesWithShiftByMonth(startDate, endDate).map { list ->
            list.map { scheduleWithShift ->
                scheduleWithShift.schedule.toDomainModel(
                    scheduleWithShift.shift.toDomainModel()
                )
            }
        }
    }
    
    override fun getScheduleByDate(date: LocalDate): Flow<Schedule?> {
        return scheduleDao.getScheduleWithShiftByDate(date.toEpochDay() * 86400000).map { scheduleWithShift ->
            scheduleWithShift?.let {
                it.schedule.toDomainModel(it.shift.toDomainModel())
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
        scheduleDao.insertSchedules(schedules.map { it.toEntity() })
    }
    
    override suspend fun createSchedulesByPattern(pattern: SchedulePattern) {
        // 这个方法的实现在UseCase层，这里只提供基础方法
        // UseCase会调用saveSchedules方法来保存生成的排班
    }
    
    override suspend fun deleteScheduleByDate(date: LocalDate) {
        scheduleDao.deleteScheduleByDate(date.toEpochDay() * 86400000)
    }
    
    override suspend fun clearSchedules(startDate: LocalDate, endDate: LocalDate) {
        scheduleDao.clearSchedulesByDateRange(
            startDate.toEpochDay() * 86400000,
            endDate.toEpochDay() * 86400000
        )
    }
    
    // ========== 统计相关 ==========
    
    override suspend fun getMonthlyStatistics(yearMonth: YearMonth): ScheduleStatistics {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        return getStatistics(startDate, endDate)
    }
    
    override suspend fun getStatistics(startDate: LocalDate, endDate: LocalDate): ScheduleStatistics {
        // 使用优化的数据库查询进行统计
        val shiftStats = scheduleDao.getScheduleStatistics(
            startDate.toEpochDay() * 86400000,
            endDate.toEpochDay() * 86400000
        )
        
        // 转换统计结果
        val shiftDistribution = shiftStats.associate { it.shiftName to it.count }
        val totalHours = shiftStats.sumOf { it.totalHours }
        val workDays = shiftStats.sumOf { it.count }
        
        // 计算总天数（包括休息日）
        val totalDays = startDate.until(endDate.plusDays(1), java.time.temporal.ChronoUnit.DAYS).toInt()
        val restDays = totalDays - workDays
        
        return ScheduleStatistics(
            totalDays = totalDays,
            workDays = workDays,
            restDays = restDays,
            shiftDistribution = shiftDistribution,
            totalHours = totalHours
        )
    }
    
    /**
     * 计算班次时长（小时）
     */
    private fun calculateHours(startTime: String, endTime: String): Double {
        // 解析时间格式 "HH:mm"
        val start = startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        val end = endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
        
        val minutes = if (end >= start) {
            end - start
        } else {
            // 跨天的情况
            (24 * 60) - start + end
        }
        
        return minutes / 60.0
    }
}