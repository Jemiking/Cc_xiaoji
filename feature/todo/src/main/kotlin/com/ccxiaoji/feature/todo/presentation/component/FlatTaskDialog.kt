package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.todo.R
import com.ccxiaoji.feature.todo.domain.model.Task
import com.ccxiaoji.feature.todo.domain.model.Priority
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 扁平化设计的任务对话框
 * 用于添加或编辑任务
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, dueAt: Instant?, priorityLevel: Priority) -> Unit,
    task: Task? = null
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var priorityLevel by remember { mutableStateOf(task?.priorityLevel ?: Priority.MEDIUM) }
    var dueAt by remember { mutableStateOf(task?.dueAt) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.large)
            ) {
                // 标题
                Text(
                    text = if (task == null) "添加任务" else "编辑任务",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Todo
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 任务标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Todo,
                        focusedLabelColor = DesignTokens.BrandColors.Todo
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 任务描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("任务描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesignTokens.BrandColors.Todo,
                        focusedLabelColor = DesignTokens.BrandColors.Todo
                    )
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 优先级选择
                Column {
                    Text(
                        text = "优先级",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    FlatPrioritySelector(
                        selectedPriority = priorityLevel,
                        onPriorityChange = { priorityLevel = it }
                    )
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                // 截止日期选择
                Column {
                    Text(
                        text = "到期日期",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        // 日期选择按钮
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DesignTokens.BrandColors.Todo
                            ),
                            border = BorderStroke(
                                1.dp,
                                DesignTokens.BrandColors.Todo.copy(alpha = 0.2f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                            Text(
                                text = dueAt?.let {
                                    it.toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date.toString()
                                } ?: "选择日期"
                            )
                        }
                        
                        // 清除按钮
                        if (dueAt != null) {
                            TextButton(
                                onClick = { dueAt = null },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("清除")
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    // 取消按钮
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("取消")
                    }
                    
                    // 确认按钮
                    FlatButton(
                        onClick = {
                            onConfirm(title, description.ifEmpty { null }, dueAt, priorityLevel)
                        },
                        enabled = title.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        backgroundColor = DesignTokens.BrandColors.Todo
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDateMillis ->
                selectedDateMillis?.let {
                    dueAt = Instant.fromEpochMilliseconds(it)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 扁平化优先级选择器
 */
@Composable
fun FlatPrioritySelector(
    selectedPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        Priority.values().forEach { priority ->
            val isSelected = selectedPriority == priority
            val backgroundColor = when (priority) {
                Priority.HIGH -> DesignTokens.BrandColors.Error
                Priority.MEDIUM -> DesignTokens.BrandColors.Warning
                Priority.LOW -> DesignTokens.BrandColors.Success
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
                    .background(
                        if (isSelected) backgroundColor.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                    )
                    .clickable { onPriorityChange(priority) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priority.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) backgroundColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
