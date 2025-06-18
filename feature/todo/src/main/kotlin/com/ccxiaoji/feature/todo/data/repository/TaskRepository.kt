package com.ccxiaoji.feature.todo.data.repository

import com.ccxiaoji.feature.todo.data.local.dao.TaskDao
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val userApi: UserApi
) {
    fun getTasks(): Flow<List<Task>> {
        return taskDao.getTasksByUser(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getIncompleteTasks(): Flow<List<Task>> {
        return taskDao.getIncompleteTasks(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(userApi.getCurrentUserId(), "%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    fun getTodayTasks(): Flow<List<Task>> {
        val todayStart = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val todayEnd = todayStart + 86400000 // 24 hours
        
        return taskDao.getTasksByDateRange(userApi.getCurrentUserId(), todayStart, todayEnd)
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
            userId = userApi.getCurrentUserId(),
            title = title,
            description = description,
            dueAt = dueAt?.toEpochMilliseconds(),
            priority = priority,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        
        taskDao.insertTask(entity)
        
        // TODO: Log the change for sync through ChangeLogApi
        
        return entity.toDomainModel()
    }
    
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean) {
        val now = System.currentTimeMillis()
        val completedAt = if (completed) now else null
        
        taskDao.updateTaskCompletion(taskId, completed, completedAt, now)
        
        // TODO: Log the change for sync through ChangeLogApi
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
        
        // TODO: Log the change for sync through ChangeLogApi
    }
    
    suspend fun deleteTask(taskId: String) {
        val now = System.currentTimeMillis()
        
        taskDao.softDeleteTask(taskId, now)
        
        // TODO: Log the change for sync through ChangeLogApi
    }
    
    suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomainModel()
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