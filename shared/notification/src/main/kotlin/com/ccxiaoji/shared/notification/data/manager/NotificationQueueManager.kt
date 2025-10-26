package com.ccxiaoji.shared.notification.data.manager

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ccxiaoji.core.database.dao.NotificationQueueDao
import com.ccxiaoji.core.database.entity.NotificationQueueEntity
import com.ccxiaoji.core.database.entity.NotificationStatus
import com.ccxiaoji.core.database.entity.NotificationType
import com.ccxiaoji.shared.notification.data.worker.UnifiedNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知队列管理器
 * - 入队/取消
 * - 同步创建/取消 WorkManager 任务
 */
@Singleton
class NotificationQueueManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val queueDao: NotificationQueueDao
){
    companion object {
        private const val UNIQUE_WORK_PREFIX = "notif_queue_"
        const val KEY_QUEUE_ID = "queue_id"
    }

    /**
     * 入队并创建对应的 WorkManager 任务
     */
    suspend fun enqueue(
        type: NotificationType,
        sourceModule: String,
        sourceId: String?,
        title: String,
        message: String,
        scheduledAt: Long,
        userId: String = "local"
    ): String {
        // 1) 基于 sourceId 的去重：存在进行中/待发送则不重复创建；
        if (!sourceId.isNullOrBlank()) {
            val existing = queueDao.findActiveBySource(type, sourceModule, sourceId)
            if (existing != null) {
                // 已存在：如果仍是待发送，可考虑提早/更新内容；如已在处理，直接返回现有ID
                if (existing.status == NotificationStatus.PENDING) {
                    val newWhen = kotlin.math.min(existing.scheduledAt, scheduledAt)
                    // 取消并以相同唯一名重新入队（REPLACE）以更新延时；
                    val uniqueName = UNIQUE_WORK_PREFIX + existing.id
                    WorkManager.getInstance(context).cancelUniqueWork(uniqueName)

                    // 更新DB中计划时间与内容
                    queueDao.updateScheduleAndContent(existing.id, newWhen, title, message)

                    val delay = (newWhen - System.currentTimeMillis()).coerceAtLeast(0)
                    val req = OneTimeWorkRequestBuilder<UnifiedNotificationWorker>()
                        .setInputData(workDataOf(KEY_QUEUE_ID to existing.id))
                        .setInitialDelay(delay, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .addTag(existing.id)
                        .build()
                    WorkManager.getInstance(context)
                        .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, req)
                    queueDao.updateStatusAndWorker(existing.id, NotificationStatus.PENDING, req.id.toString())
                }
                return existing.id
            }
        }

        // 2) 无重复：正常入队
        val id = UUID.randomUUID().toString()
        val entity = NotificationQueueEntity(
            id = id,
            type = type,
            sourceModule = sourceModule,
            sourceId = sourceId,
            title = title,
            message = message,
            scheduledAt = scheduledAt,
            status = NotificationStatus.PENDING,
            workerId = null,
            attempts = 0,
            createdAt = System.currentTimeMillis(),
            sentAt = null,
            userId = userId
        )
        queueDao.insert(entity)

        val delayMs = (scheduledAt - System.currentTimeMillis()).coerceAtLeast(0)
        val workRequest = OneTimeWorkRequestBuilder<UnifiedNotificationWorker>()
            .setInputData(workDataOf(KEY_QUEUE_ID to id))
            .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .addTag(id)
            .build()

        val uniqueName = UNIQUE_WORK_PREFIX + id
        WorkManager.getInstance(context)
            .enqueueUniqueWork(uniqueName, ExistingWorkPolicy.REPLACE, workRequest)

        queueDao.updateStatusAndWorker(id, NotificationStatus.PENDING, workRequest.id.toString())
        return id
    }

    /**
     * 取消通知（同时取消 WorkManager 任务）
     */
    suspend fun cancel(id: String) {
        try {
            queueDao.cancel(id, NotificationStatus.CANCELLED)
        } finally {
            val uniqueName = UNIQUE_WORK_PREFIX + id
            WorkManager.getInstance(context).cancelUniqueWork(uniqueName)
        }
    }

    /**
     * 根据来源取消（去重辅助）：同一 source 的活动队列全部取消
     */
    suspend fun cancelBySource(type: NotificationType, sourceModule: String, sourceId: String) {
        val all = queueDao.findAllActiveBySource(type, sourceModule, sourceId)
        all.forEach { cancel(it.id) }
    }
}
