package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.domain.model.Task
import kotlinx.datetime.Instant

/**
 * 添加/编辑任务对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, dueAt: Instant?, priority: Int) -> Unit,
    task: Task? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: 0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) stringResource(R.string.todo_add_task) else stringResource(R.string.todo_edit_task)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 任务标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.todo_task_title_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 任务描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.todo_task_description_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // 优先级选择
                Column {
                    Text(
                        text = stringResource(R.string.todo_priority_label),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    PrioritySelector(
                        selectedPriority = priority,
                        onPriorityChange = { priority = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(title, description.ifEmpty { null }, null, priority)
                },
                enabled = title.isNotEmpty()
            ) {
                Text(stringResource(R.string.todo_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.todo_cancel))
            }
        }
    )
}

/**
 * 优先级选择器组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySelector(
    selectedPriority: Int,
    onPriorityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            0 to stringResource(R.string.todo_priority_low),
            1 to stringResource(R.string.todo_priority_medium), 
            2 to stringResource(R.string.todo_priority_high)
        ).forEach { (value, label) ->
            FilterChip(
                selected = selectedPriority == value,
                onClick = { onPriorityChange(value) },
                label = { Text(label) }
            )
        }
    }
}