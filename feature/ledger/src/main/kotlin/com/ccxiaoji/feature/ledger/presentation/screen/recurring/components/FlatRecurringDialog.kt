package com.ccxiaoji.feature.ledger.presentation.screen.recurring.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.viewmodel.RecurringTransactionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatRecurringDialog(
    uiState: RecurringTransactionUiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onNameChange: (String) -> Unit,
    onAmountChange: (Int) -> Unit,
    onAccountIdChange: (String) -> Unit,
    onCategoryIdChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onFrequencyChange: (RecurringFrequency) -> Unit,
    onDayOfWeekChange: (Int?) -> Unit,
    onDayOfMonthChange: (Int?) -> Unit,
    onMonthOfYearChange: (Int?) -> Unit,
    onStartDateChange: (Long) -> Unit,
    onEndDateChange: (Long?) -> Unit
) {
    var expandedFrequency by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = if (uiState.editingTransaction != null) "编辑定期交易" else "添加定期交易",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 名称输入
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                )
                
                // 金额输入
                OutlinedTextField(
                    value = if (uiState.amountCents == 0) "" else (uiState.amountCents / 100.0).toString(),
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { 
                            onAmountChange((it * 100).toInt())
                        }
                    },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                )
                
                // 备注输入
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                )
                
                // 频率选择器
                ExposedDropdownMenuBox(
                    expanded = expandedFrequency,
                    onExpandedChange = { expandedFrequency = it }
                ) {
                    OutlinedTextField(
                        value = when (uiState.frequency) {
                            RecurringFrequency.DAILY -> "每天"
                            RecurringFrequency.WEEKLY -> "每周"
                            RecurringFrequency.MONTHLY -> "每月"
                            RecurringFrequency.YEARLY -> "每年"
                            else -> "每月"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("频率") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrequency) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(),
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedFrequency,
                        onDismissRequest = { expandedFrequency = false }
                    ) {
                        RecurringFrequency.entries.forEach { frequency ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        when (frequency) {
                                            RecurringFrequency.DAILY -> "每天"
                                            RecurringFrequency.WEEKLY -> "每周"
                                            RecurringFrequency.MONTHLY -> "每月"
                                            RecurringFrequency.YEARLY -> "每年"
                                        }
                                    )
                                },
                                onClick = {
                                    onFrequencyChange(frequency)
                                    expandedFrequency = false
                                }
                            )
                        }
                    }
                }
                
                // 根据频率显示额外的选择器
                when (uiState.frequency) {
                    RecurringFrequency.WEEKLY -> {
                        // TODO: 星期选择器
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                        ) {
                            Text(
                                text = "星期选择器待实现",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(DesignTokens.Spacing.small)
                            )
                        }
                    }
                    RecurringFrequency.MONTHLY -> {
                        OutlinedTextField(
                            value = uiState.dayOfMonth?.toString() ?: "",
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { day ->
                                    if (day in 1..31) onDayOfMonthChange(day)
                                }
                            },
                            label = { Text("每月几号（1-31）") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(),
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                        )
                    }
                    RecurringFrequency.YEARLY -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                        ) {
                            OutlinedTextField(
                                value = uiState.monthOfYear?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { month ->
                                        if (month in 1..12) onMonthOfYearChange(month)
                                    }
                                },
                                label = { Text("月份（1-12）") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(),
                                shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                            )
                            
                            OutlinedTextField(
                                value = uiState.dayOfMonth?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { day ->
                                        if (day in 1..31) onDayOfMonthChange(day)
                                    }
                                },
                                label = { Text("日期（1-31）") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(),
                                shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                            )
                        }
                    }
                    else -> {}
                }
                
                // TODO: 开始日期和结束日期选择器
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                ) {
                    Text(
                        text = "日期选择器待实现",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(DesignTokens.Spacing.small)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.name.isNotBlank() && uiState.amountCents != 0,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DesignTokens.BrandColors.Ledger
                )
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("取消")
            }
        },
        shape = RoundedCornerShape(DesignTokens.BorderRadius.large),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}