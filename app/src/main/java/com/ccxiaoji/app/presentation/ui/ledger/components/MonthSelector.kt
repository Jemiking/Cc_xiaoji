package com.ccxiaoji.app.presentation.ui.ledger.components

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
                    // TODO: Show date picker dialog
                    expanded = false
                }
            )
        }
    }
}

private fun formatYearMonth(yearMonth: YearMonth): String {
    return "${yearMonth.year}年${yearMonth.monthValue}月"
}