package com.ccxiaoji.feature.schedule.presentation.ui.shift

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
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.core.ui.theme.*
import com.ccxiaoji.feature.schedule.presentation.ui.components.CustomTimePickerDialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 班次编辑状态
 */
@Stable
private data class ShiftEditState(
    val name: String = "",
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(18, 0),
    val selectedColor: Int = Shift.PRESET_COLORS.first(),
    val description: String = ""
)

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
    var editState by remember(shift) {
        mutableStateOf(ShiftEditState(
            name = shift?.name ?: "",
            startTime = shift?.startTime ?: LocalTime.of(9, 0),
            endTime = shift?.endTime ?: LocalTime.of(18, 0),
            selectedColor = shift?.color ?: Shift.PRESET_COLORS.first(),
            description = shift?.description ?: ""
        ))
    }
    
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
                    value = editState.name,
                    onValueChange = { editState = editState.copy(name = it) },
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
                        value = editState.startTime.format(timeFormatter),
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
                        value = editState.endTime.format(timeFormatter),
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
                                .clickable { editState = editState.copy(selectedColor = color) }
                                .then(
                                    if (editState.selectedColor == color) {
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
                    value = editState.description,
                    onValueChange = { editState = editState.copy(description = it) },
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
                            if (editState.name.isNotBlank()) {
                                onConfirm(
                                    Shift(
                                        id = shift?.id ?: 0,
                                        name = editState.name.trim(),
                                        startTime = editState.startTime,
                                        endTime = editState.endTime,
                                        color = editState.selectedColor,
                                        description = editState.description.ifBlank { null }
                                    )
                                )
                            }
                        },
                        enabled = editState.name.isNotBlank()
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
        initialTime = editState.startTime,
        onTimeSelected = { time ->
            editState = editState.copy(startTime = time)
            showStartTimePicker = false
        },
        onDismiss = { showStartTimePicker = false }
    )
    
    CustomTimePickerDialog(
        showDialog = showEndTimePicker,
        initialTime = editState.endTime,
        onTimeSelected = { time ->
            editState = editState.copy(endTime = time)
            showEndTimePicker = false
        },
        onDismiss = { showEndTimePicker = false }
    )
}

// 旧的时间选择器已被 CustomTimePickerDialog 替代