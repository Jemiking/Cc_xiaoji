package com.ccxiaoji.feature.ledger.worker.creditcard

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 信用卡还款提醒调度器
 * 负责设置和管理还款提醒的定时任务
 */
@Singleton
class PaymentReminderScheduler @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val DAILY_REMINDER_HOUR = 10 // 每天上午10点提醒
        private const val DAILY_REMINDER_MINUTE = 0
    }
    
    /**
     * 启动还款提醒定时任务
     * 每天上午10点运行一次
     */
    fun schedulePaymentReminders() {
        val currentTimeMillis = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(java.util.Calendar.HOUR_OF_DAY, DAILY_REMINDER_HOUR)
            set(java.util.Calendar.MINUTE, DAILY_REMINDER_MINUTE)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            
            // 如果今天的提醒时间已过，设置为明天
            if (timeInMillis <= currentTimeMillis) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val initialDelay = calendar.timeInMillis - currentTimeMillis
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val reminderRequest = PeriodicWorkRequestBuilder<PaymentReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(PaymentReminderWorker.TAG)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PaymentReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }
    
    /**
     * 取消还款提醒定时任务
     */
    fun cancelPaymentReminders() {
        WorkManager.getInstance(context)
            .cancelUniqueWork(PaymentReminderWorker.WORK_NAME)
    }
    
    /**
     * 立即运行一次还款提醒（用于测试）
     */
    fun runPaymentReminderNow() {
        val immediateRequest = OneTimeWorkRequestBuilder<PaymentReminderWorker>()
            .addTag(PaymentReminderWorker.TAG)
            .build()
        
        WorkManager.getInstance(context).enqueue(immediateRequest)
    }
    
    /**
     * 检查还款提醒是否已启用
     */
    suspend fun isPaymentReminderEnabled(): Boolean {
        return try {
            val workInfos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(PaymentReminderWorker.WORK_NAME)
                .get()
            
            workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            false
        }
    }
}