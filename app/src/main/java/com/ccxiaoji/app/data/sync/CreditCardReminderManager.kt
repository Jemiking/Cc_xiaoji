package com.ccxiaoji.app.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class CreditCardReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    private val workManager = WorkManager.getInstance(context)
    
    fun startPeriodicReminders() {
        Log.d(TAG, "Starting periodic credit card reminders")
        try {
            val periodicReminderRequest = CreditCardReminderWorker.createPeriodicWorkRequest()
            
            workManager.enqueueUniquePeriodicWork(
                CreditCardReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicReminderRequest
            )
            Log.d(TAG, "Periodic credit card reminders started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting periodic credit card reminders", e)
            throw e
        }
    }
    
    fun checkRemindersNow() {
        val oneTimeReminderRequest = CreditCardReminderWorker.createOneTimeWorkRequest()
        workManager.enqueue(oneTimeReminderRequest)
    }
    
    fun cancelReminders() {
        workManager.cancelUniqueWork(CreditCardReminderWorker.WORK_NAME)
    }
}