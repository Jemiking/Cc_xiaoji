package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    fun startPeriodicSync() {
        Log.d(TAG, "Starting periodic sync")
        try {
        val periodicSyncRequest = SyncWorker.buildPeriodicWorkRequest()
        
            workManager.enqueueUniquePeriodicWork(
                SyncWorker.SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
            Log.d(TAG, "Periodic sync started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting periodic sync", e)
            throw e
        }
    }
    
    fun syncNow() {
        val oneTimeSyncRequest = SyncWorker.buildOneTimeWorkRequest()
        workManager.enqueue(oneTimeSyncRequest)
    }
    
    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.SYNC_WORK_NAME)
    }
    
    fun getSyncStatus(): Flow<SyncState> {
        return workManager.getWorkInfosForUniqueWorkFlow(SyncWorker.SYNC_WORK_NAME)
            .map { workInfos ->
                when {
                    workInfos.isEmpty() -> SyncState.IDLE
                    workInfos.any { it.state == WorkInfo.State.RUNNING } -> SyncState.SYNCING
                    workInfos.any { it.state == WorkInfo.State.FAILED } -> SyncState.ERROR
                    else -> SyncState.SUCCESS
                }
            }
    }
    
    enum class SyncState {
        IDLE,
        SYNCING,
        SUCCESS,
        ERROR
    }
}