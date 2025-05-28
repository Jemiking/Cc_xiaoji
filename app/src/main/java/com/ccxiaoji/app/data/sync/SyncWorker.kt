package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ccxiaoji.app.data.local.CcDatabase
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.data.remote.api.SyncApi
import com.ccxiaoji.app.data.repository.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val database: CcDatabase,
    private val syncApi: SyncApi,
    private val userRepository: UserRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentUser = userRepository.getCurrentUser() ?: return@withContext Result.failure()
            
            // Upload local changes
            uploadChanges()
            
            // Download remote changes
            val lastSyncTime = userRepository.getLastSyncTime()
            downloadChanges(lastSyncTime)
            
            // Update last sync time
            userRepository.updateLastSyncTime(System.currentTimeMillis())
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    private suspend fun uploadChanges() {
        val changeLogDao = database.changeLogDao()
        val pendingChanges = changeLogDao.getPendingChanges(limit = BATCH_SIZE)
        
        if (pendingChanges.isEmpty()) return
        
        val uploadRequest = pendingChanges.map { change ->
            SyncUploadItem(
                table = change.tableName,
                rowId = change.rowId,
                operation = change.operation,
                payload = change.payload,
                timestamp = change.timestamp
            )
        }
        
        val response = syncApi.uploadChanges(uploadRequest)
        if (response.isSuccessful) {
            changeLogDao.updateSyncStatus(
                pendingChanges.map { it.id },
                SyncStatus.SYNCED
            )
        }
    }
    
    private suspend fun downloadChanges(since: Long) {
        val response = syncApi.getChanges(since)
        if (!response.isSuccessful) return
        
        val changes = response.body() ?: return
        
        // Apply changes to local database
        changes.forEach { change ->
            when (change.table) {
                "transactions" -> applyTransactionChanges(change)
                "tasks" -> applyTaskChanges(change)
                "habits" -> applyHabitChanges(change)
                "habit_records" -> applyHabitRecordChanges(change)
                "countdowns" -> applyCountdownChanges(change)
            }
        }
    }
    
    private suspend fun applyTransactionChanges(change: SyncChange) {
        // Implementation for applying transaction changes
    }
    
    private suspend fun applyTaskChanges(change: SyncChange) {
        // Implementation for applying task changes
    }
    
    private suspend fun applyHabitChanges(change: SyncChange) {
        // Implementation for applying habit changes
    }
    
    private suspend fun applyHabitRecordChanges(change: SyncChange) {
        // Implementation for applying habit record changes
    }
    
    private suspend fun applyCountdownChanges(change: SyncChange) {
        // Implementation for applying countdown changes
    }
    
    companion object {
        private const val MAX_RETRY_COUNT = 3
        private const val BATCH_SIZE = 100
        const val SYNC_WORK_NAME = "periodic_sync"
        
        fun buildPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
        
        fun buildOneTimeWorkRequest(): OneTimeWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}

data class SyncUploadItem(
    val table: String,
    val rowId: String,
    val operation: String,
    val payload: String,
    val timestamp: Long
)

data class SyncChange(
    val table: String,
    val rows: List<Map<String, Any>>
)