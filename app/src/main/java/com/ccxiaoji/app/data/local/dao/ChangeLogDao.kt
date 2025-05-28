package com.ccxiaoji.app.data.local.dao

import androidx.room.*
import com.ccxiaoji.app.data.local.entity.ChangeLogEntity
import com.ccxiaoji.app.data.sync.SyncStatus

@Dao
interface ChangeLogDao {
    @Query("SELECT * FROM change_log WHERE syncStatus = :syncStatus ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getPendingChanges(syncStatus: SyncStatus = SyncStatus.PENDING_SYNC, limit: Int = 100): List<ChangeLogEntity>
    
    @Query("SELECT * FROM change_log WHERE timestamp > :since ORDER BY timestamp ASC")
    suspend fun getChangesSince(since: Long): List<ChangeLogEntity>
    
    @Insert
    suspend fun insertChange(change: ChangeLogEntity)
    
    @Insert
    suspend fun insertChanges(changes: List<ChangeLogEntity>)
    
    @Query("UPDATE change_log SET syncStatus = :syncStatus WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<Long>, syncStatus: SyncStatus)
    
    @Query("DELETE FROM change_log WHERE syncStatus = :syncStatus AND timestamp < :beforeTimestamp")
    suspend fun deleteSyncedChanges(syncStatus: SyncStatus = SyncStatus.SYNCED, beforeTimestamp: Long)
    
    @Query("DELETE FROM change_log")
    suspend fun deleteAllChanges()
}