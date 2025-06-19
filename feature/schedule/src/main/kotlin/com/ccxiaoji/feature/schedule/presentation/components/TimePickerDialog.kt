package com.ccxiaoji.feature.schedule.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import java.time.LocalTime

/**
 * 时间选择器对话框
 * @param showDialog 是否显示对话框
 * @param initialTime 初始时间，格式 "HH:mm"
 * @param onTimeSelected 时间选择回调
 * @param onDismiss 关闭对话框回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    showDialog: Boolean,
    initialTime: String = "08:00",
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        // 解析初始时间
        val timeParts = initialTime.split(":")
        val initialHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
        val initialMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        
        // 时间选择器状态
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.schedule_dialog_select_time)) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        onTimeSelected("$hour:$minute")
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        )
    }
}