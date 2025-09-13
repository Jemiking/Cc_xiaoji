package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleCatalogDemoScreen(
    viewModel: LedgerViewModel = hiltViewModel()
) {
    var selectedStyle by remember { mutableStateOf(DemoStyle.CardBased) }
    var selectedDensity by remember { mutableStateOf(DemoDensity.Medium) }
    var showDensityMenu by remember { mutableStateOf(false) }
    
    DemoTheme(
        style = selectedStyle,
        density = selectedDensity
    ) {
        Scaffold(
            topBar = {
                StyleCatalogTopBar(
                    selectedStyle = selectedStyle,
                    onStyleChange = { selectedStyle = it },
                    selectedDensity = selectedDensity,
                    onDensityChange = { selectedDensity = it },
                    showDensityMenu = showDensityMenu,
                    onDensityMenuToggle = { showDensityMenu = it }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = if (selectedDensity == DemoDensity.Compact) 8.dp else 16.dp,
                    vertical = if (selectedDensity == DemoDensity.Compact) 8.dp else 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(
                    if (selectedDensity == DemoDensity.Compact) 8.dp else 12.dp
                )
            ) {
                // 汇总卡片
                item {
                    DemoOverviewCard(
                        income = 5280.00,
                        expense = 3150.00,
                        balance = 2130.00,
                        style = selectedStyle,
                        density = selectedDensity
                    )
                }
                
                // 筛选条（禁用态）
                item {
                    DemoFilterBar(
                        enabled = false,
                        style = selectedStyle,
                        density = selectedDensity
                    )
                }
                
                // 明暗对比预览卡
                item {
                    DemoStylePreviewCard(
                        currentStyle = selectedStyle,
                        density = selectedDensity
                    )
                }
                
                // 交易列表（示例数据）
                val groupedTransactions = getDemoTransactions()
                groupedTransactions.forEach { (date, transactions) ->
                    item {
                        DemoDateHeader(
                            date = date,
                            style = selectedStyle,
                            density = selectedDensity
                        )
                    }
                    
                    items(transactions) { transaction ->
                        DemoTransactionItem(
                            transaction = transaction,
                            style = selectedStyle,
                            density = selectedDensity,
                            onClick = { /* 禁用交互 */ }
                        )
                    }
                }
                
                // 分页控件（禁用态）
                item {
                    DemoPaginationBar(
                        enabled = false,
                        style = selectedStyle,
                        density = selectedDensity
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StyleCatalogTopBar(
    selectedStyle: DemoStyle,
    onStyleChange: (DemoStyle) -> Unit,
    selectedDensity: DemoDensity,
    onDensityChange: (DemoDensity) -> Unit,
    showDensityMenu: Boolean,
    onDensityMenuToggle: (Boolean) -> Unit
) {
    var showStyleMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = {
            Text(
                text = "记账 · 交易列表 · Demo",
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            // 风格选择器
            Box {
                TextButton(
                    onClick = { showStyleMenu = true }
                ) {
                    Text(
                        text = selectedStyle.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                DropdownMenu(
                    expanded = showStyleMenu,
                    onDismissRequest = { showStyleMenu = false }
                ) {
                    DemoStyle.values().forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(style.styleName)
                                    Text(
                                        style.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                onStyleChange(style)
                                showStyleMenu = false
                            },
                            leadingIcon = {
                                if (style == selectedStyle) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                    }
                }
            }
            
            // 密度切换
            IconButton(
                onClick = { onDensityMenuToggle(true) }
            ) {
                Icon(
                    imageVector = Icons.Default.FormatSize,
                    contentDescription = "密度切换"
                )
            }
            
            DropdownMenu(
                expanded = showDensityMenu,
                onDismissRequest = { onDensityMenuToggle(false) }
            ) {
                DemoDensity.values().forEach { density ->
                    DropdownMenuItem(
                        text = { Text(density.displayName) },
                        onClick = {
                            onDensityChange(density)
                            onDensityMenuToggle(false)
                        },
                        leadingIcon = {
                            if (density == selectedDensity) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

// 示例数据 - 使用标准的DemoTransaction定义
private fun getDemoTransactions(): Map<String, List<DemoTransaction>> {
    val now = Clock.System.now()
    return mapOf(
        "2025年9月7日" to listOf(
            DemoTransaction(
                id = "1", 
                amount = -25.00, 
                type = TransactionType.EXPENSE,
                category = DemoCategory("cat1", "餐饮", "🍽", TransactionType.EXPENSE),
                account = DemoAccount("acc1", "支付宝", AccountType.ALIPAY, 1000.0, "💰"),
                note = "午餐",
                dateTime = now,
                tags = listOf(DemoTag("tag1", "工作餐", "#FF5722"))
            ),
            DemoTransaction(
                id = "2", 
                amount = -4.00, 
                type = TransactionType.EXPENSE,
                category = DemoCategory("cat2", "交通", "🚇", TransactionType.EXPENSE),
                account = DemoAccount("acc2", "交通卡", AccountType.OTHER, 100.0, "🚌"),
                note = "地铁",
                dateTime = now,
                tags = emptyList()
            ),
            DemoTransaction(
                id = "3", 
                amount = -89.50, 
                type = TransactionType.EXPENSE,
                category = DemoCategory("cat3", "购物", "🛒", TransactionType.EXPENSE),
                account = DemoAccount("acc3", "微信", AccountType.WECHAT, 500.0, "💚"),
                note = "日用品",
                dateTime = now,
                tags = listOf(DemoTag("tag2", "生活", "#4CAF50"), DemoTag("tag3", "超市", "#2196F3"))
            )
        ),
        "2025年9月6日" to listOf(
            DemoTransaction(
                id = "4", 
                amount = 8500.00, 
                type = TransactionType.INCOME,
                category = DemoCategory("cat4", "工资", "💰", TransactionType.INCOME),
                account = DemoAccount("acc4", "银行卡", AccountType.BANK_CARD, 10000.0, "🏦"),
                note = "8月工资",
                dateTime = now,
                tags = listOf(DemoTag("tag4", "收入", "#8BC34A"))
            ),
            DemoTransaction(
                id = "5", 
                amount = -158.00, 
                type = TransactionType.EXPENSE,
                category = DemoCategory("cat1", "餐饮", "🍽", TransactionType.EXPENSE),
                account = DemoAccount("acc5", "信用卡", AccountType.CREDIT_CARD, 5000.0, "💳"),
                note = "晚餐",
                dateTime = now,
                tags = listOf(DemoTag("tag5", "聚餐", "#FF9800"))
            ),
            DemoTransaction(
                id = "6", 
                amount = -78.00, 
                type = TransactionType.EXPENSE,
                category = DemoCategory("cat6", "娱乐", "🎬", TransactionType.EXPENSE),
                account = DemoAccount("acc1", "支付宝", AccountType.ALIPAY, 1000.0, "💰"),
                note = "电影票",
                dateTime = now,
                tags = emptyList()
            )
        )
    )
}