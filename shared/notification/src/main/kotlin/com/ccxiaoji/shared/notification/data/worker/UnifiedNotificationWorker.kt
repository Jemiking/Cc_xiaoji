package com.ccxiaoji.shared.notification.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.core.database.dao.NotificationQueueDao
import com.ccxiaoji.core.database.entity.NotificationStatus
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.manager.NotificationQueueManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 统一通知Worker：
 * - 根据队列ID读取通知详情
 * - 更新状态：PENDING -> PROCESSING -> SENT/FAILED
 * - 实际发送由 NotificationApi 承担
 */
@HiltWorker
class UnifiedNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val queueDao: NotificationQueueDao,
    private val notificationApi: NotificationApi,
    private val queueManager: NotificationQueueManager
) : CoroutineWorker(appContext, params) {

    companion object { private const val TAG = "UnifiedNotificationWorker" }

    override suspend fun doWork(): Result {
        val queueId = inputData.getString(NotificationQueueManager.KEY_QUEUE_ID)
            ?: return Result.failure()
        return runCatching {
            val entity = queueDao.getById(queueId)
            if (entity == null) {
                Log.w(TAG, "Queue entity not found: $queueId")
                return Result.success()
            }
            if (entity.status != NotificationStatus.PENDING) {
                Log.i(TAG, "Skip non-pending entity: ${entity.id}, status=${entity.status}")
                return Result.success()
            }

            // 标记处理中
            queueDao.updateStatusAndWorker(entity.id, NotificationStatus.PROCESSING, entity.workerId)

            // 按类型分发到专用通知渠道（缺失字段时回退到通用通知）
            when (entity.type) {
                com.ccxiaoji.core.database.entity.NotificationType.TASK -> {
                    val id = entity.sourceId
                    if (id != null && entity.message.isNotBlank()) {
                        notificationApi.sendTaskReminder(id, entity.title, entity.message)
                    } else {
                        notificationApi.sendGeneralNotification("任务提醒", "${entity.title}")
                    }
                }
                com.ccxiaoji.core.database.entity.NotificationType.HABIT -> {
                    val id = entity.sourceId
                    if (id != null) {
                        notificationApi.sendHabitReminder(id, entity.title)
                        // 自动入队下一次（次日同一时间）
                        val nextAt = entity.scheduledAt + 24L * 60L * 60L * 1000L
                        runCatching {
                            queueManager.enqueue(
                                type = com.ccxiaoji.core.database.entity.NotificationType.HABIT,
                                sourceModule = "habit",
                                sourceId = id,
                                title = entity.title,
                                message = "",
                                scheduledAt = nextAt,
                                userId = entity.userId
                            )
                        }
                    } else {
                        notificationApi.sendGeneralNotification("习惯打卡提醒", "${entity.title}")
                    }
                }
                else -> {
                    notificationApi.sendGeneralNotification(entity.title, entity.message)
                }
            }

            // 成功
            queueDao.markSent(entity.id, NotificationStatus.SENT, System.currentTimeMillis())
            Log.i(TAG, "Notification sent and marked: ${entity.id}")
            Result.success()
        }.getOrElse { t ->
            Log.e(TAG, "Failed to process queue: $queueId, ${t.message}", t)
            runCatching { queueDao.markFailed(queueId) }
            Result.retry()
        }
    }
}
