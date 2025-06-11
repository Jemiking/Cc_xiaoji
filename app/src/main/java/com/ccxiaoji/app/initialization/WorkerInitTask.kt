package com.ccxiaoji.app.initialization

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.ccxiaoji.app.data.sync.CreditCardBillWorker
import com.ccxiaoji.app.data.sync.CreditCardReminderManager
import com.ccxiaoji.feature.ledger.data.worker.RecurringTransactionWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Worker初始化任务
 * 
 * 负责注册各种后台任务Worker
 * 采用延迟策略，非关键Worker延迟注册
 */
@Singleton
class WorkerInitTask @Inject constructor(
    @ApplicationContext private val context: Context,
    private val creditCardReminderManager: CreditCardReminderManager
) {
    companion object {
        private const val TAG = "WorkerInitTask"
    }
    
    /**
     * 注册关键Worker
     * 这些Worker需要立即注册
     */
    fun registerCriticalWorkers() {
        Log.d(TAG, "Registering critical workers")
        // 目前没有关键Worker需要立即注册
    }
    
    /**
     * 注册高优先级Worker
     * 这些Worker在应用启动后尽快注册
     */
    fun registerHighPriorityWorkers() {
        Log.d(TAG, "Registering high priority workers")
        
        try {
            // 注册定期交易Worker
            Log.d(TAG, "Registering RecurringTransactionWorker")
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                RecurringTransactionWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                RecurringTransactionWorker.createPeriodicWorkRequest()
            )
            Log.d(TAG, "RecurringTransactionWorker registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register RecurringTransactionWorker", e)
        }
    }
    
    /**
     * 注册普通优先级Worker
     * 这些Worker可以延迟注册
     */
    fun registerNormalPriorityWorkers() {
        Log.d(TAG, "Registering normal priority workers")
        
        try {
            // 启动信用卡提醒服务
            Log.d(TAG, "Starting CreditCardReminderManager")
            creditCardReminderManager.startPeriodicReminders()
            Log.d(TAG, "CreditCardReminderManager started")
            
            // 注册信用卡账单生成Worker
            Log.d(TAG, "Registering CreditCardBillWorker")
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CreditCardBillWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                CreditCardBillWorker.createPeriodicWorkRequest()
            )
            Log.d(TAG, "CreditCardBillWorker registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register normal priority workers", e)
        }
    }
    
    /**
     * 注册低优先级Worker
     * 这些Worker可以大幅延迟注册
     */
    fun registerLowPriorityWorkers() {
        Log.d(TAG, "Registering low priority workers")
        // 目前没有低优先级Worker
    }
}