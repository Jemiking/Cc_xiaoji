package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*

data class QuickDateOption(
    val label: String,
    val date: LocalDate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // 快捷日期选项
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val quickDateOptions = remember(today) {
        listOf(
            QuickDateOption("今天", today),
            QuickDateOption("昨天", today.minus(1, DateTimeUnit.DAY)),
            QuickDateOption("前天", today.minus(2, DateTimeUnit.DAY)),
            QuickDateOption("3天前", today.minus(3, DateTimeUnit.DAY)),
            QuickDateOption("一周前", today.minus(7, DateTimeUnit.DAY))
        )
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 快捷日期选择
        Text(
            text = "快捷选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
            contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.small)
        ) {
            items(quickDateOptions) { option ->
                FilterChip(
                    selected = selectedDate == option.date,
                    onClick = { onDateSelected(option.date) },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f),
                        selectedLabelColor = DesignTokens.BrandColors.Ledger
                    )
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 日期选择
            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "选择日期",
                        tint = DesignTokens.BrandColors.Ledger
                    )
                    Column {
                        Text(
                            text = "日期",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDate(selectedDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // 时间选择
            OutlinedCard(
                modifier = Modifier.weight(1f),
                onClick = { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = "选择时间",
                        tint = DesignTokens.BrandColors.Ledger
                    )
                    Column {
                        Text(
                            text = "时间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(selectedTime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        )
        
        DatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    val instant = Instant.fromEpochMilliseconds(it)
                    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    onDateSelected(localDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // 时间选择器对话框
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                onTimeSelected(LocalTime(hour, minute))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = { content() },
        confirmButton = {
            TextButton(onClick = { onDateSelected(null) }) {
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

@Composable
private fun TimePickerDialog(
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = { content() },
        confirmButton = {
            TextButton(onClick = { 
                // 这里需要从TimePicker状态获取时间
                onTimeSelected(0, 0) // 临时实现
            }) {
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

private fun formatDate(date: LocalDate): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return when {
        date == today -> "今天"
        date == today.minus(1, DateTimeUnit.DAY) -> "昨天"
        date == today.minus(2, DateTimeUnit.DAY) -> "前天"
        else -> "${date.monthNumber}月${date.dayOfMonth}日"
    }
}

private fun formatTime(time: LocalTime): String {
    return String.format("%02d:%02d", time.hour, time.minute)
}