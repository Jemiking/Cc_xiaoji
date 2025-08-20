package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.*
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard

@Composable
fun DateRangePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    // 默认日期范围：过去30天到今天
    val defaultStartDate = initialStartDate ?: today.minus(30, DateTimeUnit.DAY)
    val defaultEndDate = initialEndDate ?: today
    
    var startDate by remember { mutableStateOf(defaultStartDate) }
    var endDate by remember { mutableStateOf(defaultEndDate) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.large),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 标题
                Text(
                    text = "选择日期范围",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Ledger
                )
                
                // 开始日期选择
                DateSelectionCard(
                    label = "开始日期",
                    date = startDate,
                    onClick = { showStartDatePicker = true }
                )
                
                // 结束日期选择
                DateSelectionCard(
                    label = "结束日期", 
                    date = endDate,
                    onClick = { showEndDatePicker = true }
                )
                
                // 预设快捷选项
                QuickDateRangeOptions(
                    onRangeSelected = { start, end ->
                        startDate = start
                        endDate = end
                    }
                )
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        DesignTokens.Spacing.medium,
                        Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            if (startDate <= endDate) {
                                onConfirm(startDate, endDate)
                            }
                        },
                        enabled = startDate <= endDate,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.BrandColors.Ledger
                        )
                    ) {
                        Text("确定")
                    }
                }
                
                // 日期验证提示
                if (startDate > endDate) {
                    Text(
                        text = "开始日期不能晚于结束日期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    // 开始日期选择器
    if (showStartDatePicker) {
        SimpleDatePicker(
            initialDate = startDate,
            onDateSelected = { 
                startDate = it
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    // 结束日期选择器
    if (showEndDatePicker) {
        SimpleDatePicker(
            initialDate = endDate,
            onDateSelected = { 
                endDate = it
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
private fun DateSelectionCard(
    label: String,
    date: LocalDate,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun QuickDateRangeOptions(
    onRangeSelected: (startDate: LocalDate, endDate: LocalDate) -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    Column(
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        Text(
            text = "快捷选择",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            // 最近7天
            OutlinedButton(
                onClick = {
                    onRangeSelected(
                        today.minus(7, DateTimeUnit.DAY),
                        today
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("最近7天", style = MaterialTheme.typography.labelMedium)
            }
            
            // 最近30天
            OutlinedButton(
                onClick = {
                    onRangeSelected(
                        today.minus(30, DateTimeUnit.DAY),
                        today
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("最近30天", style = MaterialTheme.typography.labelMedium)
            }
            
            // 最近90天
            OutlinedButton(
                onClick = {
                    onRangeSelected(
                        today.minus(90, DateTimeUnit.DAY),
                        today
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("最近90天", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun SimpleDatePicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // 简化的日期选择器实现
    // 在实际项目中，可以使用 Material3 的 DatePicker
    // 或者第三方日期选择器库
    
    var selectedYear by remember { mutableStateOf(initialDate.year) }
    var selectedMonth by remember { mutableStateOf(initialDate.monthNumber) }
    var selectedDay by remember { mutableStateOf(initialDate.dayOfMonth) }
    
    Dialog(onDismissRequest = onDismiss) {
        ModernCard(
            modifier = Modifier.padding(DesignTokens.Spacing.medium),
            backgroundColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(DesignTokens.Spacing.large),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                Text(
                    text = "选择日期",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                
                // 显示当前选择的日期
                Text(
                    text = "${selectedYear}-${selectedMonth.toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DesignTokens.BrandColors.Ledger
                )
                
                Text(
                    text = "注：在实际项目中，这里应该集成完整的日期选择器组件",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        DesignTokens.Spacing.medium,
                        Alignment.End
                    )
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            val newDate = LocalDate(selectedYear, selectedMonth, selectedDay)
                            onDateSelected(newDate)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignTokens.BrandColors.Ledger
                        )
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}