package com.ccxiaoji.feature.todo.domain.usecase

import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.presentation.viewmodel.DateFilter
import com.ccxiaoji.feature.todo.presentation.viewmodel.TaskFilterOptions
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 过滤待办事项用例
 * 处理复杂的任务过滤逻辑
 */
class FilterTodosUseCase @Inject constructor() {
    
    /**
     * 根据过滤条件过滤任务列表
     * @param tasks 原始任务列表
     * @param query 搜索关键词
     * @param filterOptions 过滤选项
     * @return 过滤后的任务列表
     */
    operator fun invoke(
        tasks: List<Task>,
        query: String,
        filterOptions: TaskFilterOptions
    ): List<Task> {
        var filteredTasks = tasks
        
        // 应用搜索过滤
        if (query.isNotBlank()) {
            filteredTasks = filteredTasks.filter { task ->
                task.title.contains(query, ignoreCase = true) || 
                task.description?.contains(query, ignoreCase = true) == true
            }
        }
        
        // 应用优先级过滤
        filteredTasks = filteredTasks.filter { task ->
            task.priority in filterOptions.selectedPriorities
        }
        
        // 应用日期过滤
        filteredTasks = when (filterOptions.dateFilter) {
            DateFilter.TODAY -> {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                filteredTasks.filter { task ->
                    task.dueAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date == today
                }
            }
            DateFilter.THIS_WEEK -> {
                val now = Clock.System.now()
                val weekStart = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    .let { it.minus(it.dayOfWeek.ordinal, DateTimeUnit.DAY) }
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                val weekEnd = weekStart.plus(DateTimePeriod(days = 7), TimeZone.currentSystemDefault())
                filteredTasks.filter { task ->
                    task.dueAt?.let { it >= weekStart && it < weekEnd } ?: false
                }
            }
            DateFilter.OVERDUE -> {
                val now = Clock.System.now()
                filteredTasks.filter { task ->
                    task.dueAt?.let { it < now && !task.completed } ?: false
                }
            }
            DateFilter.ALL -> filteredTasks
        }
        
        return filteredTasks
    }
}