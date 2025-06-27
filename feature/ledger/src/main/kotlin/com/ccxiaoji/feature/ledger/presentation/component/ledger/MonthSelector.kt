package com.ccxiaoji.feature.ledger.presentation.component.ledger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MonthSelector(
    currentMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val currentYearMonth = YearMonth.now()
    
    // Generate list of months (current month and previous 11 months)
    val monthOptions = remember(currentYearMonth) {
        (0..11).map { monthsBack ->
            currentYearMonth.minusMonths(monthsBack.toLong())
        }
    }
    
    Box(modifier = modifier) {
        // Month display button
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatYearMonth(currentMonth),
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "选择月份",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            monthOptions.forEach { yearMonth ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = formatYearMonth(yearMonth))
                            if (yearMonth == currentMonth) {
                                Text(
                                    text = "当前",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onMonthSelected(yearMonth)
                        expanded = false
                    }
                )
            }
            
            Divider()
            
            // Option to select custom month
            DropdownMenuItem(
                text = { Text("选择其他月份...") },
                onClick = {
                    showDatePicker = true
                    expanded = false
                }
            )
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        MonthYearPickerDialog(
            initialMonth = currentMonth,
            onMonthSelected = { selectedMonth ->
                onMonthSelected(selectedMonth)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialMonth.year) }
    var selectedMonth by remember { mutableStateOf(initialMonth.monthValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择月份") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Year selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("年份:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedYear -= 1 }) {
                            Text("-")
                        }
                        Text(
                            text = selectedYear.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(60.dp)
                        )
                        TextButton(onClick = { selectedYear += 1 }) {
                            Text("+")
                        }
                    }
                }
                
                // Month selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("月份:", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { 
                            selectedMonth = if (selectedMonth > 1) selectedMonth - 1 else 12
                        }) {
                            Text("-")
                        }
                        Text(
                            text = "${selectedMonth}月",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.width(60.dp)
                        )
                        TextButton(onClick = { 
                            selectedMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
                        }) {
                            Text("+")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onMonthSelected(YearMonth.of(selectedYear, selectedMonth))
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
    )
}

private fun formatYearMonth(yearMonth: YearMonth): String {
    return "${yearMonth.year}年${yearMonth.monthValue}月"
}