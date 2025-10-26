package com.ccxiaoji.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ccxiaoji.core.database.entity.NotificationQueueEntity
import com.ccxiaoji.core.database.entity.NotificationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationQueueDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: NotificationQueueEntity)

    @Update
    suspend fun update(entity: NotificationQueueEntity)

    @Query("SELECT * FROM notification_queue WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): NotificationQueueEntity?

    @Query(
        "SELECT * FROM notification_queue " +
            "WHERE status = :pending AND scheduledAt <= :now ORDER BY scheduledAt ASC LIMIT :limit"
    )
    suspend fun findDuePending(now: Long, pending: NotificationStatus = NotificationStatus.PENDING, limit: Int = 50): List<NotificationQueueEntity>

    @Query("UPDATE notification_queue SET status = :status, workerId = :workerId WHERE id = :id")
    suspend fun updateStatusAndWorker(id: String, status: NotificationStatus, workerId: String?)

    @Query("UPDATE notification_queue SET status = :status, attempts = attempts + 1 WHERE id = :id")
    suspend fun markFailed(id: String, status: NotificationStatus = NotificationStatus.FAILED)

    @Query("UPDATE notification_queue SET status = :status, sentAt = :sentAt WHERE id = :id")
    suspend fun markSent(id: String, status: NotificationStatus = NotificationStatus.SENT, sentAt: Long = System.currentTimeMillis())

    @Query("UPDATE notification_queue SET status = :status WHERE id = :id")
    suspend fun cancel(id: String, status: NotificationStatus = NotificationStatus.CANCELLED)

    @Query(
        "SELECT * FROM notification_queue WHERE type = :type AND sourceModule = :sourceModule AND sourceId = :sourceId AND status IN (:statuses) LIMIT 1"
    )
    suspend fun findActiveBySource(
        type: com.ccxiaoji.core.database.entity.NotificationType,
        sourceModule: String,
        sourceId: String,
        statuses: List<NotificationStatus> = listOf(NotificationStatus.PENDING, NotificationStatus.PROCESSING)
    ): NotificationQueueEntity?

    @Query(
        "SELECT * FROM notification_queue WHERE type = :type AND sourceModule = :sourceModule AND sourceId = :sourceId AND status IN (:statuses)"
    )
    suspend fun findAllActiveBySource(
        type: com.ccxiaoji.core.database.entity.NotificationType,
        sourceModule: String,
        sourceId: String,
        statuses: List<NotificationStatus> = listOf(NotificationStatus.PENDING, NotificationStatus.PROCESSING)
    ): List<NotificationQueueEntity>

    @Query("UPDATE notification_queue SET scheduledAt = :scheduledAt, title = :title, message = :message WHERE id = :id")
    suspend fun updateScheduleAndContent(id: String, scheduledAt: Long, title: String, message: String)

    @Query("SELECT * FROM notification_queue ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<NotificationQueueEntity>>
}
