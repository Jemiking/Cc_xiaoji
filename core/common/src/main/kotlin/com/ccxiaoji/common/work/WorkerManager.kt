package com.ccxiaoji.common.work

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一的后台任务管理器
 * 
 * 负责管理应用中所有的WorkManager任务，提供统一的调度和取消接口。
 * 通过这个管理器，可以：
 * - 统一调度所有后台任务
 * - 监控任务执行状态
 * - 取消特定或所有任务
 * - 设置统一的任务约束条件
 */
@Singleton
class WorkerManager @Inject constructor(
    private val context: Context
) {
    
    private val workManager: WorkManager = WorkManager.getInstance(context)
    
    /**
     * 调度一次性任务
     * @param request 一次性工作请求
     * @param tag 任务标签，用于后续查询和取消
     */
    fun scheduleOneTimeWork(request: OneTimeWorkRequest, tag: String? = null) {
        // WorkRequest的tags是不可变的，需要在构建时添加
        // 如果需要添加额外标签，应该在创建请求时完成
        workManager.enqueue(request)
    }
    
    /**
     * 调度周期性任务
     * @param request 周期性工作请求
     * @param uniqueWorkName 唯一工作名称
     * @param existingPeriodicWorkPolicy 已存在工作的处理策略
     */
    fun schedulePeriodicWork(
        request: PeriodicWorkRequest,
        uniqueWorkName: String,
        existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName,
            existingPeriodicWorkPolicy,
            request
        )
    }
    
    /**
     * 取消特定标签的所有任务
     * @param tag 任务标签
     */
    fun cancelWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }
    
    /**
     * 取消特定名称的唯一任务
     * @param uniqueWorkName 唯一工作名称
     */
    fun cancelUniqueWork(uniqueWorkName: String) {
        workManager.cancelUniqueWork(uniqueWorkName)
    }
    
    /**
     * 取消所有任务
     */
    fun cancelAllWork() {
        workManager.cancelAllWork()
    }
    
    /**
     * 获取任务信息
     * @param uniqueWorkName 唯一工作名称
     * @return 工作信息的LiveData
     */
    fun getWorkInfoByName(uniqueWorkName: String) = 
        workManager.getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
    
    /**
     * 获取特定标签的任务信息
     * @param tag 任务标签
     * @return 工作信息列表的LiveData
     */
    fun getWorkInfoByTag(tag: String) = 
        workManager.getWorkInfosByTagLiveData(tag)
    
    /**
     * 创建默认的网络约束
     * 需要网络连接时才执行任务
     */
    fun createNetworkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
    
    /**
     * 创建充电约束
     * 只在设备充电时执行任务
     */
    fun createChargingConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresCharging(true)
            .build()
    }
    
    /**
     * 创建空闲约束
     * 只在设备空闲时执行任务
     */
    fun createIdleConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .build()
    }
    
    /**
     * 创建低电量约束
     * 电量不低时才执行任务
     */
    fun createBatteryNotLowConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
    }
    
    /**
     * 创建组合约束
     * @param requireNetwork 是否需要网络
     * @param requireCharging 是否需要充电
     * @param requireIdle 是否需要空闲
     * @param requireBatteryNotLow 是否需要电量充足
     */
    fun createCombinedConstraints(
        requireNetwork: Boolean = false,
        requireCharging: Boolean = false,
        requireIdle: Boolean = false,
        requireBatteryNotLow: Boolean = false
    ): Constraints {
        return Constraints.Builder().apply {
            if (requireNetwork) {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }
            if (requireCharging) {
                setRequiresCharging(true)
            }
            if (requireIdle) {
                setRequiresDeviceIdle(true)
            }
            if (requireBatteryNotLow) {
                setRequiresBatteryNotLow(true)
            }
        }.build()
    }
}

/**
 * Worker标签常量
 * 用于统一管理所有Worker的标签
 */
object WorkerTags {
    const val SYNC = "sync_worker"
    const val NOTIFICATION = "notification_worker"
    const val RECURRING_TRANSACTION = "recurring_transaction_worker"
    const val CREDIT_CARD_BILL = "credit_card_bill_worker"
    const val CREDIT_CARD_REMINDER = "credit_card_reminder_worker"
    const val SCHEDULE_NOTIFICATION = "schedule_notification_worker"
    const val BACKUP = "backup_worker"
    const val CLEANUP = "cleanup_worker"
}

/**
 * Worker名称常量
 * 用于唯一标识周期性任务
 */
object WorkerNames {
    const val DAILY_SYNC = "daily_sync_work"
    const val RECURRING_TRANSACTION_CHECK = "recurring_transaction_check"
    const val CREDIT_CARD_BILL_GENERATION = "credit_card_bill_generation"
    const val CREDIT_CARD_PAYMENT_REMINDER = "credit_card_payment_reminder"
    const val SCHEDULE_NOTIFICATION_CHECK = "schedule_notification_check"
    const val AUTO_BACKUP = "auto_backup"
    const val DATABASE_CLEANUP = "database_cleanup"
}