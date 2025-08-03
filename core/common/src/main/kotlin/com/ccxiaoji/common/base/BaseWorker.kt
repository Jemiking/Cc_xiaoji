package com.ccxiaoji.common.base

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 基础Worker类，提供统一的错误处理和日志记录
 * 
 * 这个抽象类为所有Worker提供了标准化的实现模式，包括：
 * - 统一的错误处理机制
 * - 进度报告支持
 * - 重试策略
 * - 日志记录
 * 
 * @sample
 * ```kotlin
 * @HiltWorker
 * class MyWorker @AssistedInject constructor(
 *     @Assisted context: Context,
 *     @Assisted workerParams: WorkerParameters,
 *     private val repository: MyRepository
 * ) : BaseWorker(context, workerParams) {
 *     
 *     override suspend fun performWork(): Result {
 *         // 执行具体的工作
 *         repository.syncData()
 *         return Result.success()
 *     }
 *     
 *     override fun getWorkerName() = "MyWorker"
 * }
 * ```
 */
abstract class BaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    /**
     * 获取Worker的名称，用于日志记录
     */
    protected abstract fun getWorkerName(): String
    
    /**
     * 执行具体的工作任务
     * @return 工作结果
     */
    protected abstract suspend fun performWork(): Result
    
    /**
     * 获取最大重试次数，默认为3次
     */
    protected open fun getMaxRetryCount(): Int = 3
    
    /**
     * 是否在IO调度器上执行工作，默认为true
     */
    protected open fun shouldRunOnIoDispatcher(): Boolean = true
    
    final override suspend fun doWork(): Result {
        val workerName = getWorkerName()
        
        return try {
            logInfo("$workerName started")
            
            val result = if (shouldRunOnIoDispatcher()) {
                withContext(Dispatchers.IO) {
                    performWork()
                }
            } else {
                performWork()
            }
            
            // Log the result based on the Worker's outcome
            logInfo("$workerName finished with result: $result")
            
            result
        } catch (e: Exception) {
            logError("$workerName encountered error: ${e.message}", e)
            
            // 检查是否应该重试
            if (runAttemptCount < getMaxRetryCount()) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
    
    /**
     * 更新工作进度
     * @param progress 进度数据
     */
    protected suspend fun updateProgress(progress: Data) {
        setProgress(progress)
    }
    
    /**
     * 更新工作进度
     * @param percent 进度百分比（0-100）
     */
    protected suspend fun updateProgress(percent: Int) {
        val progress = workDataOf("progress" to percent)
        updateProgress(progress)
    }
    
    /**
     * 记录信息日志
     */
    protected fun logInfo(message: String) {
        // TODO: 接入实际的日志系统
        println("[INFO] $message")
    }
    
    /**
     * 记录警告日志
     */
    protected fun logWarning(message: String) {
        // TODO: 接入实际的日志系统
        println("[WARN] $message")
    }
    
    /**
     * 记录错误日志
     */
    protected fun logError(message: String, throwable: Throwable? = null) {
        // TODO: 接入实际的日志系统
        println("[ERROR] $message")
        throwable?.printStackTrace()
    }
}

/**
 * Worker配置构建器基类
 * 提供统一的Worker配置创建方法
 */
abstract class BaseWorkerBuilder<W : ListenableWorker> {
    
    /**
     * 创建周期性工作请求
     * @param repeatInterval 重复间隔
     * @param timeUnit 时间单位
     * @param constraints 工作约束条件
     * @return PeriodicWorkRequest
     */
    inline fun <reified W : ListenableWorker> createPeriodicWorkRequest(
        repeatInterval: Long,
        timeUnit: java.util.concurrent.TimeUnit,
        constraints: Constraints? = null
    ): PeriodicWorkRequest {
        val builder = PeriodicWorkRequestBuilder<W>(repeatInterval, timeUnit)
        
        constraints?.let {
            builder.setConstraints(it)
        }
        
        // 直接添加标签，避免调用protected方法
        builder.addTag(getWorkerTag())
        
        return builder.build()
    }
    
    /**
     * 创建一次性工作请求
     * @param constraints 工作约束条件
     * @param inputData 输入数据
     * @return OneTimeWorkRequest
     */
    inline fun <reified W : ListenableWorker> createOneTimeWorkRequest(
        constraints: Constraints? = null,
        inputData: Data? = null
    ): OneTimeWorkRequest {
        val builder = OneTimeWorkRequestBuilder<W>()
        
        constraints?.let {
            builder.setConstraints(it)
        }
        
        inputData?.let {
            builder.setInputData(it)
        }
        
        // 直接添加标签，避免调用protected方法
        builder.addTag(getWorkerTag())
        
        return builder.build()
    }
    
    /**
     * 配置WorkRequest，子类可以重写此方法添加额外配置
     */
    protected open fun configureWorkRequest(builder: WorkRequest.Builder<*, *>) {
        // 默认配置
        builder.addTag(getWorkerTag())
    }
    
    /**
     * 获取Worker的标签
     */
    abstract fun getWorkerTag(): String
    
    /**
     * 创建默认的网络约束
     */
    protected fun createNetworkConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
    
    /**
     * 创建充电时执行的约束
     */
    protected fun createChargingConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresCharging(true)
            .build()
    }
    
    /**
     * 创建设备空闲时执行的约束
     */
    protected fun createIdleConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiresDeviceIdle(true)
            .build()
    }
}