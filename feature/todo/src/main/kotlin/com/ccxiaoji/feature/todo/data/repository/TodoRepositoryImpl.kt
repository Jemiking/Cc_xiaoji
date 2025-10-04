package com.ccxiaoji.feature.todo.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.DomainException
import com.ccxiaoji.common.base.safeSuspendCall
import com.ccxiaoji.feature.todo.data.local.dao.TaskDao
import com.ccxiaoji.feature.todo.data.local.entity.TaskEntity
import com.ccxiaoji.common.model.SyncStatus
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import com.ccxiaoji.shared.user.api.UserApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import android.util.Log
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val userApi: UserApi
) : TodoRepository {
    override fun getAllTodos(): Flow<List<Task>> {
        return taskDao.getTasksByUser(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getIncompleteTodos(): Flow<List<Task>> {
        return taskDao.getIncompleteTasks(userApi.getCurrentUserId())
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun searchTodos(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(userApi.getCurrentUserId(), "%$query%")
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTodayTodos(): Flow<List<Task>> {
        val todayStart = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            .date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val todayEnd = todayStart + 86400000 // 24 hours
        
        return taskDao.getTasksByDateRange(userApi.getCurrentUserId(), todayStart, todayEnd)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTodayTodosCount(): Flow<Int> {
        return getTodayTodos().map { it.size }
    }
    
    override suspend fun addTodo(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ): BaseResult<Task> = safeSuspendCall {
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        
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
        
        entity.toDomainModel()
    }
    
    override suspend fun updateTodoCompletion(todoId: String, completed: Boolean): BaseResult<Unit> = safeSuspendCall {
        val now = System.currentTimeMillis()
        val completedAt = if (completed) now else null
        
        taskDao.updateTaskCompletion(todoId, completed, completedAt, now)
        
        // TODO: Log the change for sync through ChangeLogApi
    }
    
    override suspend fun updateTodo(
        todoId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ): BaseResult<Unit> = safeSuspendCall {
        if (title.isBlank()) {
            throw DomainException.ValidationException("标题不能为空")
        }
        
        val existingTask = taskDao.getTaskById(todoId) 
            ?: throw DomainException.DataException("任务不存在")
        
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
    
    override suspend fun deleteTodo(todoId: String): BaseResult<Unit> = safeSuspendCall {
        val now = System.currentTimeMillis()
        
        taskDao.softDeleteTask(todoId, now)
        
        // TODO: Log the change for sync through ChangeLogApi
    }
    
    override suspend fun getTodoById(todoId: String): BaseResult<Task?> = safeSuspendCall {
        taskDao.getTaskById(todoId)?.toDomainModel()
    }

    override suspend fun updateTaskReminder(
        todoId: String,
        reminderEnabled: Boolean?,
        reminderAt: Instant?,
        reminderMinutesBefore: Int?,
        reminderTime: String?
    ): BaseResult<Unit> = safeSuspendCall {
        val task = taskDao.getTaskById(todoId)
            ?: throw DomainException.DataException("Task not found: $todoId")

        // 更新提醒字段
        val updated = task.copy(
            reminderEnabled = reminderEnabled,
            reminderAt = reminderAt?.toEpochMilliseconds(),
            reminderMinutesBefore = reminderMinutesBefore,
            reminderTime = reminderTime,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            syncStatus = SyncStatus.PENDING_SYNC
        )

        taskDao.updateTask(updated)

        Log.d("TodoRepository", "Task reminder updated: todoId=$todoId, enabled=$reminderEnabled, at=$reminderAt, minutesBefore=$reminderMinutesBefore, time=$reminderTime")
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
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),

        // ===== 新增字段映射（Phase 2 & Phase 3）=====
        reminderEnabled = reminderEnabled,
        reminderAt = reminderAt?.let { Instant.fromEpochMilliseconds(it) },
        reminderMinutesBefore = reminderMinutesBefore,
        reminderTime = reminderTime
    )
}