package com.ccxiaoji.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalTime

/**
 * 通用时间选择器对话框（Material You 风格）
 *
 * @param showDialog 是否显示对话框
 * @param title 对话框标题，默认为"选择时间"
 * @param initialTime 初始时间
 * @param onTimeSelected 时间选择回调
 * @param onDismiss 关闭对话框回调
 * @param is24Hour 是否使用24小时制，默认为true
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String = "选择时间",
    is24Hour: Boolean = true
) {
    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24Hour
        )

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            // Material You 风格卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )

                    // 当前选择的时间显示
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 小时显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%02d", timePickerState.hour),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "时",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // 分钟显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%02d", timePickerState.minute),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "分",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Material3 时间选择器
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = MaterialTheme.colorScheme.surface,
                                    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    selectorColor = MaterialTheme.colorScheme.primary,
                                    containerColor = Color.Transparent,
                                    periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    periodSelectorUnselectedContainerColor = Color.Transparent,
                                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // 操作按钮区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 当前时间按钮
                        FilledTonalButton(
                            onClick = {
                                val now = LocalTime.now()
                                onTimeSelected(now)
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("当前时间")
                        }

                        // 操作按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 取消按钮
                            TextButton(
                                onClick = onDismiss,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("取消")
                            }

                            // 确定按钮
                            Button(
                                onClick = {
                                    onTimeSelected(
                                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    )
                                    onDismiss()
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("确定")
                            }
                        }
                    }
                }
            }
        }
    }
}
