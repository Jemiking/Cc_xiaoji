package com.ccxiaoji.shared.notification.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * 每日检查Worker
 * 用于执行每日定时任务，如检查预算使用情况等
 */
class DailyCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // TODO: 实现预算检查等每日任务
        // 这里可以注入Repository来检查预算使用情况等
        
        return Result.success()
    }
}