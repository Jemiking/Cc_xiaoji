package com.ccxiaoji.feature.ledger.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.ledger.presentation.viewmodel.GroupingMode
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionGroup
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    navigator: LedgerNavigator,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LedgerDrawerContent(
                navigator = navigator,
                onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        gesturesEnabled = !uiState.isSelectionMode && !uiState.isSearchMode
    ) {
        Scaffold(
            topBar = {
                LedgerTopBar(
                    uiState = uiState,
                    selectedMonth = selectedMonth,
                    onMonthSelected = viewModel::selectMonth,
                    onToggleSearch = viewModel::toggleSearchMode,
                    onSearchQueryChange = viewModel::searchTransactions,
                    onClearSearch = { viewModel.searchTransactions("") },
                    onToggleSelection = viewModel::toggleSelectionMode,
                    onSelectAll = viewModel::selectAllTransactions,
                    onDeleteSelected = viewModel::deleteSelectedTransactions,
                    onOpenDrawer = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            floatingActionButton = {
                if (!uiState.isSelectionMode && !uiState.isSearchMode) {
                    FloatingActionButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加交易")
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 本月收支概览
                MonthlyOverviewBar(
                    monthlyIncome = uiState.monthlyIncome,
                    monthlyExpense = uiState.monthlyExpense
                )
                
                HorizontalDivider()
                
                // 分组选项
                if (!uiState.isSearchMode) {
                    GroupingOptions(
                        currentMode = uiState.groupingMode,
                        onModeSelected = viewModel::setGroupingMode
                    )
                }
                
                // 交易列表
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.isSearchMode) "没有找到相关交易记录" else "暂无交易记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.groupingMode == GroupingMode.NONE) {
                            items(uiState.transactions) { transaction ->
                                TransactionListItem(
                                    transaction = transaction,
                                    isSelected = uiState.selectedTransactionIds.contains(transaction.id),
                                    isSelectionMode = uiState.isSelectionMode,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleTransactionSelection(transaction.id)
                                        } else {
                                            navigator.navigateToTransactionDetail(transaction.id)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.toggleSelectionMode()
                                            viewModel.toggleTransactionSelection(transaction.id)
                                        }
                                    }
                                )
                            }
                        } else {
                            uiState.groupedTransactions.forEach { group ->
                                item {
                                    TransactionGroupHeader(group = group)
                                }
                                
                                items(group.transactions) { transaction ->
                                    TransactionListItem(
                                        transaction = transaction,
                                        isSelected = uiState.selectedTransactionIds.contains(transaction.id),
                                        isSelectionMode = uiState.isSelectionMode,
                                        onClick = {
                                            if (uiState.isSelectionMode) {
                                                viewModel.toggleTransactionSelection(transaction.id)
                                            } else {
                                                navigator.navigateToTransactionDetail(transaction.id)
                                            }
                                        },
                                        onLongClick = {
                                            if (!uiState.isSelectionMode) {
                                                viewModel.toggleSelectionMode()
                                                viewModel.toggleTransactionSelection(transaction.id)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加交易对话框
    if (showAddDialog) {
        AddTransactionDialog(
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { amountCents, categoryId, note ->
                viewModel.addTransaction(amountCents, categoryId, note)
                showAddDialog = false
            }
        )
    }
    
    // 编辑交易对话框
    editingTransactionId?.let { transactionId ->
        uiState.editingTransactionDetail?.let { detail ->
            EditTransactionDialog(
                transactionDetail = detail,
                categories = uiState.categories,
                onDismiss = { 
                    editingTransactionId = null
                    viewModel.setEditingTransaction(null)
                },
                onConfirm = { amountCents, categoryId, note ->
                    viewModel.updateTransaction(transactionId, amountCents, categoryId, note)
                    editingTransactionId = null
                    viewModel.setEditingTransaction(null)
                }
            )
        }
    }
    
    // 错误提示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 显示错误信息
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LedgerTopBar(
    uiState: com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerUiState,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onToggleSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    when {
        uiState.isSearchMode -> {
            TopAppBar(
                title = {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("搜索交易记录...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        onToggleSearch()
                        onClearSearch()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "退出搜索")
                    }
                },
                actions = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "清除搜索")
                        }
                    }
                }
            )
        }
        uiState.isSelectionMode -> {
            TopAppBar(
                title = { 
                    Text(
                        text = "已选择 ${uiState.selectedTransactionIds.size} 项",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onToggleSelection) {
                        Icon(Icons.Default.Close, contentDescription = "退出选择")
                    }
                },
                actions = {
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Default.SelectAll, contentDescription = "全选")
                    }
                    IconButton(
                        onClick = onDeleteSelected,
                        enabled = uiState.selectedTransactionIds.isNotEmpty()
                    ) {
                        Icon(
                            Icons.Default.Delete, 
                            contentDescription = "删除选中项",
                            tint = if (uiState.selectedTransactionIds.isNotEmpty()) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
        else -> {
            TopAppBar(
                title = { 
                    Text(
                        text = "记账",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "打开菜单")
                    }
                },
                actions = {
                    // 月份选择器
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            onMonthSelected(selectedMonth.minusMonths(1))
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                        }
                        
                        Text(
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        IconButton(onClick = { 
                            onMonthSelected(selectedMonth.plusMonths(1))
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                        }
                    }
                    
                    IconButton(onClick = onToggleSearch) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                }
            )
        }
    }
}

@Composable
private fun MonthlyOverviewBar(
    monthlyIncome: Double,
    monthlyExpense: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "收入",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "¥%.2f".format(monthlyIncome),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "¥%.2f".format(monthlyExpense),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "结余",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                val balance = monthlyIncome - monthlyExpense
                Text(
                    text = "¥%.2f".format(balance),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GroupingOptions(
    currentMode: GroupingMode,
    onModeSelected: (GroupingMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        GroupingMode.values().forEach { mode ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) },
                label = { 
                    Text(
                        text = when (mode) {
                            GroupingMode.NONE -> "不分组"
                            GroupingMode.DAY -> "按天"
                            GroupingMode.WEEK -> "按周"
                            GroupingMode.MONTH -> "按月"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun TransactionGroupHeader(
    group: TransactionGroup
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                group.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "收支 ¥%.2f".format(group.balanceYuan),
                style = MaterialTheme.typography.bodyMedium,
                color = if (group.balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TransactionListItem(
    transaction: TransactionItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                
                // 分类图标
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(android.graphics.Color.parseColor(transaction.categoryColor)).copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.categoryIcon ?: "💰",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    transaction.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = transaction.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "¥%.2f".format(transaction.amount),
                style = MaterialTheme.typography.titleMedium,
                color = if (transaction.categoryName.contains("收入")) 
                    Color(0xFF4CAF50) 
                else 
                    Color(0xFFF44336),
                fontWeight = FontWeight.Bold
            )
        }
    }
}