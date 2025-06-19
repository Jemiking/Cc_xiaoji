package com.ccxiaoji.feature.schedule.presentation.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.theme.*
import com.ccxiaoji.feature.schedule.presentation.components.CustomTimePickerDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 班次编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftEditDialog(
    shift: Shift?,
    onDismiss: () -> Unit,
    onConfirm: (Shift) -> Unit
) {
    var name by remember(shift) { mutableStateOf(shift?.name ?: "") }
    var startTime by remember(shift) { mutableStateOf(shift?.startTime ?: LocalTime.of(9, 0)) }
    var endTime by remember(shift) { mutableStateOf(shift?.endTime ?: LocalTime.of(18, 0)) }
    var selectedColor by remember(shift) { mutableStateOf(shift?.color ?: Shift.PRESET_COLORS.first()) }
    var description by remember(shift) { mutableStateOf(shift?.description ?: "") }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Text(
                    text = if (shift == null) "新建班次" else "编辑班次",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 班次名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("班次名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 时间选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 开始时间
                    OutlinedTextField(
                        value = startTime.format(timeFormatter),
                        onValueChange = { },
                        label = { Text("开始时间") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showStartTimePicker = true }) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "选择开始时间"
                                )
                            }
                        }
                    )
                    
                    // 结束时间
                    OutlinedTextField(
                        value = endTime.format(timeFormatter),
                        onValueChange = { },
                        label = { Text("结束时间") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showEndTimePicker = true }) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "选择结束时间"
                                )
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 颜色选择
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Shift.PRESET_COLORS) { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color(color),
                                    shape = MaterialTheme.shapes.small
                                )
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = MaterialTheme.shapes.small
                                        )
                                    } else Modifier
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 描述
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    Shift(
                                        id = shift?.id ?: 0,
                                        name = name.trim(),
                                        startTime = startTime,
                                        endTime = endTime,
                                        color = selectedColor,
                                        description = description.ifBlank { null }
                                    )
                                )
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
    
    // 时间选择器
    CustomTimePickerDialog(
        showDialog = showStartTimePicker,
        initialTime = startTime,
        onTimeSelected = { time ->
            startTime = time
            showStartTimePicker = false
        },
        onDismiss = { showStartTimePicker = false }
    )
    
    CustomTimePickerDialog(
        showDialog = showEndTimePicker,
        initialTime = endTime,
        onTimeSelected = { time ->
            endTime = time
            showEndTimePicker = false
        },
        onDismiss = { showEndTimePicker = false }
    )
}

// 旧的时间选择器已被 CustomTimePickerDialog 替代