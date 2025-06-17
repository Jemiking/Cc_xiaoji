package com.ccxiaoji.app.presentation.ui.ledger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.R
import androidx.compose.foundation.background
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.Category
import com.ccxiaoji.app.domain.model.CategoryDetails
import com.ccxiaoji.app.domain.model.Transaction
import com.ccxiaoji.app.presentation.ui.components.AccountSelector
import com.ccxiaoji.app.presentation.ui.ledger.components.LedgerDrawerContent
import com.ccxiaoji.app.presentation.ui.ledger.components.MonthlyOverviewBar
import com.ccxiaoji.app.presentation.ui.ledger.components.MonthSelector
import com.ccxiaoji.app.presentation.ui.navigation.*
import com.ccxiaoji.app.presentation.viewmodel.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    navController: androidx.navigation.NavController? = null,
    accountId: String? = null,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    // 如果传入了accountId，则设置账户筛选
    LaunchedEffect(accountId) {
        accountId?.let {
            viewModel.filterByAccount(it)
        }
    }
    val uiState by viewModel.uiState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentAccount = uiState.selectedAccount
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LedgerDrawerContent(
                currentAccountName = currentAccount?.name ?: "默认账户",
                onNavigateToStatistics = {
                    navController?.navigate(StatisticsRoute.route)
                },
                onNavigateToAssetOverview = {
                    // TODO: 实现资产总览页面
                },
                onNavigateToAccountManagement = {
                    navController?.navigate(AccountManagementRoute.route)
                },
                onNavigateToCategoryManagement = {
                    navController?.navigate(CategoryManagementRoute.route)
                },
                onNavigateToRecurringTransaction = {
                    navController?.navigate(RecurringTransactionRoute.route)
                },
                onNavigateToBudget = {
                    navController?.navigate(BudgetRoute.route)
                },
                onNavigateToSavingsGoal = {
                    navController?.navigate(SavingsGoalRoute.route)
                },
                onNavigateToCreditCard = {
                    navController?.navigate(CreditCardRoute.route)
                },
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
            if (uiState.isSearchMode) {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
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
                            viewModel.toggleSearchMode()
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "退出搜索")
                        }
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除搜索")
                            }
                        }
                    }
                )
            } else if (uiState.isSelectionMode) {
                TopAppBar(
                    title = { 
                        Text(
                            text = "已选择 ${uiState.selectedTransactionIds.size} 项",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.toggleSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "退出选择")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAllTransactions() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "全选")
                        }
                        IconButton(
                            onClick = { viewModel.deleteSelectedTransactions() },
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
            } else {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = "记账",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            if (uiState.activeFilter.accountId != null) {
                                val accountName = uiState.accounts.find { it.id == uiState.activeFilter.accountId }?.name ?: ""
                                Text(
                                    text = "筛选账户：$accountName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "打开菜单")
                        }
                    },
                    actions = {
                        // 月份选择器
                        MonthSelector(
                            currentMonth = selectedMonth,
                            onMonthSelected = { viewModel.selectMonth(it) }
                        )
                        
                        IconButton(onClick = { viewModel.toggleSearchMode() }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode && !uiState.isSearchMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 本月收支概览条
            MonthlyOverviewBar(
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpense = uiState.monthlyExpense
            )
            
            Divider()
            
            // Budget Alert
            uiState.budgetAlert?.let { alert ->
                BudgetAlertCard(
                    alert = alert,
                    onDismiss = { viewModel.dismissBudgetAlert() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Transactions List
            val displayTransactions = if (uiState.isSearchMode && uiState.searchQuery.isNotEmpty()) {
                uiState.filteredTransactions
            } else {
                uiState.transactions
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.isSearchMode && uiState.searchQuery.isNotEmpty() && displayTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "没有找到相关交易记录",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                items(displayTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        isSelected = uiState.selectedTransactionIds.contains(transaction.id),
                        isSelectionMode = uiState.isSelectionMode,
                        onEdit = { 
                            viewModel.setEditingTransaction(transaction)
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteTransaction(transaction.id) },
                        onCopy = { viewModel.copyTransaction(transaction) },
                        onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.toggleTransactionSelection(transaction.id)
                            } else {
                                navController?.navigate(
                                    TransactionDetailRoute.createRoute(transaction.id)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddTransactionDialog(
            accounts = uiState.accounts,
            selectedAccount = uiState.selectedAccount,
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, categoryId, note, accountId ->
                viewModel.addTransaction(amount, categoryId, note, accountId)
                showAddDialog = false
            }
        )
    }
    
    if (showEditDialog && uiState.editingTransaction != null) {
        EditTransactionDialog(
            transaction = uiState.editingTransaction!!,
            categories = uiState.categories,
            onDismiss = { 
                showEditDialog = false
                viewModel.setEditingTransaction(null)
            },
            onConfirm = { updatedTransaction ->
                viewModel.updateTransaction(updatedTransaction)
                showEditDialog = false
                viewModel.setEditingTransaction(null)
            }
        )
    }
    
    if (uiState.showFilterDialog) {
        FilterTransactionDialog(
            currentFilter = uiState.activeFilter,
            categories = uiState.categories,
            onDismiss = { viewModel.toggleFilterDialog() },
            onConfirm = { filter ->
                viewModel.updateFilter(filter)
                viewModel.toggleFilterDialog()
            },
            onClearFilter = {
                viewModel.clearFilter()
                viewModel.toggleFilterDialog()
            }
        )
    }
    }
}

@Composable
fun MonthlySummaryCard(
    monthlyIncome: Double,
    monthlyExpense: Double,
    hasActiveFilter: Boolean = false,
    groupingMode: GroupingMode = GroupingMode.NONE,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.monthly_summary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (groupingMode != GroupingMode.NONE) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = when (groupingMode) {
                                    GroupingMode.DAY -> "按天"
                                    GroupingMode.WEEK -> "按周"
                                    GroupingMode.MONTH -> "按月"
                                    GroupingMode.YEAR -> "按年"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (hasActiveFilter) {
                        Text(
                            text = "已筛选",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¥%.2f".format(monthlyIncome),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.income),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¥%.2f".format(monthlyExpense),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.expense),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¥%.2f".format(monthlyIncome - monthlyExpense),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "结余",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit = {},
    onClick: () -> Unit = onEdit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (!isSelectionMode) {
                            showMenu = true
                        }
                    }
                ),
            colors = if (isSelected) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            } else {
                CardDefaults.cardColors()
            }
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by card onClick
                    )
                }
                
                Text(
                    text = transaction.categoryDetails?.icon ?: "📝",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Column {
                    Text(
                        text = transaction.categoryDetails?.name ?: "未分类",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    transaction.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = transaction.createdAt.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = if (transaction.categoryDetails?.type == "INCOME") {
                    "+¥%.2f".format(transaction.amountYuan)
                } else {
                    "-¥%.2f".format(transaction.amountYuan)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (transaction.categoryDetails?.type == "INCOME") {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("复制") },
                onClick = {
                    onCopy()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.FileCopy, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("编辑") },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<Account>,
    selectedAccount: Account?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (amountCents: Int, categoryId: String, note: String?, accountId: String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var currentAccount by remember { mutableStateOf(selectedAccount) }
    
    // Filter categories by type
    val filteredCategories = remember(isIncome, categories) {
        categories.filter { it.type == if (isIncome) Category.Type.INCOME else Category.Type.EXPENSE }
    }
    
    // Auto-select first category if none selected
    LaunchedEffect(filteredCategories, selectedCategoryId) {
        if (selectedCategoryId == null && filteredCategories.isNotEmpty()) {
            selectedCategoryId = filteredCategories.first().id
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_transaction)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Selection
                if (accounts.isNotEmpty()) {
                    AccountSelector(
                        accounts = accounts,
                        selectedAccount = currentAccount,
                        onAccountSelected = { currentAccount = it },
                        label = "选择账户"
                    )
                }
                
                // Income/Expense Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isIncome,
                        onClick = { isIncome = false },
                        label = { Text(stringResource(R.string.expense)) }
                    )
                    FilterChip(
                        selected = isIncome,
                        onClick = { isIncome = true },
                        label = { Text(stringResource(R.string.income)) }
                    )
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(R.string.amount_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Selection
                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.labelLarge
                )
                
                // Category grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                }
                
                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountCents = ((amount.toDoubleOrNull() ?: 0.0) * 100).toInt()
                    val categoryId = selectedCategoryId ?: return@TextButton
                    val accountId = currentAccount?.id ?: return@TextButton
                    onConfirm(amountCents, categoryId, note.ifEmpty { null }, accountId)
                },
                enabled = amount.isNotEmpty() && currentAccount != null && selectedCategoryId != null
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionDialog(
    transaction: Transaction,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf(transaction.amountYuan.toString()) }
    var selectedCategoryId by remember { mutableStateOf(transaction.categoryId) }
    var note by remember { mutableStateOf(transaction.note ?: "") }
    var isIncome by remember { mutableStateOf(
        transaction.categoryDetails?.type == "INCOME"
    ) }
    
    // Filter categories by type
    val filteredCategories = remember(isIncome, categories) {
        categories.filter { it.type == if (isIncome) Category.Type.INCOME else Category.Type.EXPENSE }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑交易") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Income/Expense Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isIncome,
                        onClick = { isIncome = false },
                        label = { Text(stringResource(R.string.expense)) }
                    )
                    FilterChip(
                        selected = isIncome,
                        onClick = { isIncome = true },
                        label = { Text(stringResource(R.string.income)) }
                    )
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text(stringResource(R.string.amount_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category Selection
                Text(
                    text = stringResource(R.string.select_category),
                    style = MaterialTheme.typography.labelLarge
                )
                
                // Category grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                }
                
                // Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountCents = ((amount.toDoubleOrNull() ?: 0.0) * 100).toInt()
                    val categoryId = selectedCategoryId
                    val updatedCategory = filteredCategories.find { it.id == categoryId }
                    val updatedTransaction = transaction.copy(
                        amountCents = amountCents,
                        categoryId = categoryId,
                        categoryDetails = updatedCategory?.let {
                            CategoryDetails(
                                id = it.id,
                                name = it.name,
                                icon = it.icon,
                                color = it.color,
                                type = it.type.name
                            )
                        },
                        note = note.ifEmpty { null }
                    )
                    onConfirm(updatedTransaction)
                },
                enabled = amount.isNotEmpty() && selectedCategoryId.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTransactionDialog(
    currentFilter: TransactionFilter,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionFilter) -> Unit,
    onClearFilter: () -> Unit
) {
    var transactionType by remember { mutableStateOf(currentFilter.transactionType) }
    var selectedCategoryIds by remember { mutableStateOf(currentFilter.categoryIds) }
    var minAmount by remember { mutableStateOf(currentFilter.minAmount?.toString() ?: "") }
    var maxAmount by remember { mutableStateOf(currentFilter.maxAmount?.toString() ?: "") }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var dateRange by remember { mutableStateOf(currentFilter.dateRange) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Date picker states
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateRange?.start?.let { 
            java.time.LocalDate.of(it.year, it.monthValue, it.dayOfMonth)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateRange?.end?.let { 
            java.time.LocalDate.of(it.year, it.monthValue, it.dayOfMonth)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选交易记录") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Filter
                Column {
                    Text(
                        text = "交易类型",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = transactionType == TransactionType.ALL,
                            onClick = { transactionType = TransactionType.ALL },
                            label = { Text("全部") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.INCOME,
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text("收入") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.EXPENSE,
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text("支出") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Category Filter
                Column {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    val filteredCategories = remember(transactionType, categories) {
                        when (transactionType) {
                            TransactionType.INCOME -> categories.filter { it.type == Category.Type.INCOME }
                            TransactionType.EXPENSE -> categories.filter { it.type == Category.Type.EXPENSE }
                            TransactionType.ALL -> categories
                        }
                    }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(filteredCategories) { category ->
                            FilterChip(
                                selected = selectedCategoryIds.contains(category.id),
                                onClick = {
                                    selectedCategoryIds = if (selectedCategoryIds.contains(category.id)) {
                                        selectedCategoryIds - category.id
                                    } else {
                                        selectedCategoryIds + category.id
                                    }
                                },
                                label = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(category.icon)
                                        Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Amount Range Filter
                Column {
                    Text(
                        text = "金额范围",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmount,
                            onValueChange = { minAmount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("最小金额") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxAmount,
                            onValueChange = { maxAmount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text("最大金额") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Date Range Filter
                Column {
                    Text(
                        text = "日期范围",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    // Quick date range selections
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                val today = java.time.LocalDate.now()
                                dateRange = DateRange(today, today)
                            },
                            label = { Text("今天") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfWeek = now.with(java.time.DayOfWeek.MONDAY)
                                dateRange = DateRange(startOfWeek, now)
                            },
                            label = { Text("本周") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfMonth = now.withDayOfMonth(1)
                                dateRange = DateRange(startOfMonth, now)
                            },
                            label = { Text("本月") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfYear = now.withDayOfYear(1)
                                dateRange = DateRange(startOfYear, now)
                            },
                            label = { Text("本年") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Custom date range selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateRange?.start?.toString() ?: "开始日期",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        OutlinedCard(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateRange?.end?.toString() ?: "结束日期",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    if (dateRange != null) {
                        TextButton(
                            onClick = { dateRange = null },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("清除日期筛选")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClearFilter) {
                    Text("清除筛选")
                }
                TextButton(
                    onClick = {
                        val filter = TransactionFilter(
                            transactionType = transactionType,
                            categoryIds = selectedCategoryIds,
                            minAmount = minAmount.toDoubleOrNull(),
                            maxAmount = maxAmount.toDoubleOrNull(),
                            dateRange = dateRange
                        )
                        onConfirm(filter)
                    }
                ) {
                    Text("应用")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // Start Date Picker Dialog
    if (showStartDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    val selectedDate = java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    dateRange = DateRange(
                        start = selectedDate,
                        end = dateRange?.end ?: selectedDate
                    )
                }
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            datePickerState = startDatePickerState
        )
    }
    
    // End Date Picker Dialog
    if (showEndDatePicker) {
        CustomDatePickerDialog(
            onDateSelected = { millis ->
                millis?.let {
                    val selectedDate = java.time.Instant.ofEpochMilli(it)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    dateRange = DateRange(
                        start = dateRange?.start ?: selectedDate,
                        end = selectedDate
                    )
                }
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            datePickerState = endDatePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = false
        )
    }
}

@Composable
fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            BorderStroke(2.dp, Color(android.graphics.Color.parseColor(category.color)))
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category.icon,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GroupHeader(
    title: String,
    subtitle: String? = null,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥%.2f".format(totalIncome),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Expense
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥%.2f".format(totalExpense),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥%.2f".format(balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "结余",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
fun BudgetAlertCard(
    alert: BudgetAlertInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isExceeded) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (alert.isExceeded) Icons.Default.Warning else Icons.Default.Info,
                contentDescription = null,
                tint = if (alert.isExceeded) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
