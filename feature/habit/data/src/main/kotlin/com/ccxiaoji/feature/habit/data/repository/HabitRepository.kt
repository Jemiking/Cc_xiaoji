package com.ccxiaoji.feature.habit.data.repository

import com.ccxiaoji.core.database.dao.HabitDao
import com.ccxiaoji.core.database.dao.ChangeLogDao
import com.ccxiaoji.core.database.entity.HabitEntity
import com.ccxiaoji.core.database.entity.HabitRecordEntity
import com.ccxiaoji.core.database.model.SyncStatus
import com.ccxiaoji.core.database.entity.ChangeLogEntity
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitRecord
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.*
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val changeLogDao: ChangeLogDao,
    private val gson: Gson
) {
    fun getHabits(): Flow<List<Habit>> {
        return habitDao.getHabitsByUser(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getActiveHabitsCount(): Flow<Int> {
        return getHabits().map { it.size }
    }
    
    fun getTodayCheckedHabitsCount(): Flow<Int> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStart = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return habitDao.getUserHabitRecordsByDateRange(
            getCurrentUserId(),
            todayStart,
            todayStart
        ).map { records -> 
            records.distinctBy { it.habitId }.size 
        }
    }
    
    suspend fun getHabitById(habitId: String): Habit? {
        return habitDao.getHabitById(habitId)?.toDomainModel()
    }
    
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        return getHabits().map { habits ->
            habits.map { habit ->
                val streak = calculateStreak(habit.id)
                HabitWithStreak(
                    habit = habit,
                    currentStreak = streak.current,
                    completedCount = streak.total,
                    longestStreak = streak.longest
                )
            }
        }
    }
    
    private suspend fun calculateStreak(habitId: String): StreakInfo {
        // 获取所有habit记录
        val records = habitDao.getHabitRecordsByDateRangeSync(
            habitId = habitId,
            startDate = 0L, // 从最早开始
            endDate = System.currentTimeMillis() // 到现在
        )
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val sortedDates = records
            .map { Instant.fromEpochMilliseconds(it.recordDate).toLocalDateTime(TimeZone.currentSystemDefault()).date }
            .distinct()
            .sorted()
            .reversed()
        
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 1
        
        // Calculate current streak
        if (sortedDates.isNotEmpty()) {
            if (sortedDates.first() == today || sortedDates.first() == today.minus(DatePeriod(days = 1))) {
                currentStreak = 1
                var previousDate = sortedDates.first()
                
                for (i in 1 until sortedDates.size) {
                    val currentDate = sortedDates[i]
                    val daysBetween = previousDate.minus(DatePeriod(days = 1)) == currentDate
                    
                    if (daysBetween) {
                        currentStreak++
                        previousDate = currentDate
                    } else {
                        break
                    }
                }
            }
        }
        
        // Calculate longest streak
        if (sortedDates.size > 1) {
            for (i in 1 until sortedDates.size) {
                val previousDate = sortedDates[i - 1]
                val currentDate = sortedDates[i]
                
                if (previousDate.minus(DatePeriod(days = 1)) == currentDate) {
                    tempStreak++
                } else {
                    longestStreak = maxOf(longestStreak, tempStreak)
                    tempStreak = 1
                }
            }
            longestStreak = maxOf(longestStreak, tempStreak)
        } else {
            longestStreak = 1
        }
        
        return StreakInfo(currentStreak, records.size, longestStreak)
    }
    
    suspend fun createHabit(
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String = "#FF9800",
        icon: String? = null
    ): String {
        val habitId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val habit = HabitEntity(
            id = habitId,
            userId = getCurrentUserId(),
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        habitDao.insertHabit(habit)
        
        // Log the change for sync
        logChange("habits", habitId, "INSERT", habit)
        
        return habitId
    }
    
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ) {
        val existingHabit = habitDao.getHabitById(habitId) ?: return
        
        val updatedHabit = existingHabit.copy(
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        habitDao.updateHabit(updatedHabit)
        
        // Log the change for sync
        logChange("habits", habitId, "UPDATE", mapOf(
            "id" to habitId,
            "title" to title,
            "description" to description,
            "period" to period,
            "target" to target,
            "color" to color,
            "icon" to icon
        ))
    }
    
    suspend fun checkInHabit(habitId: String, date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) {
        val recordDate = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        // Check if already checked in today
        val existingRecord = habitDao.getHabitRecordByDate(habitId, recordDate)
        if (existingRecord != null) {
            // Update count
            val updatedRecord = existingRecord.copy(
                count = existingRecord.count + 1,
                updatedAt = System.currentTimeMillis()
            )
            habitDao.updateHabitRecord(updatedRecord)
            
            logChange("habit_records", existingRecord.id, "UPDATE", updatedRecord)
        } else {
            // Create new record
            val recordId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            
            val record = HabitRecordEntity(
                id = recordId,
                habitId = habitId,
                recordDate = recordDate,
                count = 1,
                createdAt = now,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_SYNC
            )
            
            habitDao.insertHabitRecord(record)
            
            logChange("habit_records", recordId, "INSERT", record)
        }
    }
    
    suspend fun deleteHabit(habitId: String) {
        val now = System.currentTimeMillis()
        
        habitDao.softDeleteHabit(habitId, now)
        
        // Log the change for sync
        logChange("habits", habitId, "DELETE", mapOf("id" to habitId))
    }
    
    private suspend fun logChange(table: String, rowId: String, operation: String, payload: Any) {
        val changeLog = ChangeLogEntity(
            tableName = table,
            rowId = rowId,
            operation = operation,
            payload = gson.toJson(payload),
            timestamp = System.currentTimeMillis()
        )
        changeLogDao.insertChange(changeLog)
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get the actual current user ID
        return "current_user_id"
    }
    
    private data class StreakInfo(
        val current: Int,
        val total: Int,
        val longest: Int
    )
}

private fun HabitEntity.toDomainModel(): Habit {
    return Habit(
        id = id,
        title = title,
        description = description,
        period = period,
        target = target,
        color = color,
        icon = icon,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}