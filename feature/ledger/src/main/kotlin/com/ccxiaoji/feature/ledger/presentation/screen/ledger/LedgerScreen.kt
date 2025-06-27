package com.ccxiaoji.feature.ledger.presentation.screen.ledger

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
import com.ccxiaoji.feature.ledger.R
import androidx.compose.foundation.background
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.CategoryDetails
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.component.AccountSelector
import com.ccxiaoji.feature.ledger.presentation.component.BatchDeleteDialog
import com.ccxiaoji.feature.ledger.presentation.component.BatchUpdateCategoryDialog
import com.ccxiaoji.feature.ledger.presentation.component.BatchUpdateAccountDialog
import com.ccxiaoji.feature.ledger.presentation.component.AdvancedFilterDialog
import com.ccxiaoji.feature.ledger.presentation.component.ledger.LedgerDrawerContent
import com.ccxiaoji.feature.ledger.presentation.component.ledger.MonthlyOverviewBar
import com.ccxiaoji.feature.ledger.presentation.component.ledger.MonthSelector
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation
import com.ccxiaoji.feature.ledger.presentation.viewmodel.*
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
    viewModel: LedgerViewModel = hiltViewModel(),
    selectionViewModel: SelectionViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    dialogViewModel: DialogViewModel = hiltViewModel(),
    filterViewModel: FilterViewModel = hiltViewModel()
) {
    // 收集所有 ViewModels 的状态
    val uiState by viewModel.uiState.collectAsState()
    val selectionState by selectionViewModel.selectionState.collectAsState()
    val searchState by searchViewModel.searchState.collectAsState()
    val dialogState by dialogViewModel.dialogState.collectAsState()
    val filterState by filterViewModel.filterState.collectAsState()
    
    // 如果传入了accountId，则设置账户筛选
    LaunchedEffect(accountId) {
        accountId?.let {
            filterViewModel.updateFilter(
                filterState.activeFilter.copy(accountId = it)
            )
        }
    }
    
    // 同步交易数据到SearchViewModel
    LaunchedEffect(uiState.transactions) {
        searchViewModel.setSearchableTransactions(uiState.transactions)
    }
    
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val currentAccount = uiState.accounts.find { it.id == uiState.selectedAccountId }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LedgerDrawerContent(
                currentAccountName = currentAccount?.name ?: stringResource(R.string.default_account),
                onNavigateToStatistics = {
                    navController?.navigate(LedgerNavigation.StatisticsRoute)
                },
                onNavigateToAssetOverview = {
                    navController?.navigate(LedgerNavigation.AssetOverviewRoute)
                },
                onNavigateToAccountManagement = {
                    navController?.navigate(LedgerNavigation.AccountManagementRoute)
                },
                onNavigateToCategoryManagement = {
                    navController?.navigate(LedgerNavigation.CategoryManagementRoute)
                },
                onNavigateToRecurringTransaction = {
                    navController?.navigate(LedgerNavigation.RecurringTransactionRoute)
                },
                onNavigateToBudget = {
                    navController?.navigate(LedgerNavigation.BudgetRoute)
                },
                onNavigateToSavingsGoal = {
                    navController?.navigate(LedgerNavigation.SavingsGoalRoute)
                },
                onNavigateToCreditCard = {
                    navController?.navigate(LedgerNavigation.CreditCardRoute)
                },
                onCloseDrawer = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        gesturesEnabled = !selectionState.isSelectionMode && !searchState.isSearchMode
    ) {
        Scaffold(
        topBar = {
            if (searchState.isSearchMode) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchState.searchQuery,
                            onValueChange = { searchViewModel.updateSearchQuery(it) },
                            placeholder = { Text(stringResource(R.string.search_transactions)) },
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
                            searchViewModel.toggleSearchMode()
                            searchViewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.exit_search))
                        }
                    },
                    actions = {
                        if (searchState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchViewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    }
                )
            } else if (selectionState.isSelectionMode) {
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.selected_items, selectionState.selectedTransactionIds.size),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectionViewModel.toggleSelectionMode() }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.exit_selection))
                        }
                    },
                    actions = {
                        var showBatchMenu by remember { mutableStateOf(false) }
                        
                        IconButton(onClick = { selectionViewModel.selectAllTransactions(uiState.transactions.map { it.id }) }) {
                            Icon(Icons.Default.SelectAll, contentDescription = stringResource(R.string.select_all))
                        }
                        
                        // 批量操作菜单
                        IconButton(
                            onClick = { showBatchMenu = true },
                            enabled = selectionState.selectedTransactionIds.isNotEmpty()
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.batch_operations))
                        }
                        
                        DropdownMenu(
                            expanded = showBatchMenu,
                            onDismissRequest = { showBatchMenu = false }
                        ) {
                            // 批量删除
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.batch_delete)) },
                                onClick = {
                                    showBatchMenu = false
                                    dialogViewModel.showBatchDeleteDialog()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                            
                            Divider()
                            
                            // 批量修改分类
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.batch_change_category)) },
                                onClick = {
                                    showBatchMenu = false
                                    dialogViewModel.showBatchCategoryDialog()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Category, contentDescription = null)
                                }
                            )
                            
                            // 批量修改账户
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.batch_change_account)) },
                                onClick = {
                                    showBatchMenu = false
                                    dialogViewModel.showBatchAccountDialog()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null)
                                }
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = stringResource(R.string.ledger),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            if (filterState.activeFilter.accountId != null) {
                                val accountName = uiState.accounts.find { it.id == filterState.activeFilter.accountId }?.name ?: ""
                                Text(
                                    text = stringResource(R.string.filter_account, accountName),
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
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu))
                        }
                    },
                    actions = {
                        // 月份选择器
                        MonthSelector(
                            currentMonth = selectedMonth,
                            onMonthSelected = { viewModel.selectMonth(it) }
                        )
                        
                        IconButton(onClick = { dialogViewModel.showFilterDialog() }) {
                            Icon(
                                Icons.Default.FilterList, 
                                contentDescription = stringResource(R.string.filter),
                                tint = if (filterState.activeFilter != TransactionFilter()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        
                        IconButton(onClick = { searchViewModel.toggleSearchMode() }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!selectionState.isSelectionMode && !searchState.isSearchMode) {
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
            dialogState.budgetAlert?.let { alert ->
                BudgetAlertCard(
                    alert = alert,
                    onDismiss = { dialogViewModel.dismissBudgetAlert() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Transactions List
            val filteredTransactions = filterViewModel.applyFilter(uiState.transactions)
            val displayTransactions = if (searchState.isSearchMode && searchState.searchQuery.isNotEmpty()) {
                searchState.searchResults
            } else {
                filteredTransactions
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchState.isSearchMode && searchState.searchQuery.isNotEmpty() && displayTransactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_search_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                items(displayTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        isSelected = selectionState.selectedTransactionIds.contains(transaction.id),
                        isSelectionMode = selectionState.isSelectionMode,
                        onEdit = { 
                            dialogViewModel.showEditTransactionDialog(transaction)
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteTransaction(transaction.id) },
                        onCopy = { viewModel.copyTransaction(transaction) },
                        onClick = {
                            if (selectionState.isSelectionMode) {
                                selectionViewModel.toggleTransactionSelection(transaction.id)
                            } else {
                                navController?.navigate(
                                    LedgerNavigation.transactionDetailRoute(transaction.id)
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
            selectedAccount = currentAccount,
            categories = uiState.categories,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, categoryId, note, accountId ->
                viewModel.addTransaction(amount, categoryId, note, accountId)
                showAddDialog = false
            }
        )
    }
    
    if (showEditDialog && dialogState.editingTransaction != null) {
        EditTransactionDialog(
            transaction = dialogState.editingTransaction!!,
            categories = uiState.categories,
            onDismiss = { 
                showEditDialog = false
                dialogViewModel.hideEditTransactionDialog()
            },
            onConfirm = { updatedTransaction ->
                viewModel.updateTransaction(updatedTransaction)
                showEditDialog = false
                dialogViewModel.hideEditTransactionDialog()
            }
        )
    }
    
    if (dialogState.showFilterDialog) {
        AdvancedFilterDialog(
            currentFilter = filterState.activeFilter,
            categories = uiState.categories,
            accounts = uiState.accounts,
            filterPresets = filterViewModel.getFilterPresets(),
            onDismiss = { dialogViewModel.hideFilterDialog() },
            onConfirm = { filter ->
                filterViewModel.updateFilter(filter)
                dialogViewModel.hideFilterDialog()
            },
            onPresetSelected = { preset ->
                filterViewModel.applyPresetFilter(preset)
                dialogViewModel.hideFilterDialog()
            },
            onClearFilter = {
                filterViewModel.clearFilter()
                dialogViewModel.hideFilterDialog()
            }
        )
    }
    
    // 批量删除对话框
    if (dialogState.showBatchDeleteDialog) {
        BatchDeleteDialog(
            selectedCount = selectionState.selectedCount,
            onConfirm = {
                selectionViewModel.batchDeleteTransactions { successCount, deletedIds ->
                    // TODO: 显示成功提示，支持撤销
                    viewModel.refreshTransactions()
                }
            },
            onDismiss = { dialogViewModel.hideBatchDeleteDialog() }
        )
    }
    
    // 批量修改分类对话框
    if (dialogState.showBatchCategoryDialog) {
        BatchUpdateCategoryDialog(
            selectedCount = selectionState.selectedCount,
            categories = uiState.categories.filter { it.type == Category.Type.EXPENSE },
            onConfirm = { categoryId ->
                selectionViewModel.batchUpdateCategory(categoryId) { successCount ->
                    // TODO: 显示成功提示
                    viewModel.refreshTransactions()
                }
            },
            onDismiss = { dialogViewModel.hideBatchCategoryDialog() }
        )
    }
    
    // 批量修改账户对话框
    if (dialogState.showBatchAccountDialog) {
        BatchUpdateAccountDialog(
            selectedCount = selectionState.selectedCount,
            accounts = uiState.accounts,
            onConfirm = { accountId ->
                selectionViewModel.batchUpdateAccount(accountId) { successCount ->
                    // TODO: 显示成功提示
                    viewModel.refreshTransactions()
                }
            },
            onDismiss = { dialogViewModel.hideBatchAccountDialog() }
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
                                    GroupingMode.DAY -> stringResource(R.string.group_by_day)
                                    GroupingMode.WEEK -> stringResource(R.string.group_by_week)
                                    GroupingMode.MONTH -> stringResource(R.string.group_by_month)
                                    GroupingMode.YEAR -> stringResource(R.string.group_by_year)
                                    else -> ""
                                },
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (hasActiveFilter) {
                        Text(
                            text = stringResource(R.string.filtered),
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
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), monthlyIncome),
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
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), monthlyExpense),
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
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), monthlyIncome - monthlyExpense),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.balance),
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
                        text = transaction.categoryDetails?.name ?: stringResource(R.string.uncategorized),
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
                    stringResource(R.string.amount_format_positive, stringResource(R.string.currency_symbol), transaction.amountYuan)
                } else {
                    stringResource(R.string.amount_format_negative, stringResource(R.string.currency_symbol), transaction.amountYuan)
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
                text = { Text(stringResource(R.string.copy)) },
                onClick = {
                    onCopy()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.FileCopy, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
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
                        label = stringResource(R.string.select_account)
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
        title = { Text(stringResource(R.string.edit_transaction)) },
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
        initialSelectedDateMillis = dateRange?.startDate?.let { 
            java.time.LocalDate.of(it.year, it.monthValue, it.dayOfMonth)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateRange?.endDate?.let { 
            java.time.LocalDate.of(it.year, it.monthValue, it.dayOfMonth)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filter_transactions)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Filter
                Column {
                    Text(
                        text = stringResource(R.string.transaction_type),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = transactionType == TransactionType.ALL,
                            onClick = { transactionType = TransactionType.ALL },
                            label = { Text(stringResource(R.string.all)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.INCOME,
                            onClick = { transactionType = TransactionType.INCOME },
                            label = { Text(stringResource(R.string.income)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = transactionType == TransactionType.EXPENSE,
                            onClick = { transactionType = TransactionType.EXPENSE },
                            label = { Text(stringResource(R.string.expense)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Category Filter
                Column {
                    Text(
                        text = stringResource(R.string.category),
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
                        text = stringResource(R.string.amount_range),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minAmount,
                            onValueChange = { minAmount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text(stringResource(R.string.min_amount)) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxAmount,
                            onValueChange = { maxAmount = it.filter { char -> char.isDigit() || char == '.' } },
                            label = { Text(stringResource(R.string.max_amount)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Date Range Filter
                Column {
                    Text(
                        text = stringResource(R.string.date_range),
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
                            label = { Text(stringResource(R.string.today)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfWeek = now.with(java.time.DayOfWeek.MONDAY)
                                dateRange = DateRange(startOfWeek, now)
                            },
                            label = { Text(stringResource(R.string.this_week)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfMonth = now.withDayOfMonth(1)
                                dateRange = DateRange(startOfMonth, now)
                            },
                            label = { Text(stringResource(R.string.this_month)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val now = java.time.LocalDate.now()
                                val startOfYear = now.withDayOfYear(1)
                                dateRange = DateRange(startOfYear, now)
                            },
                            label = { Text(stringResource(R.string.this_year)) },
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
                                    text = dateRange?.startDate?.toString() ?: stringResource(R.string.start_date),
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
                                    text = dateRange?.endDate?.toString() ?: stringResource(R.string.end_date),
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
                            Text(stringResource(R.string.clear_date_filter))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClearFilter) {
                    Text(stringResource(R.string.clear_filter))
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
                    Text(stringResource(R.string.apply))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
                        startDate = selectedDate,
                        endDate = dateRange?.endDate ?: selectedDate
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
                        startDate = dateRange?.startDate ?: selectedDate,
                        endDate = selectedDate
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
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), totalIncome),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.income),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Expense
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), totalExpense),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.expense),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.amount_format, stringResource(R.string.currency_symbol), balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = stringResource(R.string.balance),
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
                    contentDescription = stringResource(R.string.close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}