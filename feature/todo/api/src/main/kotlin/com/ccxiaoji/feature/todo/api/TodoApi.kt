package com.ccxiaoji.feature.todo.api

import kotlinx.coroutines.flow.Flow

/**
 * Todo模块对外暴露的API接口
 * 其他模块通过此接口访问Todo功能
 */
interface TodoApi {
    /**
     * 获取今日待办任务
     */
    suspend fun getTodayTasks(): List<TodoTask>
    
    /**
     * 获取待办任务数量统计
     */
    suspend fun getTaskStatistics(): TaskStatistics
    
    /**
     * 获取未完成任务数量
     */
    fun getUncompletedTaskCount(): Flow<Int>
    
    /**
     * 获取所有任务（用于数据导出）
     */
    suspend fun getAllTasks(): List<TodoTask>
    
    /**
     * 导航到待办列表页面
     */
    fun navigateToTodoList()
    
    /**
     * 导航到添加待办页面
     */
    fun navigateToAddTask()
}

/**
 * 待办任务数据模型（简化版）
 */
data class TodoTask(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val priority: Int,
    val dueDate: Long?
)

/**
 * 任务统计信息
 */
data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val overdueTasks: Int
)