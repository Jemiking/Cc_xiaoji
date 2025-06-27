package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.presentation.viewmodel.TaskFilterOptions
import com.ccxiaoji.feature.todo.presentation.viewmodel.DateFilter

/**
 * 任务过滤器栏组件
 * 包含显示已完成、日期过滤、优先级过滤等选项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoFilterBar(
    filterOptions: TaskFilterOptions,
    onFilterOptionsChange: (TaskFilterOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 显示已完成切换
        item {
            FilterChip(
                selected = filterOptions.showCompleted,
                onClick = {
                    onFilterOptionsChange(
                        filterOptions.copy(showCompleted = !filterOptions.showCompleted)
                    )
                },
                label = { Text(stringResource(R.string.todo_show_completed)) }
            )
        }
        
        // 日期过滤器
        item {
            DateFilterChips(
                selectedFilter = filterOptions.dateFilter,
                onFilterChange = { newFilter ->
                    onFilterOptionsChange(
                        filterOptions.copy(dateFilter = newFilter)
                    )
                }
            )
        }
        
        // 优先级过滤器
        item {
            PriorityFilterChips(
                selectedPriorities = filterOptions.selectedPriorities,
                onPrioritiesChange = { priorities ->
                    onFilterOptionsChange(
                        filterOptions.copy(selectedPriorities = priorities)
                    )
                }
            )
        }
    }
}

/**
 * 日期过滤器芯片组
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterChips(
    selectedFilter: DateFilter,
    onFilterChange: (DateFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // 今天
        FilterChip(
            selected = selectedFilter == DateFilter.TODAY,
            onClick = {
                onFilterChange(
                    if (selectedFilter == DateFilter.TODAY) DateFilter.ALL 
                    else DateFilter.TODAY
                )
            },
            label = { Text(stringResource(R.string.todo_filter_today)) }
        )
        
        // 本周
        FilterChip(
            selected = selectedFilter == DateFilter.THIS_WEEK,
            onClick = {
                onFilterChange(
                    if (selectedFilter == DateFilter.THIS_WEEK) DateFilter.ALL 
                    else DateFilter.THIS_WEEK
                )
            },
            label = { Text(stringResource(R.string.todo_filter_this_week)) }
        )
        
        // 逾期
        FilterChip(
            selected = selectedFilter == DateFilter.OVERDUE,
            onClick = {
                onFilterChange(
                    if (selectedFilter == DateFilter.OVERDUE) DateFilter.ALL 
                    else DateFilter.OVERDUE
                )
            },
            label = { Text(stringResource(R.string.todo_filter_overdue)) }
        )
    }
}

/**
 * 优先级过滤器芯片组
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityFilterChips(
    selectedPriorities: Set<Int>,
    onPrioritiesChange: (Set<Int>) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            0 to stringResource(R.string.todo_priority_low_short),
            1 to stringResource(R.string.todo_priority_medium_short),
            2 to stringResource(R.string.todo_priority_high_short)
        ).forEach { (priority, label) ->
            FilterChip(
                selected = selectedPriorities.contains(priority),
                onClick = {
                    val newPriorities = if (selectedPriorities.contains(priority)) {
                        selectedPriorities - priority
                    } else {
                        selectedPriorities + priority
                    }
                    onPrioritiesChange(newPriorities)
                },
                label = { Text(label) }
            )
        }
    }
}