package com.ccxiaoji.feature.schedule.presentation.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 基础日期选择对话框
 * 支持多种参数风格以保证兼容性
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate? = null,
    selectedDate: LocalDate? = initialDate,
    weekStartDay: Int = 1, // 1 = Monday, 7 = Sunday
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    if (showDialog) {
        val dateToUse = selectedDate ?: initialDate ?: LocalDate.now()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateToUse
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(date)
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}