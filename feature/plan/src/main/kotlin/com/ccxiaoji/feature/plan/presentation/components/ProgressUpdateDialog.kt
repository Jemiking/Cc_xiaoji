package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt

/**
 * 进度更新对话框
 * 
 * @param currentProgress 当前进度值（0-100）
 * @param hasChildren 是否有子计划
 * @param onProgressUpdate 进度更新回调
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun ProgressUpdateDialog(
    currentProgress: Int,
    hasChildren: Boolean,
    onProgressUpdate: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var progress by remember { mutableStateOf(currentProgress.toFloat()) }
    var textProgress by remember { mutableStateOf(currentProgress.toString()) }
    var isTextFieldError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Text(
                    text = "更新进度",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (hasChildren) {
                    // 有子计划时显示提示信息
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "此计划包含子计划，进度将根据子计划自动计算。\n请更新子计划的进度。",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 显示当前进度（只读）
                    Text(
                        text = "当前进度：$currentProgress%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    // 无子计划时允许手动更新
                    
                    // 进度滑动条
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "拖动滑块调整进度",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Slider(
                            value = progress,
                            onValueChange = { newValue ->
                                progress = newValue
                                textProgress = newValue.roundToInt().toString()
                                isTextFieldError = false
                            },
                            valueRange = 0f..100f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 进度标签
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${progress.roundToInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "100%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 手动输入进度
                    OutlinedTextField(
                        value = textProgress,
                        onValueChange = { value ->
                            textProgress = value
                            val intValue = value.toIntOrNull()
                            if (intValue != null && intValue in 0..100) {
                                progress = intValue.toFloat()
                                isTextFieldError = false
                            } else {
                                isTextFieldError = true
                            }
                        },
                        label = { Text("或直接输入进度值") },
                        suffix = { Text("%") },
                        isError = isTextFieldError,
                        supportingText = {
                            if (isTextFieldError) {
                                Text(
                                    text = "请输入0-100之间的数字",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    if (!hasChildren) {
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                val finalProgress = progress.roundToInt()
                                onProgressUpdate(finalProgress)
                                onDismiss()
                            },
                            enabled = !isTextFieldError
                        ) {
                            Text("更新")
                        }
                    }
                }
            }
        }
    }
}