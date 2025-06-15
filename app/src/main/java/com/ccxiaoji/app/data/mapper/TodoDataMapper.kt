package com.ccxiaoji.app.data.mapper

import com.ccxiaoji.app.domain.usecase.excel.TaskData
import com.ccxiaoji.feature.todo.api.TodoTask
import javax.inject.Inject

/**
 * 待办数据映射器
 * 负责处理待办模块相关的数据转换
 * 
 * 主要职责：
 * 1. API数据类型 → 业务数据类型
 * 2. 缺失字段的默认值处理
 * 3. 优先级映射
 */
class TodoDataMapper @Inject constructor() {
    
    /**
     * 将待办任务转换为Excel导出数据
     * @param task API待办任务
     * @return Excel导出用的任务数据
     */
    fun mapTodoTaskToExportData(task: TodoTask): TaskData {
        return TaskData(
            title = task.title,
            description = null, // TodoTask没有description字段
            priority = task.priority,
            completed = task.isCompleted,
            dueAt = task.dueDate,
            createdAt = System.currentTimeMillis() // TodoTask没有createdAt字段，使用当前时间
        )
    }
    
    /**
     * 批量转换待办任务
     * @param tasks API待办任务列表
     * @return Excel导出用的任务数据列表
     */
    fun mapTodoTaskListToExportData(tasks: List<TodoTask>): List<TaskData> {
        return tasks.map { mapTodoTaskToExportData(it) }
    }
    
    /**
     * 获取优先级的中文描述
     * @param priority 优先级数值
     * @return 中文描述
     */
    fun getPriorityText(priority: Int): String {
        return when (priority) {
            3 -> "高"
            2 -> "中"
            1 -> "低"
            else -> "普通"
        }
    }
    
    /**
     * 获取任务状态的中文描述
     * @param completed 是否已完成
     * @return 中文描述
     */
    fun getStatusText(completed: Boolean): String {
        return if (completed) "已完成" else "待完成"
    }
    
    /**
     * 计算任务的剩余天数
     * @param dueDate 截止日期（时间戳）
     * @return 剩余天数，过期返回负数，无截止日期返回null
     */
    fun calculateRemainingDays(dueDate: Long?): Int? {
        if (dueDate == null) return null
        
        val now = System.currentTimeMillis()
        val diff = dueDate - now
        val days = diff / (24 * 60 * 60 * 1000)
        
        return days.toInt()
    }
    
    /**
     * 获取剩余时间的描述
     * @param dueDate 截止日期（时间戳）
     * @return 描述文本
     */
    fun getRemainingTimeText(dueDate: Long?): String {
        val days = calculateRemainingDays(dueDate) ?: return "无截止日期"
        
        return when {
            days < 0 -> "已过期${-days}天"
            days == 0 -> "今天到期"
            days == 1 -> "明天到期"
            days <= 7 -> "${days}天后到期"
            days <= 30 -> "${days / 7}周后到期"
            else -> "${days / 30}个月后到期"
        }
    }
}