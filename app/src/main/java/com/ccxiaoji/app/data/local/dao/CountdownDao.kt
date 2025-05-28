package com.ccxiaoji.app.data.local.dao

import androidx.room.*
import com.ccxiaoji.app.data.local.entity.CountdownEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdowns WHERE userId = :userId AND isDeleted = 0 ORDER BY targetDate ASC")
    fun getCountdownsByUser(userId: String): Flow<List<CountdownEntity>>
    
    @Query("SELECT * FROM countdowns WHERE userId = :userId AND targetDate > :currentDate AND isDeleted = 0 ORDER BY targetDate ASC")
    fun getUpcomingCountdowns(userId: String, currentDate: Long): Flow<List<CountdownEntity>>
    
    @Query("SELECT * FROM countdowns WHERE userId = :userId AND targetDate <= :currentDate AND isDeleted = 0 ORDER BY targetDate DESC")
    fun getPastCountdowns(userId: String, currentDate: Long): Flow<List<CountdownEntity>>
    
    @Query("SELECT * FROM countdowns WHERE id = :countdownId AND isDeleted = 0")
    suspend fun getCountdownById(countdownId: String): CountdownEntity?
    
    @Query("SELECT * FROM countdowns WHERE userId = :userId AND showOnWidget = 1 AND isDeleted = 0 ORDER BY targetDate ASC")
    fun getWidgetCountdowns(userId: String): Flow<List<CountdownEntity>>
    
    @Query("SELECT * FROM countdowns WHERE syncStatus != :syncStatus AND isDeleted = 0")
    suspend fun getUnsyncedCountdowns(syncStatus: SyncStatus = SyncStatus.SYNCED): List<CountdownEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdown(countdown: CountdownEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdowns(countdowns: List<CountdownEntity>)
    
    @Update
    suspend fun updateCountdown(countdown: CountdownEntity)
    
    @Query("UPDATE countdowns SET showOnWidget = :showOnWidget, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :countdownId")
    suspend fun updateWidgetVisibility(
        countdownId: String,
        showOnWidget: Boolean,
        timestamp: Long,
        syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
    )
    
    @Query("UPDATE countdowns SET isDeleted = 1, updatedAt = :timestamp, syncStatus = :syncStatus WHERE id = :countdownId")
    suspend fun softDeleteCountdown(countdownId: String, timestamp: Long, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
    
    @Query("UPDATE countdowns SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>, syncStatus: SyncStatus)
}