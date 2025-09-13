package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoTransaction
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.TransactionType
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoBottomNavItem
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.GroupingStrategy
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.SpecsRegistry
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoViewModel
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar
import java.time.LocalDate

/**
 * Demo主页面 - 交易列表
 * 展示不同风格下的列表布局差异
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoHomeScreen(
    navController: NavController,
    viewModel: DemoViewModel
) {
    // Home 顶部为浅色背景，状态栏随之设置为表面色
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    val currentStyle by viewModel.currentStyle.collectAsState()
    val currentDensity by viewModel.currentDensity.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val todayStats = viewModel.getTodayStats()
    val monthStats = viewModel.getMonthStats()
    
    // 获取当前风格的规格
    val specs = SpecsRegistry.getSpecs(currentStyle)
    val listSpec = specs.listSpec
    
    Scaffold(
        topBar = {
            // 根据不同风格渲染不同的顶部栏
            when (specs.headerSpec.getTitleStyle()) {
                com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.HeaderSpec.TitleStyle.LARGE -> {
                    LargeTopAppBar(
                        title = { 
                            Column {
                                Text("记账")
                                Text(
                                    text = currentStyle.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(DemoScreen.StyleSelector.route) }) {
                                Icon(Icons.Default.Palette, contentDescription = "切换风格")
                            }
                            IconButton(onClick = { navController.navigate(DemoScreen.Search.route) }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                        }
                    )
                }
                com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.HeaderSpec.TitleStyle.MEDIUM -> {
                    MediumTopAppBar(
                        title = { Text("记账") },
                        actions = {
                            IconButton(onClick = { navController.navigate(DemoScreen.StyleSelector.route) }) {
                                Icon(Icons.Default.Palette, contentDescription = "切换风格")
                            }
                            IconButton(onClick = { navController.navigate(DemoScreen.Search.route) }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                        }
                    )
                }
                else -> {
                    TopAppBar(
                        title = { Text("记账") },
                        actions = {
                            IconButton(onClick = { navController.navigate(DemoScreen.StyleSelector.route) }) {
                                Icon(Icons.Default.Palette, contentDescription = "切换风格")
                            }
                            IconButton(onClick = { navController.navigate(DemoScreen.Search.route) }) {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            DemoBottomNavigationBar(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(DemoScreen.AddTransaction.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加交易")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 概览卡片
            if (specs.headerSpec.showQuickStats()) {
                specs.headerSpec.RenderOverviewCard(
                    income = monthStats.totalIncome,
                    expense = monthStats.totalExpense,
                    balance = monthStats.balance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            
            // 交易列表容器
            listSpec.ListContainer(
                modifier = Modifier.fillMaxSize()
            ) {
                // 根据分组策略组织交易
                when (listSpec.getGroupingStrategy()) {
                    GroupingStrategy.BY_DAY -> {
                        RenderTransactionsByDay(
                            transactions = transactions,
                            listSpec = listSpec,
                            onItemClick = { transaction ->
                                navController.navigate(
                                    DemoScreen.EditTransaction.createRoute(transaction.id)
                                )
                            }
                        )
                    }
                    GroupingStrategy.BY_WEEK -> {
                        // TODO: 按周分组实现
                        RenderTransactionsList(transactions, listSpec) { transaction ->
                            navController.navigate(
                                DemoScreen.EditTransaction.createRoute(transaction.id)
                            )
                        }
                    }
                    GroupingStrategy.BY_MONTH -> {
                        // TODO: 按月分组实现
                        RenderTransactionsList(transactions, listSpec) { transaction ->
                            navController.navigate(
                                DemoScreen.EditTransaction.createRoute(transaction.id)
                            )
                        }
                    }
                    GroupingStrategy.BY_CATEGORY -> {
                        // TODO: 按分类分组实现
                        RenderTransactionsList(transactions, listSpec) { transaction ->
                            navController.navigate(
                                DemoScreen.EditTransaction.createRoute(transaction.id)
                            )
                        }
                    }
                    GroupingStrategy.NO_GROUPING -> {
                        RenderTransactionsList(transactions, listSpec) { transaction ->
                            navController.navigate(
                                DemoScreen.EditTransaction.createRoute(transaction.id)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 按日分组渲染交易列表
 */
@Composable
private fun RenderTransactionsByDay(
    transactions: List<DemoTransaction>,
    listSpec: com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.ListSpec,
    onItemClick: (DemoTransaction) -> Unit
) {
    // 按日期分组
    val groupedTransactions = transactions.groupBy { 
        it.toLocalDate()
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(listSpec.getItemSpacing(DemoDensity.Medium))
    ) {
        groupedTransactions.forEach { (date, dayTransactions) ->
            // 渲染分组头
            item(key = "header_$date") {
                val dayIncome = dayTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val dayExpense = dayTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                listSpec.RenderGroupHeader(
                    date = date,
                    totalIncome = dayIncome,
                    totalExpense = dayExpense,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 渲染该日的交易项
            items(
                items = dayTransactions,
                key = { it.id }
            ) { transaction ->
                listSpec.RenderListItem(
                    transaction = transaction,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onItemClick(transaction) },
                    onLongClick = { /* TODO: 长按操作 */ }
                )
            }
        }
    }
}

/**
 * 不分组渲染交易列表
 */
@Composable
private fun RenderTransactionsList(
    transactions: List<DemoTransaction>,
    listSpec: com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.spec.ListSpec,
    onItemClick: (DemoTransaction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(listSpec.getItemSpacing(DemoDensity.Medium))
    ) {
        items(
            items = transactions,
            key = { it.id }
        ) { transaction ->
            listSpec.RenderListItem(
                transaction = transaction,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onItemClick(transaction) },
                onLongClick = { /* TODO: 长按操作 */ }
            )
        }
    }
}

/**
 * Demo底部导航栏
 */
@Composable
fun DemoBottomNavigationBar(navController: NavController) {
    NavigationBar {
        DemoBottomNavItem.values().forEach { item ->
            NavigationBarItem(
                selected = false, // TODO: 实现选中状态
                onClick = { navController.navigate(item.screen.route) },
                icon = {
                    Icon(
                        imageVector = when (item.icon) {
                            "home" -> Icons.Default.Home
                            "chart" -> Icons.Default.BarChart
                            "category" -> Icons.Default.Category
                            "wallet" -> Icons.Default.AccountBalanceWallet
                            "settings" -> Icons.Default.Settings
                            else -> Icons.Default.Home
                        },
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
