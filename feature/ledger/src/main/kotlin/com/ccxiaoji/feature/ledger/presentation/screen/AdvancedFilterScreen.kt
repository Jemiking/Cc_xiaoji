package com.ccxiaoji.feature.ledger.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AdvancedFilterViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import java.time.format.DateTimeFormatter

/**
 * 高级筛选页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterScreen(
    navController: NavController,
    viewModel: AdvancedFilterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    // 处理筛选应用
    LaunchedEffect(uiState.isFilterApplied) {
        if (uiState.isFilterApplied) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("filter_applied", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.advanced_filter)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.clearFilter() }
                    ) {
                        Text(stringResource(R.string.clear_filter))
                    }
                    TextButton(
                        onClick = { viewModel.applyFilter() },
                        enabled = !uiState.isLoading
                    ) {
                        Text(stringResource(R.string.apply_filter))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 快速筛选预设
            item {
                Column {
                    Text(
                        text = stringResource(R.string.quick_filter),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filterPresets) { preset ->
                            FilterChip(
                                selected = false,
                                onClick = { viewModel.applyPreset(preset) },
                                label = { Text(preset.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getIconForPreset(preset.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            // 关键词搜索
            item {
                OutlinedTextField(
                    value = uiState.keyword,
                    onValueChange = viewModel::updateKeyword,
                    label = { Text(stringResource(R.string.search_by_note)) },
                    placeholder = { Text(stringResource(R.string.search_keyword_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )
            }
            
            // 交易类型筛选
            item {
                Column {
                    Text(
                        text = stringResource(R.string.transaction_type),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.transactionType == TransactionType.ALL,
                            onClick = { viewModel.updateTransactionType(TransactionType.ALL) },
                            label = { Text(stringResource(R.string.all)) }
                        )
                        FilterChip(
                            selected = uiState.transactionType == TransactionType.INCOME,
                            onClick = { viewModel.updateTransactionType(TransactionType.INCOME) },
                            label = { Text(stringResource(R.string.income)) }
                        )
                        FilterChip(
                            selected = uiState.transactionType == TransactionType.EXPENSE,
                            onClick = { viewModel.updateTransactionType(TransactionType.EXPENSE) },
                            label = { Text(stringResource(R.string.expense)) }
                        )
                    }
                }
            }
            
            // 金额范围
            item {
                Column {
                    Text(
                        text = stringResource(R.string.amount_range),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.minAmountText,
                            onValueChange = viewModel::updateMinAmount,
                            label = { Text(stringResource(R.string.min_amount)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                        OutlinedTextField(
                            value = uiState.maxAmountText,
                            onValueChange = viewModel::updateMaxAmount,
                            label = { Text(stringResource(R.string.max_amount)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            enabled = !uiState.isLoading
                        )
                    }
                }
            }
            
            // 账户选择
            item {
                Column {
                    Text(
                        text = stringResource(R.string.account_management),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedAccountId == null,
                                onClick = { viewModel.selectAccount(null) },
                                label = { Text(stringResource(R.string.all_accounts)) }
                            )
                        }
                        items(uiState.accounts) { account ->
                            FilterChip(
                                selected = uiState.selectedAccountId == account.id,
                                onClick = { viewModel.selectAccount(account.id) },
                                label = { Text(account.name) }
                            )
                        }
                    }
                }
            }
            
            // 分类选择
            item {
                Column {
                    Text(
                        text = stringResource(R.string.category),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedCategoryIds.isEmpty(),
                                onClick = { viewModel.clearCategorySelection() },
                                label = { Text(stringResource(R.string.all_categories)) }
                            )
                        }
                        items(uiState.categories) { category ->
                            FilterChip(
                                selected = uiState.selectedCategoryIds.contains(category.id),
                                onClick = { viewModel.toggleCategory(category.id) },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
            }
            
            // 日期范围
            item {
                Column {
                    Text(
                        text = stringResource(R.string.date_range),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDateRangePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.dateRange?.let { dateRange ->
                                    "${dateRange.startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))} ~ " +
                                    dateRange.endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                } ?: stringResource(R.string.select_date_range)
                            )
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    }
                }
            }
        }
        
        // 加载中指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
    
    // 日期范围选择器
    if (showDateRangePicker) {
        DateRangePickerDialog(
            currentRange = uiState.dateRange,
            onConfirm = { dateRange ->
                viewModel.updateDateRange(dateRange)
                showDateRangePicker = false
            },
            onDismiss = { showDateRangePicker = false }
        )
    }
}

@Composable
private fun getIconForPreset(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when(iconName) {
        "today" -> Icons.Default.Today
        "week" -> Icons.Default.DateRange
        "month" -> Icons.Default.CalendarMonth
        "income" -> Icons.Default.TrendingUp
        "expense" -> Icons.Default.TrendingDown
        "large" -> Icons.Default.AttachMoney
        else -> Icons.Default.FilterList
    }
}

// 日期范围选择对话框（临时实现，后续可改为独立页面）
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerDialog(
    currentRange: com.ccxiaoji.feature.ledger.presentation.viewmodel.DateRange?,
    onConfirm: (com.ccxiaoji.feature.ledger.presentation.viewmodel.DateRange) -> Unit,
    onDismiss: () -> Unit
) {
    // 这里可以使用 Material3 的 DateRangePicker
    // 暂时使用简单的实现
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期范围") },
        text = { 
            Text("日期范围选择器将在后续实现")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
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