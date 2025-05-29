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
import kotlinx.datetime.*
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
    
    fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
        return getHabits().map { habits ->
            habits.map { habit ->
                val currentStreak = habitDao.getCurrentStreak(
                    habit.id,
                    Clock.System.now().toEpochMilliseconds()
                )
                HabitWithStreak(habit, currentStreak)
            }
        }
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
    val currentStreak: Int
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