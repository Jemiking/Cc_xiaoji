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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.ccxiaoji.feature.ledger.presentation.component.StyleableMonthlyOverviewBar
import com.ccxiaoji.feature.ledger.presentation.component.StyleableComponentFactory
import com.ccxiaoji.feature.ledger.presentation.component.groupByDate
import com.ccxiaoji.feature.ledger.presentation.component.transactionItems
import com.ccxiaoji.feature.ledger.presentation.component.ledger.LedgerDrawerContent
import com.ccxiaoji.feature.ledger.presentation.screen.ledger.components.*
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.component.ledger.MonthSelector
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation
import com.ccxiaoji.feature.ledger.presentation.navigation.BatchUpdateCategoryRoute
import com.ccxiaoji.feature.ledger.presentation.navigation.BatchDeleteRoute
import com.ccxiaoji.feature.ledger.presentation.navigation.BatchUpdateAccountRoute
import com.ccxiaoji.feature.ledger.presentation.navigation.FilterTransactionRoute
import com.ccxiaoji.feature.ledger.presentation.viewmodel.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
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
    filterViewModel: FilterViewModel = hiltViewModel(),
    uiStyleViewModel: LedgerUIStyleViewModel = hiltViewModel()
) {
    // 收集所有 ViewModels 的状态
    val uiState by viewModel.uiState.collectAsState()
    val selectionState by selectionViewModel.selectionState.collectAsState()
    val searchState by searchViewModel.searchState.collectAsState()
    val uiStyleState by uiStyleViewModel.uiState.collectAsState()
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
    
    // 处理筛选页面返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val filterObserver = androidx.lifecycle.Observer<TransactionFilter> { filter ->
                if (filter != null) {
                    filterViewModel.updateFilter(filter)
                    savedStateHandle.remove<TransactionFilter>("filterResult")
                }
            }
            
            val clearObserver = androidx.lifecycle.Observer<Boolean> { cleared ->
                if (cleared == true) {
                    filterViewModel.clearFilter()
                    savedStateHandle.remove<Boolean>("filterCleared")
                }
            }
            
            savedStateHandle.getLiveData<TransactionFilter>("filterResult").observe(lifecycleOwner, filterObserver)
            savedStateHandle.getLiveData<Boolean>("filterCleared").observe(lifecycleOwner, clearObserver)
            
            onDispose {
                savedStateHandle.getLiveData<TransactionFilter>("filterResult").removeObserver(filterObserver)
                savedStateHandle.getLiveData<Boolean>("filterCleared").removeObserver(clearObserver)
            }
        }
    }
    
    // 同步交易数据到SearchViewModel
    LaunchedEffect(uiState.transactions) {
        searchViewModel.setSearchableTransactions(uiState.transactions)
    }
    
    val selectedMonth by viewModel.selectedMonth.collectAsState()
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
                onNavigateToLedgerSettings = {
                    navController?.navigate(LedgerNavigation.LedgerSettingsRoute)
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
                            
                            HorizontalDivider()
                            
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
                        
                        IconButton(onClick = { 
                            navController?.navigate(
                                FilterTransactionRoute.createRoute(
                                    accountId = filterState.activeFilter.accountId
                                )
                            )
                        }) {
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
                    onClick = { 
                        navController?.navigate(
                            LedgerNavigation.addTransactionRoute(currentAccount?.id)
                        )
                    },
                    containerColor = DesignTokens.BrandColors.Ledger,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 1.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction), tint = androidx.compose.ui.graphics.Color.White)
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
            StyleableMonthlyOverviewBar(
                monthlyIncome = uiState.monthlyIncome,
                monthlyExpense = uiState.monthlyExpense,
                currentStyle = uiStyleState.uiStyle,
                animationDurationMs = uiStyleState.animationDurationMs
            )
            
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
                
                // 根据UI风格显示交易列表
                val transactionGroups = displayTransactions.groupByDate()
                
                transactionItems(
                    transactionGroups = transactionGroups,
                    style = uiStyleState.uiStyle,
                    isSelectionMode = selectionState.isSelectionMode,
                    selectedTransactionIds = selectionState.selectedTransactionIds,
                    onItemClick = { transaction: Transaction ->
                        if (selectionState.isSelectionMode) {
                            selectionViewModel.toggleTransactionSelection(transaction.id)
                        }
                    },
                    onItemLongClick = { transaction: Transaction ->
                        if (!selectionState.isSelectionMode) {
                            selectionViewModel.toggleSelectionMode()
                            selectionViewModel.toggleTransactionSelection(transaction.id)
                        }
                    },
                    onEdit = { transaction: Transaction ->
                        navController?.navigate(
                            LedgerNavigation.editTransactionRoute(transaction.id)
                        )
                    },
                    onDelete = { transaction: Transaction ->
                        viewModel.deleteTransaction(transaction.id)
                    },
                    onCopy = { transaction: Transaction ->
                        viewModel.copyTransaction(transaction)
                    },
                    animationDurationMs = uiStyleState.animationDurationMs
                )
            }
        }
    }
    
    // 高级筛选对话框已改为页面导航
    
    // 批量删除对话框
    if (dialogState.showBatchDeleteDialog) {
        // 导航到批量删除确认页面
        LaunchedEffect(Unit) {
            navController?.navigate(
                BatchDeleteRoute.createRoute(
                    selectedCount = selectionState.selectedCount
                )
            )
            dialogViewModel.hideBatchDeleteDialog()
        }
    }
    
    // 批量修改分类对话框
    if (dialogState.showBatchCategoryDialog) {
        // 导航到批量修改分类页面
        LaunchedEffect(Unit) {
            navController?.navigate(
                BatchUpdateCategoryRoute.createRoute(
                    selectedCount = selectionState.selectedCount
                )
            )
            dialogViewModel.hideBatchCategoryDialog()
        }
    }
    
    // 处理批量删除返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val deleteObserver = androidx.lifecycle.Observer<Boolean> { confirmed ->
                if (confirmed == true) {
                    selectionViewModel.batchDeleteTransactions { successCount: Int, deletedIds: List<String> ->
                        // TODO: 显示成功提示，支持撤销
                        viewModel.refreshTransactions()
                    }
                    savedStateHandle.remove<Boolean>("batch_delete_confirmed")
                }
            }
            
            savedStateHandle.getLiveData<Boolean>("batch_delete_confirmed").observe(lifecycleOwner, deleteObserver)
            
            onDispose {
                savedStateHandle.getLiveData<Boolean>("batch_delete_confirmed").removeObserver(deleteObserver)
            }
        }
    }
    
    // 处理批量修改分类返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val categoryObserver = androidx.lifecycle.Observer<String> { categoryId ->
                if (categoryId != null) {
                    selectionViewModel.batchUpdateCategory(categoryId) { successCount: Int ->
                        // TODO: 显示成功提示
                        viewModel.refreshTransactions()
                    }
                    savedStateHandle.remove<String>("selected_category_id")
                }
            }
            
            savedStateHandle.getLiveData<String>("selected_category_id").observe(lifecycleOwner, categoryObserver)
            
            onDispose {
                savedStateHandle.getLiveData<String>("selected_category_id").removeObserver(categoryObserver)
            }
        }
    }
    
    // 批量修改账户对话框
    if (dialogState.showBatchAccountDialog) {
        // 导航到批量修改账户页面
        LaunchedEffect(Unit) {
            navController?.navigate(
                BatchUpdateAccountRoute.createRoute(
                    selectedCount = selectionState.selectedCount
                )
            )
            dialogViewModel.hideBatchAccountDialog()
        }
    }
    
    // 处理批量修改账户返回结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val accountObserver = androidx.lifecycle.Observer<String> { accountId ->
                if (accountId != null) {
                    selectionViewModel.batchUpdateAccount(accountId) { successCount: Int ->
                        // TODO: 显示成功提示
                        viewModel.refreshTransactions()
                    }
                    savedStateHandle.remove<String>("selected_account_id")
                }
            }
            
            savedStateHandle.getLiveData<String>("selected_account_id").observe(lifecycleOwner, accountObserver)
            
            onDispose {
                savedStateHandle.getLiveData<String>("selected_account_id").removeObserver(accountObserver)
            }
        }
    }
    }
}

