package com.ccxiaoji.feature.schedule.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate

/**
 * 自定义日期范围选择对话框
 */
@Composable
fun CustomDateRangePickerDialog(
    showDialog: Boolean,
    initialStartDate: LocalDate,
    initialEndDate: LocalDate,
    weekStartDay: Int = 1,
    onDismiss: () -> Unit,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    if (showDialog) {
        var startDate by remember { mutableStateOf(initialStartDate) }
        var endDate by remember { mutableStateOf(initialEndDate) }
        
        // 使用简单的对话框实现，由于需要选择两个日期
        Dialog(onDismissRequest = onDismiss) {
            Card {
                Column(
                    modifier = androidx.compose.ui.Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "选择日期范围",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    
                    Text("开始日期: ${startDate}")
                    Text("结束日期: ${endDate}")
                    
                    Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    
                    Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = { onDateRangeSelected(startDate, endDate) }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}