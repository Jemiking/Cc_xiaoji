package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.CountdownDao
import com.ccxiaoji.shared.sync.data.local.dao.ChangeLogDao
import com.ccxiaoji.app.data.local.entity.CountdownEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.shared.sync.data.local.entity.ChangeLogEntity
import com.ccxiaoji.app.domain.model.Countdown
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountdownRepository @Inject constructor(
    private val countdownDao: CountdownDao,
    private val changeLogDao: ChangeLogDao,
    private val gson: Gson
) {
    fun getCountdowns(): Flow<List<Countdown>> {
        return countdownDao.getCountdownsByUser(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getUpcomingCountdowns(limit: Int = 5): Flow<List<Countdown>> {
        val currentDate = Clock.System.now().toEpochMilliseconds()
        
        return countdownDao.getUpcomingCountdowns(getCurrentUserId(), currentDate)
            .map { entities -> 
                entities.take(limit).map { it.toDomainModel() }
            }
    }
    
    fun getWidgetCountdowns(): Flow<List<Countdown>> {
        return countdownDao.getWidgetCountdowns(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    suspend fun createCountdown(
        title: String,
        targetDate: LocalDate,
        emoji: String? = "📅",
        color: String = "#3A7AFE"
    ): Countdown {
        val countdownId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = CountdownEntity(
            id = countdownId,
            userId = getCurrentUserId(),
            title = title,
            targetDate = targetDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            emoji = emoji,
            color = color,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        countdownDao.insertCountdown(entity)
        
        // Log the change for sync
        logChange("countdowns", countdownId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    suspend fun updateWidgetVisibility(countdownId: String, showOnWidget: Boolean) {
        val now = System.currentTimeMillis()
        
        countdownDao.updateWidgetVisibility(countdownId, showOnWidget, now)
        
        // Log the change for sync
        logChange("countdowns", countdownId, "UPDATE", mapOf(
            "id" to countdownId,
            "showOnWidget" to showOnWidget
        ))
    }
    
    suspend fun deleteCountdown(countdownId: String) {
        val now = System.currentTimeMillis()
        
        countdownDao.softDeleteCountdown(countdownId, now)
        
        // Log the change for sync
        logChange("countdowns", countdownId, "DELETE", mapOf("id" to countdownId))
    }
    
    private suspend fun logChange(table: String, rowId: String, operation: String, payload: Any) {
        val changeLog = ChangeLogEntity(
            tableName = table,
            rowId = rowId,
            operation = operation,
            payload = gson.toJson(payload),
            timestamp = System.currentTimeMillis()
        )
        changeLogDao.insertChange(changeLog)
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get the actual current user ID
        return "current_user_id"
    }
}

private fun CountdownEntity.toDomainModel(): Countdown {
    val targetInstant = Instant.fromEpochMilliseconds(targetDate)
    val targetLocalDate = targetInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    return Countdown(
        id = id,
        title = title,
        targetDate = targetLocalDate,
        emoji = emoji,
        color = color,
        showOnWidget = showOnWidget,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}