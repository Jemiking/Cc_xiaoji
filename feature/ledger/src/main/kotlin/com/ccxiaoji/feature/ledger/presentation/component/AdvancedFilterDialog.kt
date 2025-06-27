package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.presentation.viewmodel.FilterPreset
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionFilter
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DateRange
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 高级筛选对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterDialog(
    currentFilter: TransactionFilter,
    categories: List<Category>,
    accounts: List<Account>,
    filterPresets: List<FilterPreset>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionFilter) -> Unit,
    onPresetSelected: (FilterPreset) -> Unit,
    onClearFilter: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.advanced_filter))
                TextButton(
                    onClick = {
                        onClearFilter()
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.clear_filter))
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            items(filterPresets) { preset ->
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        onPresetSelected(preset)
                                        onDismiss()
                                    },
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
                        value = tempFilter.keyword ?: "",
                        onValueChange = { tempFilter = tempFilter.copy(keyword = it.ifBlank { null }) },
                        label = { Text(stringResource(R.string.search_by_note)) },
                        placeholder = { Text(stringResource(R.string.search_keyword_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                                selected = tempFilter.transactionType == TransactionType.ALL,
                                onClick = { tempFilter = tempFilter.copy(transactionType = TransactionType.ALL) },
                                label = { Text(stringResource(R.string.all)) }
                            )
                            FilterChip(
                                selected = tempFilter.transactionType == TransactionType.INCOME,
                                onClick = { tempFilter = tempFilter.copy(transactionType = TransactionType.INCOME) },
                                label = { Text(stringResource(R.string.income)) }
                            )
                            FilterChip(
                                selected = tempFilter.transactionType == TransactionType.EXPENSE,
                                onClick = { tempFilter = tempFilter.copy(transactionType = TransactionType.EXPENSE) },
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
                                value = tempFilter.minAmount?.toString() ?: "",
                                onValueChange = { 
                                    tempFilter = tempFilter.copy(
                                        minAmount = it.toDoubleOrNull()
                                    )
                                },
                                label = { Text(stringResource(R.string.min_amount)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = tempFilter.maxAmount?.toString() ?: "",
                                onValueChange = { 
                                    tempFilter = tempFilter.copy(
                                        maxAmount = it.toDoubleOrNull()
                                    )
                                },
                                label = { Text(stringResource(R.string.max_amount)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
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
                                    selected = tempFilter.accountId == null,
                                    onClick = { tempFilter = tempFilter.copy(accountId = null) },
                                    label = { Text(stringResource(R.string.all_accounts)) }
                                )
                            }
                            items(accounts) { account ->
                                FilterChip(
                                    selected = tempFilter.accountId == account.id,
                                    onClick = { tempFilter = tempFilter.copy(accountId = account.id) },
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
                                    selected = tempFilter.categoryIds.isEmpty(),
                                    onClick = { tempFilter = tempFilter.copy(categoryIds = emptySet()) },
                                    label = { Text(stringResource(R.string.all_categories)) }
                                )
                            }
                            items(categories) { category ->
                                FilterChip(
                                    selected = tempFilter.categoryIds.contains(category.id),
                                    onClick = {
                                        tempFilter = tempFilter.copy(
                                            categoryIds = if (tempFilter.categoryIds.contains(category.id)) {
                                                tempFilter.categoryIds - category.id
                                            } else {
                                                tempFilter.categoryIds + category.id
                                            }
                                        )
                                    },
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
                                    text = tempFilter.dateRange?.let { dateRange ->
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(tempFilter)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.apply_filter))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
    
    // 日期范围选择器（简化版，实际应使用日期选择器组件）
    if (showDateRangePicker) {
        AlertDialog(
            onDismissRequest = { showDateRangePicker = false },
            title = { Text(stringResource(R.string.select_date_range)) },
            text = {
                // 这里应该使用实际的日期选择器组件
                Text("日期选择器功能待实现")
            },
            confirmButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

/**
 * 根据预设图标名称获取对应的图标
 */
@Composable
private fun getIconForPreset(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "today" -> Icons.Default.Today
        "date_range" -> Icons.Default.DateRange
        "calendar_month" -> Icons.Default.CalendarMonth
        "trending_up" -> Icons.Default.TrendingUp
        "trending_down" -> Icons.Default.TrendingDown
        "attach_money" -> Icons.Default.AttachMoney
        else -> Icons.Default.FilterList
    }
}