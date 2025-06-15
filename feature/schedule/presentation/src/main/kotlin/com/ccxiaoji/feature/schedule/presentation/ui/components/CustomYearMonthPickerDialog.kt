package com.ccxiaoji.feature.schedule.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * 自定义年月选择对话框
 */
@Composable
fun CustomYearMonthPickerDialog(
    showDialog: Boolean,
    currentYearMonth: YearMonth,
    onDismiss: () -> Unit,
    onYearMonthSelected: (YearMonth) -> Unit
) {
    if (showDialog) {
        var selectedYear by remember { mutableIntStateOf(currentYearMonth.year) }
        var selectedMonth by remember { mutableIntStateOf(currentYearMonth.monthValue) }
        
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "选择年月",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // 年份选择
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { selectedYear-- }) {
                            Text("<")
                        }
                        Text(
                            text = "${selectedYear}年",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        TextButton(onClick = { selectedYear++ }) {
                            Text(">")
                        }
                    }
                    
                    // 月份选择
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..12).toList()) { month ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMonth = month },
                                colors = if (month == selectedMonth) {
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    CardDefaults.cardColors()
                                }
                            ) {
                                Text(
                                    text = "${month}月",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    color = if (month == selectedMonth) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                    
                    // 按钮
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                onYearMonthSelected(YearMonth.of(selectedYear, selectedMonth))
                                onDismiss()
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}