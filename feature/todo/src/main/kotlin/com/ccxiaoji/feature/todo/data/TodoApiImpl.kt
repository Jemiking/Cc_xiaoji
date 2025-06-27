package com.ccxiaoji.feature.todo.data

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
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
    private val todoRepository: TodoRepository
) : TodoApi {
    
    override fun getTasks(): Flow<List<Task>> {
        return todoRepository.getAllTodos()
    }
    
    override fun getIncompleteTasks(): Flow<List<Task>> {
        return todoRepository.getIncompleteTodos()
    }
    
    override suspend fun getTodayTasks(): List<Task> {
        return todoRepository.getTodayTodos().first()
    }
    
    override suspend fun getTaskCount(): Int {
        return todoRepository.getAllTodos().first().size
    }
    
    override suspend fun getIncompleteTaskCount(): Int {
        return todoRepository.getIncompleteTodos().first().size
    }
    
    override suspend fun getTaskById(taskId: String): Task? {
        return todoRepository.getTodoById(taskId).getOrNull()
    }
    
    override suspend fun addTask(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ): Task {
        return todoRepository.addTodo(title, description, dueAt, priority).getOrThrow()
    }
    
    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ) {
        todoRepository.updateTodo(taskId, title, description, dueAt, priority).getOrThrow()
    }
    
    override suspend fun updateTaskCompletion(taskId: String, completed: Boolean) {
        todoRepository.updateTodoCompletion(taskId, completed).getOrThrow()
    }
    
    override suspend fun deleteTask(taskId: String) {
        todoRepository.deleteTodo(taskId).getOrThrow()
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