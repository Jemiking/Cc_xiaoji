package com.ccxiaoji.shared.sync.data.worker

import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * 同步Worker的公共配置和常量
 */
object SyncWorkerConfig {
    const val SYNC_WORK_NAME = "periodic_sync"
    const val MAX_RETRY_COUNT = 3
    const val BATCH_SIZE = 100
    
    /**
     * 创建周期性同步的约束条件
     */
    fun createSyncConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
    
    /**
     * 创建退避策略配置
     */
    fun getBackoffPolicy() = BackoffPolicy.EXPONENTIAL
    
    /**
     * 获取退避延迟时间
     */
    fun getBackoffDelay() = WorkRequest.MIN_BACKOFF_MILLIS
}