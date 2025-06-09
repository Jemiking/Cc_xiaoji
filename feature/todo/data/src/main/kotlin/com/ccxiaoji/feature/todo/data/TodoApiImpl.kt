package com.ccxiaoji.feature.todo.data

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.api.TodoTask
import com.ccxiaoji.feature.todo.api.TaskStatistics
import com.ccxiaoji.feature.todo.api.TodoNavigator
import com.ccxiaoji.feature.todo.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoApiImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val todoNavigator: TodoNavigator
) : TodoApi {
    
    override suspend fun getTodayTasks(): List<TodoTask> {
        return taskRepository.getTodayTasks().first().map { task ->
            TodoTask(
                id = task.id,
                title = task.title,
                isCompleted = task.completed,
                priority = task.priority,
                dueDate = task.dueAt?.toEpochMilliseconds()
            )
        }
    }
    
    override suspend fun getTaskStatistics(): TaskStatistics {
        val allTasks = taskRepository.getTasks().first()
        val totalTasks = allTasks.size
        val completedTasks = allTasks.count { it.completed }
        val pendingTasks = allTasks.count { !it.completed }
        val overdueTasks = allTasks.count { task ->
            !task.completed && task.dueAt != null && task.dueAt.toEpochMilliseconds() < System.currentTimeMillis()
        }
        
        return TaskStatistics(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            overdueTasks = overdueTasks
        )
    }
    
    override fun getUncompletedTaskCount(): Flow<Int> {
        return taskRepository.getIncompleteTasks().map { it.size }
    }
    
    override suspend fun getAllTasks(): List<TodoTask> {
        return taskRepository.getTasks().first().map { task ->
            TodoTask(
                id = task.id,
                title = task.title,
                isCompleted = task.completed,
                priority = task.priority,
                dueDate = task.dueAt?.toEpochMilliseconds()
            )
        }
    }
    
    override fun navigateToTodoList() {
        todoNavigator.navigateToTodoList()
    }
    
    override fun navigateToAddTask() {
        todoNavigator.navigateToAddTask()
    }
}