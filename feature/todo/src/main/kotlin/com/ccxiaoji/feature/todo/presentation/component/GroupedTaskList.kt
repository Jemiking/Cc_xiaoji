package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*

/**
 * 分组待办列表：按日期分组展示任务，空列表展示统一空态组件
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
        EmptyTaskState(onAddTask = onAddTask, modifier = modifier)
    } else {
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

                item { Spacer(modifier = Modifier.height(DesignTokens.Spacing.small)) }
            }
        }
    }
}

@Composable
private fun EmptyTaskState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyStateView(
        title = "暂无待办",
        message = "点击右下角加号开始添加你的第一个任务",
        actionText = "新建任务",
        onAction = onAddTask,
        modifier = modifier.padding(DesignTokens.Spacing.xxl)
    )
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

    return listOf(
        TaskGroup.TODAY,
        TaskGroup.TOMORROW,
        TaskGroup.THIS_WEEK,
        TaskGroup.LATER,
        TaskGroup.NO_DUE_DATE
    ).mapNotNull { group -> grouped[group]?.let { group to it } }
}

private enum class TaskGroup(val title: String) {
    TODAY("今天"),
    TOMORROW("明天"),
    THIS_WEEK("本周"),
    LATER("以后"),
    NO_DUE_DATE("无截止日期")
}

