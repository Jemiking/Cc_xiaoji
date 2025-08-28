package com.ccxiaoji.feature.ledger.domain.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 通知清理Worker
 * 
 * 功能：
 * - 定时清理过期的自动记账通知
 * - 确保通知不会长期滞留在通知栏
 */
@HiltWorker
class NotificationCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val TAG = "NotificationCleanupWorker"
    }
    
    private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    override suspend fun doWork(): Result {
        val notificationId = inputData.getInt("notificationId", -1)
        
        if (notificationId == -1) {
            Log.e(TAG, "Invalid notificationId in input data")
            return Result.failure()
        }
        
        return try {
            Log.d(TAG, "Cleaning up notification: $notificationId")
            
            // 取消通知
            notificationManager.cancel(notificationId)
            
            Log.d(TAG, "Notification cleaned up successfully: $notificationId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clean up notification: $notificationId", e)
            Result.retry()
        }
    }
}