package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.HabitDao
import com.ccxiaoji.app.data.local.dao.ChangeLogDao
import com.ccxiaoji.app.data.local.entity.HabitEntity
import com.ccxiaoji.app.data.local.entity.HabitRecordEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.data.local.entity.ChangeLogEntity
import com.ccxiaoji.app.domain.model.Habit
import com.ccxiaoji.app.domain.model.HabitRecord
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
    
    fun searchHabits(query: String): Flow<List<Habit>> {
        return habitDao.searchHabits(getCurrentUserId(), "%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        return getHabits().flatMapLatest { habits ->
            flow {
                val habitsWithStreaks = habits.map { habit ->
                    val now = Clock.System.now()
                    val currentStreak = habitDao.getCurrentStreak(
                        habit.id,
                        now.toEpochMilliseconds()
                    )
                    
                    // Calculate completed count for last 30 days
                    val thirtyDaysAgo = now.minus(DateTimePeriod(days = 30), TimeZone.currentSystemDefault()).toEpochMilliseconds()
                    val records = habitDao.getHabitRecordsByDateRangeSync(
                        habit.id,
                        thirtyDaysAgo,
                        now.toEpochMilliseconds()
                    )
                    val completedCount = records.size
                    
                    // Calculate longest streak
                    val longestStreak = calculateLongestStreak(habit.id, records)
                    
                    HabitWithStreak(habit, currentStreak, completedCount, longestStreak)
                }
                emit(habitsWithStreaks)
            }.flowOn(Dispatchers.IO)
        }
    }
    
    private fun calculateLongestStreak(habitId: String, records: List<HabitRecordEntity>): Int {
        if (records.isEmpty()) return 0
        
        val sortedDates = records.map { 
            Instant.fromEpochMilliseconds(it.recordDate)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date 
        }.sorted()
        
        var maxStreak = 1
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val prevDate = sortedDates[i - 1]
            val currDate = sortedDates[i]
            
            if (prevDate.plus(1, DateTimeUnit.DAY) == currDate) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        
        return maxStreak
    }
    
    suspend fun createHabit(
        title: String,
        description: String? = null,
        period: String = "daily",
        target: Int = 1,
        color: String = "#3A7AFE",
        icon: String? = null
    ): Habit {
        val habitId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = HabitEntity(
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
        
        habitDao.insertHabit(entity)
        
        // Log the change for sync
        logChange("habits", habitId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String? = null,
        period: String = "daily",
        target: Int = 1,
        color: String = "#3A7AFE",
        icon: String? = null
    ) {
        val existingHabit = habitDao.getHabitById(habitId) ?: return
        
        val now = System.currentTimeMillis()
        val updatedHabit = existingHabit.copy(
            title = title,
            description = description,
            period = period,
            target = target,
            color = color,
            icon = icon,
            updatedAt = now,
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
}

data class HabitWithStreak(
    val habit: Habit,
    val currentStreak: Int,
    val completedCount: Int = 0,
    val longestStreak: Int = 0
)

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

private fun HabitRecordEntity.toDomainModel(): HabitRecord {
    return HabitRecord(
        id = id,
        habitId = habitId,
        recordDate = Instant.fromEpochMilliseconds(recordDate).toLocalDateTime(TimeZone.currentSystemDefault()).date,
        count = count,
        note = note
    )
}