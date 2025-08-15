package com.ccxiaoji.feature.todo.api

import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * 待办事项模块对外API接口
 */
interface TodoApi {
    /**
     * 获取所有任务
     */
    fun getTasks(): Flow<List<Task>>
    
    /**
     * 获取未完成任务
     */
    fun getIncompleteTasks(): Flow<List<Task>>
    
    /**
     * 获取今日任务
     */
    suspend fun getTodayTasks(): List<Task>
    
    /**
     * 获取任务数量统计
     */
    suspend fun getTaskCount(): Int
    
    /**
     * 获取未完成任务数量
     */
    suspend fun getIncompleteTaskCount(): Int
    
    /**
     * 根据ID获取任务
     */
    suspend fun getTaskById(taskId: String): Task?
    
    /**
     * 添加任务
     */
    suspend fun addTask(
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    ): Task
    
    /**
     * 更新任务
     */
    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueAt: Instant?,
        priority: Int
    )
    
    /**
     * 更新任务完成状态
     */
    suspend fun updateTaskCompletion(taskId: String, completed: Boolean)
    
    /**
     * 删除任务
     */
    suspend fun deleteTask(taskId: String)
    
    /**
     * 导航到任务列表
     */
    fun navigateToTaskList()
    
    /**
     * 导航到任务详情
     */
    fun navigateToTaskDetail(taskId: String)
    
    /**
     * 导航到添加任务
     */
    fun navigateToAddTask()
}