package com.ccxiaoji.feature.habit.data.repository

import com.ccxiaoji.feature.habit.data.local.dao.HabitDao
import com.ccxiaoji.feature.habit.data.local.entity.HabitEntity
import com.ccxiaoji.feature.habit.data.local.entity.HabitRecordEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.habit.domain.model.Habit
import com.ccxiaoji.feature.habit.domain.model.HabitRecord
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.domain.repository.HabitRepository
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.*
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val userApi: UserApi
) : HabitRepository {
    override fun getHabits(): Flow<List<Habit>> {
        return habitDao.getHabitsByUser(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getActiveHabitsCount(): Flow<Int> {
        return getHabits().map { it.size }
    }
    
    override fun getTodayCheckedHabitsCount(): Flow<Int> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val todayStart = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        return habitDao.getUserHabitRecordsByDateRange(
            userApi.getCurrentUserId(),
            todayStart,
            todayStart
        ).map { records -> 
            records.distinctBy { it.habitId }.size 
        }
    }
    
    override fun searchHabits(query: String): Flow<List<Habit>> {
        return habitDao.searchHabits(userApi.getCurrentUserId(), "%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getHabitsWithStreaks(): Flow<List<HabitWithStreak>> {
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
    
    override suspend fun createHabit(
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
    ): Habit {
        val habitId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = HabitEntity(
            id = habitId,
            userId = userApi.getCurrentUserId(),
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
        
        // TODO: Log the change for sync through ChangeLogApi
        
        return entity.toDomainModel()
    }
    
    override suspend fun updateHabit(
        habitId: String,
        title: String,
        description: String?,
        period: String,
        target: Int,
        color: String,
        icon: String?
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
        
        // TODO: Log the change for sync through ChangeLogApi
    }
    
    override suspend fun checkInHabit(habitId: String, date: LocalDate) {
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
            
            // TODO: Log the change for sync through ChangeLogApi
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
            
            // TODO: Log the change for sync through ChangeLogApi
        }
    }
    
    override suspend fun deleteHabit(habitId: String) {
        val now = System.currentTimeMillis()
        
        habitDao.softDeleteHabit(habitId, now)
        
        // TODO: Log the change for sync through ChangeLogApi
    }
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

private fun HabitRecordEntity.toDomainModel(): HabitRecord {
    return HabitRecord(
        id = id,
        habitId = habitId,
        recordDate = Instant.fromEpochMilliseconds(recordDate).toLocalDateTime(TimeZone.currentSystemDefault()).date,
        count = count,
        note = note
    )
}