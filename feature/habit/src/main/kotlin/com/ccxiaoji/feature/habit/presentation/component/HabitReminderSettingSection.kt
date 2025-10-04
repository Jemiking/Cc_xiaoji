package com.ccxiaoji.feature.habit.presentation.component

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
 * 习惯提醒设置组件（Phase 3）
 *
 * 提供两种提醒模式：
 * 1. 使用全局配置（默认）- reminderEnabled = null, reminderTime = null
 * 2. 自定义提醒时间 - reminderEnabled = true, reminderTime = "HH:mm"
 */
@Composable
fun HabitReminderSettingSection(
    reminderEnabled: Boolean?,
    reminderTime: String?,
    onReminderEnabledChange: (Boolean?) -> Unit,
    onReminderTimeChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomTimeDialog by remember { mutableStateOf(false) }

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
                        selected = reminderEnabled == null && reminderTime == null,
                        onClick = {
                            onReminderEnabledChange(null)
                            onReminderTimeChange(null)
                        }
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Text(
                        text = "使用全局配置",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 选项2：自定义提醒时间
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = reminderTime != null,
                        onClick = {
                            showCustomTimeDialog = true
                        }
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                    Column {
                        Text(
                            text = "自定义提醒时间",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (reminderTime != null) {
                            Text(
                                text = "每天 $reminderTime 提醒",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                            onReminderTimeChange(null)
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

    // 自定义时间选择对话框
    if (showCustomTimeDialog) {
        CustomTimeDialog(
            currentTime = reminderTime ?: "20:00",
            onConfirm = { time ->
                onReminderEnabledChange(true)
                onReminderTimeChange(time)
                showCustomTimeDialog = false
            },
            onDismiss = {
                showCustomTimeDialog = false
            }
        )
    }
}

@Composable
private fun CustomTimeDialog(
    currentTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // 解析当前时间字符串为LocalTime
    val initialTime = try {
        val parts = currentTime.split(":")
        LocalTime.of(parts[0].toInt(), parts[1].toInt())
    } catch (e: Exception) {
        LocalTime.of(20, 0) // 默认晚上8点
    }

    // 使用通用TimePickerDialog组件
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
