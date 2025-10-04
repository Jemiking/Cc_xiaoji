package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.TimePickerDialog
import java.time.LocalTime

/**
 * 任务提醒设置组件（Phase 3 - 混合模式）
 *
 * 提供三种提醒模式：
 * 1. 使用全局配置（默认）- reminderEnabled = null
 * 2. 相对时间提醒（提前N分钟）- reminderMinutesBefore != null
 * 3. 固定时间提醒（每天HH:mm）- reminderAt != null
 */
@Composable
fun TaskReminderSettingSection(
    reminderEnabled: Boolean?,
    reminderMinutesBefore: Int?,
    reminderTime: String?,  // 固定时间字符串 "HH:mm"，null表示使用相对时间
    onReminderEnabledChange: (Boolean?) -> Unit,
    onReminderMinutesChange: (Int?) -> Unit,
    onReminderTimeChange: (String?) -> Unit,  // 固定时间变更回调
    modifier: Modifier = Modifier
) {
    var showReminderModeDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = "提醒设置",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(DesignTokens.Spacing.medium)) {
                // 选项1：使用全局配置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderEnabled == null && reminderMinutesBefore == null,
                        onClick = {
                            onReminderEnabledChange(null)
                            onReminderMinutesChange(null)
                        }
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Text(
                        text = "使用全局配置",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 选项2：自定义提醒
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderMinutesBefore != null || reminderEnabled == true,
                        onClick = {
                            showReminderModeDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Column {
                        Text(
                            text = "自定义提醒",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // 显示当前配置
                        when {
                            reminderTime != null -> {
                                Text(
                                    text = "每天 $reminderTime 提醒",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            reminderMinutesBefore != null -> {
                                val displayText = when {
                                    reminderMinutesBefore >= 1440 -> "提前 ${reminderMinutesBefore / 1440} 天"
                                    reminderMinutesBefore >= 60 -> "提前 ${reminderMinutesBefore / 60} 小时"
                                    else -> "提前 $reminderMinutesBefore 分钟"
                                }
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 选项3：关闭提醒
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderEnabled == false,
                        onClick = {
                            onReminderEnabledChange(false)
                            onReminderMinutesChange(null)
                        }
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Text(
                        text = "关闭提醒",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // 提醒模式选择对话框
    if (showReminderModeDialog) {
        ReminderModeSelectionDialog(
            currentMinutes = reminderMinutesBefore,
            currentTime = reminderTime,
            onSelectRelativeTime = { minutes ->
                onReminderEnabledChange(true)
                onReminderMinutesChange(minutes)
                onReminderTimeChange(null)  // 清除固定时间
                showReminderModeDialog = false
            },
            onSelectFixedTime = { timeString ->
                onReminderEnabledChange(true)
                onReminderMinutesChange(null)  // 清除相对时间
                onReminderTimeChange(timeString)  // 设置固定时间
                showReminderModeDialog = false
            },
            onDismiss = {
                showReminderModeDialog = false
            }
        )
    }
}

/**
 * 提醒模式选择对话框
 * 支持两种模式：相对时间 和 固定时间
 */
@Composable
private fun ReminderModeSelectionDialog(
    currentMinutes: Int?,
    currentTime: String?,
    onSelectRelativeTime: (Int) -> Unit,
    onSelectFixedTime: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMode by remember { mutableStateOf("relative") } // "relative" 或 "fixed"
    var showRelativeTimeDialog by remember { mutableStateOf(false) }
    var showFixedTimeDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择提醒方式") },
        text = {
            Column {
                Text("请选择提醒模式：")
                Spacer(modifier = Modifier.height(16.dp))

                // 模式1：相对时间（提前N分钟）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == "relative",
                        onClick = { selectedMode = "relative" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "提前N分钟提醒",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "例如：提前30分钟提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 模式2：固定时间
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMode == "fixed",
                        onClick = { selectedMode = "fixed" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "固定时间提醒",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "例如：每天早上8:00提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    when (selectedMode) {
                        "relative" -> showRelativeTimeDialog = true
                        "fixed" -> showFixedTimeDialog = true
                    }
                    onDismiss()
                }
            ) {
                Text("下一步")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 相对时间选择对话框
    if (showRelativeTimeDialog) {
        RelativeTimeSelectionDialog(
            currentMinutes = currentMinutes ?: 30,
            onConfirm = { minutes ->
                onSelectRelativeTime(minutes)
                showRelativeTimeDialog = false
            },
            onDismiss = {
                showRelativeTimeDialog = false
            }
        )
    }

    // 固定时间选择对话框
    if (showFixedTimeDialog) {
        FixedTimeSelectionDialog(
            currentTime = currentTime ?: "08:00",
            onConfirm = { timeString ->
                onSelectFixedTime(timeString)
                showFixedTimeDialog = false
            },
            onDismiss = {
                showFixedTimeDialog = false
            }
        )
    }
}

/**
 * 相对时间选择对话框（提前N分钟）
 */
@Composable
private fun RelativeTimeSelectionDialog(
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableStateOf(currentMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置提前提醒") },
        text = {
            Column {
                Text("选择提前提醒的时间：")
                Spacer(modifier = Modifier.height(16.dp))

                // 常用选项
                val commonOptions = listOf(
                    5 to "提前 5 分钟",
                    10 to "提前 10 分钟",
                    15 to "提前 15 分钟",
                    30 to "提前 30 分钟",
                    60 to "提前 1 小时",
                    120 to "提前 2 小时",
                    1440 to "提前 1 天"
                )

                commonOptions.forEach { (minutes, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMinutes == minutes,
                            onClick = { selectedMinutes = minutes }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMinutes) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 固定时间选择对话框（每天HH:mm）
 * 参考习惯模块的实现
 */
@Composable
private fun FixedTimeSelectionDialog(
    currentTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 解析当前时间字符串为LocalTime
    val initialTime = try {
        val parts = currentTime.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) {
        LocalTime.of(8, 0) // 默认早上8点
    }

    // 使用通用TimePickerDialog组件（与习惯模块一致）
    TimePickerDialog(
        showDialog = true,
        title = "设置提醒时间",
        initialTime = initialTime,
        onTimeSelected = { selectedTime ->
            // 将LocalTime转换为HH:mm格式字符串
            val timeString = String.format("%02d:%02d", selectedTime.hour, selectedTime.minute)
            onConfirm(timeString)
        },
        onDismiss = onDismiss,
        is24Hour = true
    )
}
