package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.HabitEntity
import com.ccxiaoji.core.database.entity.HabitRecordEntity
import com.ccxiaoji.core.database.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt ASC")
    fun getHabitsByUser(userId: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habits WHERE id = :habitId AND isDeleted = 0")
    suspend fun getHabitById(habitId: String): HabitEntity?
    
    @Query("SELECT * FROM habits WHERE userId = :userId AND (title LIKE :query OR description LIKE :query) AND isDeleted = 0 ORDER BY createdAt ASC")
    fun searchHabits(userId: String, query: String): Flow<List<HabitEntity>>
    
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND isDeleted = 0 ORDER BY recordDate DESC")
    fun getHabitRecords(habitId: String): Flow<List<HabitRecordEntity>>
    
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND recordDate = :date AND isDeleted = 0")
    suspend fun getHabitRecordByDate(habitId: String, date: Long): HabitRecordEntity?
    
    @Query("SELECT * FROM habit_records WHERE id = :recordId AND isDeleted = 0")
    suspend fun getHabitRecordById(recordId: String): HabitRecordEntity?
    
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND recordDate >= :startDate AND recordDate <= :endDate AND isDeleted = 0 ORDER BY recordDate ASC")
    suspend fun getHabitRecordsByDateRange(habitId: String, startDate: Long, endDate: Long): List<HabitRecordEntity>
    
    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND recordDate >= :startDate AND recordDate <= :endDate AND isDeleted = 0 ORDER BY recordDate ASC")
    fun getHabitRecordsByDateRangeSync(habitId: String, startDate: Long, endDate: Long): List<HabitRecordEntity>
    
    @Query("""
        SELECT hr.* FROM habit_records hr
        INNER JOIN habits h ON hr.habitId = h.id
        WHERE h.userId = :userId 
        AND hr.recordDate >= :startDate 
        AND hr.recordDate <= :endDate 
        AND hr.isDeleted = 0 
        AND h.isDeleted = 0
        ORDER BY hr.recordDate ASC
    """)
    fun getUserHabitRecordsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<HabitRecordEntity>>
    
    @Query("SELECT COUNT(DISTINCT recordDate) FROM habit_records WHERE habitId = :habitId AND recordDate <= :currentDate AND isDeleted = 0")
    suspend fun getTotalDaysCompleted(habitId: String, currentDate: Long): Int
    
    @Query("""
        SELECT COUNT(DISTINCT recordDate) 
        FROM habit_records 
        WHERE habitId = :habitId 
        AND recordDate <= :currentDate 
        AND isDeleted = 0
    """)
    suspend fun getLongestStreak(habitId: String, currentDate: Long): Int?
    
    @Query("""
        SELECT COUNT(DISTINCT recordDate) 
        FROM habit_records 
        WHERE habitId = :habitId 
        AND recordDate <= :currentDate 
        AND recordDate >= :currentDate - (7 * 86400000)
        AND isDeleted = 0
    """)
    suspend fun getCurrentStreak(habitId: String, currentDate: Long): Int
    
    @Query("SELECT * FROM habits WHERE syncStatus != :syncStatus AND isDeleted = 0")
    suspend fun getUnsyncedHabits(syncStatus: SyncStatus = SyncStatus.SYNCED): List<HabitEntity>
    
    @Query("SELECT * FROM habit_records WHERE syncStatus != :syncStatus AND isDeleted = 0")
    suspend fun getUnsyncedHabitRecords(syncStatus: SyncStatus = SyncStatus.SYNCED): List<HabitRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitRecord(record: HabitRecordEntity)
    
    @Update
    suspend fun updateHabit(habit: HabitEntity)
    
    @Update
    suspend fun updateHabitRecord(record: HabitRecordEntity)
    
    @Query("UPDATE habits SET isDeleted = 1, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: String, timestamp: Long, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
    
    @Query("UPDATE habit_records SET isDeleted = 1, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :recordId")
    suspend fun softDeleteHabitRecord(recordId: String, timestamp: Long, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
    
    @Query("UPDATE habits SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateHabitSyncStatus(ids: List<String>, syncStatus: SyncStatus)
    
    @Query("UPDATE habit_records SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateHabitRecordSyncStatus(ids: List<String>, syncStatus: SyncStatus)
}