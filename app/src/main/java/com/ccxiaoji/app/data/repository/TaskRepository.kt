package com.ccxiaoji.app.data.repository

import com.ccxiaoji.app.data.local.dao.TaskDao
import com.ccxiaoji.app.data.local.dao.ChangeLogDao
import com.ccxiaoji.app.data.local.entity.TaskEntity
import com.ccxiaoji.app.data.sync.SyncStatus
import com.ccxiaoji.app.data.local.entity.ChangeLogEntity
import com.ccxiaoji.app.domain.model.Task
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val changeLogDao: ChangeLogDao,
    private val gson: Gson
) {
    fun getTasks(): Flow<List<Task>> {
        return taskDao.getTasksByUser(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getIncompleteTasks(): Flow<List<Task>> {
        return taskDao.getIncompleteTasks(getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(getCurrentUserId(), "%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getTodayTasks(): Flow<List<Task>> {
        val todayStart = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val todayEnd = todayStart + 86400000 // 24 hours
        
        return taskDao.getTasksByDateRange(getCurrentUserId(), todayStart, todayEnd)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getTodayTasksCount(): Flow<Int> {
        return getTodayTasks().map { it.size }
    }
    
    suspend fun addTask(
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ): Task {
        val taskId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        val entity = TaskEntity(
            id = taskId,
            userId = getCurrentUserId(),
            title = title,
            description = description,
            dueAt = dueAt?.toEpochMilliseconds(),
            priority = priority,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        taskDao.insertTask(entity)
        
        // Log the change for sync
        logChange("tasks", taskId, "INSERT", entity)
        
        return entity.toDomainModel()
    }
    
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean) {
        val now = System.currentTimeMillis()
        val completedAt = if (completed) now else null
        
        taskDao.updateTaskCompletion(taskId, completed, completedAt, now)
        
        // Log the change for sync
        logChange("tasks", taskId, "UPDATE", mapOf(
            "id" to taskId,
            "completed" to completed,
            "completedAt" to completedAt
        ))
    }
    
    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String? = null,
        dueAt: Instant? = null,
        priority: Int = 0
    ) {
        val existingTask = taskDao.getTaskById(taskId) ?: return
        
        val now = System.currentTimeMillis()
        val updatedTask = existingTask.copy(
            title = title,
            description = description,
            dueAt = dueAt?.toEpochMilliseconds(),
            priority = priority,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        taskDao.updateTask(updatedTask)
        
        // Log the change for sync
        logChange("tasks", taskId, "UPDATE", mapOf(
            "id" to taskId,
            "title" to title,
            "description" to description,
            "dueAt" to dueAt?.toEpochMilliseconds(),
            "priority" to priority
        ))
    }
    
    suspend fun deleteTask(taskId: String) {
        val now = System.currentTimeMillis()
        
        taskDao.softDeleteTask(taskId, now)
        
        // Log the change for sync
        logChange("tasks", taskId, "DELETE", mapOf("id" to taskId))
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

private fun TaskEntity.toDomainModel(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        dueAt = dueAt?.let { Instant.fromEpochMilliseconds(it) },
        priority = priority,
        completed = completed,
        completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}