package com.ccxiaoji.shared.sync.data

import android.content.Context
import android.util.Log
import androidx.work.*
import com.ccxiaoji.shared.sync.api.SyncState
import com.ccxiaoji.shared.sync.data.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步管理器实现
 * 负责管理同步任务的调度和状态
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    suspend fun startPeriodicSync() {
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
    
    suspend fun syncNow() {
        val oneTimeSyncRequest = SyncWorker.buildOneTimeWorkRequest()
        workManager.enqueue(oneTimeSyncRequest)
    }
    
    suspend fun cancelSync() {
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
    
}