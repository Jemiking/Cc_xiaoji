package com.ccxiaoji.feature.ledger.presentation.screen.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.feature.ledger.data.local.entity.RecurringTransactionEntity
import com.ccxiaoji.feature.ledger.presentation.viewmodel.RecurringTransactionViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.RecurringTransactionUiState
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recurringTransactions by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定期交易") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加定期交易")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recurringTransactions) { transaction ->
                RecurringTransactionItem(
                    transaction = transaction,
                    onToggleEnabled = { viewModel.toggleEnabled(transaction) },
                    onEdit = { viewModel.showEditDialog(transaction) },
                    onDelete = { viewModel.deleteRecurringTransaction(transaction) },
                    formatNextExecutionDate = viewModel::formatNextExecutionDate,
                    getFrequencyText = viewModel::getFrequencyText
                )
            }
        }
    }
    
    if (uiState.showDialog) {
        RecurringTransactionDialog(
            uiState = uiState,
            onDismiss = viewModel::hideDialog,
            onSave = viewModel::saveRecurringTransaction,
            onNameChange = viewModel::updateName,
            onAmountChange = viewModel::updateAmount,
            onAccountIdChange = viewModel::updateAccountId,
            onCategoryIdChange = viewModel::updateCategoryId,
            onNoteChange = viewModel::updateNote,
            onFrequencyChange = viewModel::updateFrequency,
            onDayOfWeekChange = viewModel::updateDayOfWeek,
            onDayOfMonthChange = viewModel::updateDayOfMonth,
            onMonthOfYearChange = viewModel::updateMonthOfYear,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransactionEntity,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    formatNextExecutionDate: (Long) -> String,
    getFrequencyText: (RecurringTransactionEntity) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${if (transaction.amountCents >= 0) "+" else ""}${
                        NumberFormat.getCurrencyInstance(Locale.CHINA).format(
                            transaction.amountCents / 100.0
                        )
                    }",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (transaction.amountCents >= 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = getFrequencyText(transaction),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (transaction.isEnabled) {
                    Text(
                        text = "下次执行: ${formatNextExecutionDate(transaction.nextExecutionDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onToggleEnabled) {
                    Icon(
                        if (transaction.isEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (transaction.isEnabled) "暂停" else "启用"
                    )
                }
                
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                }
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionDialog(
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
            Text(if (uiState.editingTransaction != null) "编辑定期交易" else "添加定期交易") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = if (uiState.amountCents == 0) "" else (uiState.amountCents / 100.0).toString(),
                    onValueChange = { value ->
                        value.toDoubleOrNull()?.let { 
                            onAmountChange((it * 100).toInt())
                        }
                    },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // TODO: 账户和分类选择器
                
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = onNoteChange,
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
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
                            .menuAnchor()
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
                    }
                    RecurringFrequency.MONTHLY -> {
                        OutlinedTextField(
                            value = uiState.dayOfMonth?.toString() ?: "",
                            onValueChange = { value ->
                                value.toIntOrNull()?.let { day ->
                                    if (day in 1..31) onDayOfMonthChange(day)
                                }
                            },
                            label = { Text("每月几号") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    RecurringFrequency.YEARLY -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.monthOfYear?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { month ->
                                        if (month in 1..12) onMonthOfYearChange(month)
                                    }
                                },
                                label = { Text("月份") },
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = uiState.dayOfMonth?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { day ->
                                        if (day in 1..31) onDayOfMonthChange(day)
                                    }
                                },
                                label = { Text("日期") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    else -> {}
                }
                
                // TODO: 开始日期和结束日期选择器
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.name.isNotBlank() && uiState.amountCents != 0
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}