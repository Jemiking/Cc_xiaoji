package com.ccxiaoji.core.database.dao

import androidx.room.*
import com.ccxiaoji.core.database.entity.ChangeLogEntity
import com.ccxiaoji.core.database.model.SyncStatus

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
    
    @Query("UPDATE change_log SET syncStatus = :syncStatus WHERE tableName = :table AND rowId = :rowId")
    suspend fun markForResync(table: String, rowId: String, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC)
}