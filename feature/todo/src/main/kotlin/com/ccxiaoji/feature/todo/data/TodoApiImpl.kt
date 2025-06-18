package com.ccxiaoji.feature.todo.data

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.data.repository.TaskRepository
import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TodoApi的实现类
 */
@Singleton
class TodoApiImpl @Inject constructor(
    private val taskRepository: TaskRepository
) : TodoApi {
    
    override fun getTasks(): Flow<List<Task>> {
        return taskRepository.getTasks()
    }
    
    override fun getIncompleteTasks(): Flow<List<Task>> {
        return taskRepository.getIncompleteTasks()
    }
    
    override suspend fun getTodayTasks(): List<Task> {
        return taskRepository.getTodayTasks().first()
    }
    
    override suspend fun getTaskCount(): Int {
        return taskRepository.getTasks().first().size
    }
    
    override suspend fun getIncompleteTaskCount(): Int {
        return taskRepository.getIncompleteTasks().first().size
    }
    
    override suspend fun getTaskById(taskId: String): Task? {
        return taskRepository.getTaskById(taskId)
    }
    
    override suspend fun addTask(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ): Task {
        return taskRepository.addTask(title, description, dueAt, priority)
    }
    
    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        taskRepository.updateTask(taskId, title, description, dueAt, priority)
    }
    
    override suspend fun updateTaskCompletion(taskId: String, completed: Boolean) {
        taskRepository.updateTaskCompletion(taskId, completed)
    }
    
    override suspend fun deleteTask(taskId: String) {
        taskRepository.deleteTask(taskId)
    }
    
    // 导航功能需要在app模块中实现
    override fun navigateToTaskList() {
        throw UnsupportedOperationException("Navigation should be handled by app module")
    }
    
    override fun navigateToTaskDetail(taskId: String) {
        throw UnsupportedOperationException("Navigation should be handled by app module")
    }
    
    override fun navigateToAddTask() {
        throw UnsupportedOperationException("Navigation should be handled by app module")
    }
}