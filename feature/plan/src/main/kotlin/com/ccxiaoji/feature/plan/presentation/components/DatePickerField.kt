package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.datetime.*

/**
 * 可重用的日期选择器字段
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text("选择日期") },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
            }
        },
        isError = isError,
        supportingText = supportingText,
        singleLine = true
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = date,
            onDateSelected = { selectedDate ->
                onDateChange(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atStartOfDayIn(TimeZone.currentSystemDefault())
            ?.toEpochMilliseconds()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        onDateSelected(localDate)
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
        }
    ) {
        DatePicker(state = datePickerState)
    }
}