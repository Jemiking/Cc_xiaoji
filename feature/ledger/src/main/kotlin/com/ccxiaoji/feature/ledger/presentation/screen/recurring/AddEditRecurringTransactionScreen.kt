package com.ccxiaoji.feature.ledger.presentation.screen.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddEditRecurringTransactionViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringTransactionScreen(
    navController: NavController,
    recurringId: String? = null,
    viewModel: AddEditRecurringTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedFrequency by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("recurring_saved", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recurringId != null) "编辑定期交易" else "添加定期交易") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 名称输入
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("名称") },
                        isError = uiState.nameError != null,
                        supportingText = {
                            uiState.nameError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // 金额输入
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = viewModel::updateAmount,
                        label = { Text("金额") },
                        prefix = { Text("¥") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        isError = uiState.amountError != null,
                        supportingText = {
                            uiState.amountError?.let { Text(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // 账户选择 - 简化实现
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "账户选择",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "账户选择功能待实现",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // 分类选择 - 简化实现
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "分类选择",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "分类选择功能待实现",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    // 备注输入
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        label = { Text("备注 (可选)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
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
                                        viewModel.updateFrequency(frequency)
                                        expandedFrequency = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // 根据频率显示额外的选择器
                    when (uiState.frequency) {
                        RecurringFrequency.WEEKLY -> {
                            // 星期选择器
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(DesignTokens.Spacing.medium)
                                ) {
                                    Text(
                                        text = "星期选择",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                                        val weekValues = listOf(2, 3, 4, 5, 6, 7, 1) // Calendar constants
                                        weekDays.forEachIndexed { index, day ->
                                            FilterChip(
                                                selected = uiState.dayOfWeek == weekValues[index],
                                                onClick = { viewModel.updateDayOfWeek(weekValues[index]) },
                                                label = { Text(day) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        RecurringFrequency.MONTHLY -> {
                            OutlinedTextField(
                                value = uiState.dayOfMonth?.toString() ?: "",
                                onValueChange = { value ->
                                    value.toIntOrNull()?.let { day ->
                                        if (day in 1..31) viewModel.updateDayOfMonth(day)
                                    }
                                },
                                label = { Text("每月几号（1-31）") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
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
                                            if (month in 1..12) viewModel.updateMonthOfYear(month)
                                        }
                                    },
                                    label = { Text("月份（1-12）") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                
                                OutlinedTextField(
                                    value = uiState.dayOfMonth?.toString() ?: "",
                                    onValueChange = { value ->
                                        value.toIntOrNull()?.let { day ->
                                            if (day in 1..31) viewModel.updateDayOfMonth(day)
                                        }
                                    },
                                    label = { Text("日期（1-31）") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                        else -> {}
                    }
                    
                    // 日期选择
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "开始日期和结束日期",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "日期选择功能待实现",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
                    
                    // 保存按钮
                    Button(
                        onClick = viewModel::saveRecurringTransaction,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("保存")
                    }
                }
            }
            
            // 错误提示
            uiState.errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text("关闭")
                        }
                    },
                    content = { Text(error) }
                )
            }
        }
    }
}