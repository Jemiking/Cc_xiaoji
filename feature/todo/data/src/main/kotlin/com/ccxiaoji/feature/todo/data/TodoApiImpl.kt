package com.ccxiaoji.feature.todo.data

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.api.TodoTask
import com.ccxiaoji.feature.todo.api.TaskStatistics
import com.ccxiaoji.feature.todo.api.TodoNavigator
import com.ccxiaoji.feature.todo.api.ImportTasksResult
import com.ccxiaoji.feature.todo.data.repository.TaskRepository
import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// TODO: 编译验证 - 需要执行 ./gradlew :feature:todo:compileDebugKotlin
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
    
    override suspend fun importTasks(
        tasks: List<Map<String, Any>>,
        conflictResolution: String
    ): ImportTasksResult {
        var successCount = 0
        var skippedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()
        
        tasks.forEach { taskData ->
            try {
                // 解析任务数据
                val title = taskData["title"] as? String
                if (title.isNullOrBlank()) {
                    failedCount++
                    errors.add("任务缺少标题")
                    return@forEach
                }
                
                val id = taskData["id"] as? String ?: UUID.randomUUID().toString()
                val description = taskData["description"] as? String
                val priority = (taskData["priority"] as? Number)?.toInt() ?: 1
                val completed = taskData["completed"] as? Boolean ?: false
                val dueAt = (taskData["dueAt"] as? Number)?.let { Instant.fromEpochMilliseconds(it.toLong()) }
                val completedAt = (taskData["completedAt"] as? Number)?.let { Instant.fromEpochMilliseconds(it.toLong()) }
                val createdAt = (taskData["createdAt"] as? Number)?.let { Instant.fromEpochMilliseconds(it.toLong()) } ?: Instant.fromEpochMilliseconds(System.currentTimeMillis())
                val updatedAt = (taskData["updatedAt"] as? Number)?.let { Instant.fromEpochMilliseconds(it.toLong()) } ?: createdAt
                
                // 检查任务是否已存在（通过标题判断，因为TaskRepository没有公开getTaskById方法）
                val existingTasks = taskRepository.getTasks().first()
                val existingTask = existingTasks.find { it.id == id }
                
                when (conflictResolution) {
                    "SKIP" -> {
                        if (existingTask != null) {
                            skippedCount++
                            return@forEach
                        }
                    }
                    "REPLACE" -> {
                        // 如果存在则删除旧任务
                        if (existingTask != null) {
                            taskRepository.deleteTask(existingTask.id)
                        }
                    }
                    "CREATE_NEW" -> {
                        // 总是创建新任务
                        taskRepository.addTask(
                            title = title,
                            description = description,
                            dueAt = dueAt,
                            priority = priority
                        )
                        // 如果任务已完成，需要更新完成状态
                        if (completed) {
                            // 获取刚创建的任务并更新完成状态
                            val newTasks = taskRepository.getTasks().first()
                            val newTask = newTasks.maxByOrNull { it.createdAt }
                            newTask?.let {
                                taskRepository.updateTaskCompletion(it.id, true)
                            }
                        }
                        successCount++
                        return@forEach
                    }
                }
                
                // 创建任务
                val newTask = taskRepository.addTask(
                    title = title,
                    description = description,
                    dueAt = dueAt,
                    priority = priority
                )
                
                // 如果任务已完成，需要更新完成状态
                if (completed) {
                    taskRepository.updateTaskCompletion(newTask.id, true)
                }
                
                successCount++
                
            } catch (e: Exception) {
                failedCount++
                errors.add("导入任务失败: ${e.message}")
            }
        }
        
        return ImportTasksResult(
            totalCount = tasks.size,
            successCount = successCount,
            skippedCount = skippedCount,
            failedCount = failedCount,
            errors = errors
        )
    }
}