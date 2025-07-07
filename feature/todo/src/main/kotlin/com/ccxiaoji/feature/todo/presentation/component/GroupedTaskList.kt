package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*
import java.time.LocalDate

/**
 * 分组任务列表组件
 * 按时间分组显示任务（今天、明天、本周、以后）
 */
@Composable
fun GroupedTaskList(
    tasks: List<Task>,
    onToggleComplete: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) {
        // 空状态显示
        EmptyTaskState(onAddTask = onAddTask, modifier = modifier)
    } else {
        // 对任务进行分组
        val groupedTasks = groupTasksByDate(tasks)
        
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            groupedTasks.forEach { (group, tasksInGroup) ->
                item {
                    TaskGroupHeader(
                        title = group.title,
                        taskCount = tasksInGroup.size
                    )
                }
                
                items(
                    items = tasksInGroup,
                    key = { task -> task.id }
                ) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onEdit = { onEditTask(task) },
                        onDelete = { onDeleteTask(task) },
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                }
            }
        }
    }
}

@Composable
private fun EmptyTaskState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DesignTokens.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        Text(
            text = "没有待办事项",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        Text(
            text = "点击下方按钮添加新任务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        FlatButton(
            onClick = onAddTask,
            backgroundColor = DesignTokens.BrandColors.Todo
        ) {
            Text("添加任务")
        }
    }
}

private fun groupTasksByDate(tasks: List<Task>): List<Pair<TaskGroup, List<Task>>> {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    val weekEnd = today.plus(7, DateTimeUnit.DAY)
    
    val grouped = tasks.groupBy { task ->
        when {
            task.dueAt == null -> TaskGroup.NO_DUE_DATE
            else -> {
                val taskDate = task.dueAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                when {
                    taskDate == today -> TaskGroup.TODAY
                    taskDate == tomorrow -> TaskGroup.TOMORROW
                    taskDate <= weekEnd -> TaskGroup.THIS_WEEK
                    else -> TaskGroup.LATER
                }
            }
        }
    }
    
    // 按顺序返回分组
    return listOf(
        TaskGroup.TODAY,
        TaskGroup.TOMORROW,
        TaskGroup.THIS_WEEK,
        TaskGroup.LATER,
        TaskGroup.NO_DUE_DATE
    ).mapNotNull { group ->
        grouped[group]?.let { group to it }
    }
}

private enum class TaskGroup(val title: String) {
    TODAY("今天"),
    TOMORROW("明天"),
    THIS_WEEK("本周"),
    LATER("以后"),
    NO_DUE_DATE("无截止日期")
}