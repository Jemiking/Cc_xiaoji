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
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.navigation.DemoScreen
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.*
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.viewmodel.DemoViewModel
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoDensity
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.DemoStyle
import com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.theme.SetStatusBar

// ==================== 添加交易页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoAddTransactionScreen(
    navController: NavController,
    viewModel: DemoViewModel
) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加交易") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TODO: 实现添加交易表单
            Text("添加交易表单（待实现）")
            
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("金额") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("分类") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}

// ==================== 编辑交易页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoEditTransactionScreen(
    navController: NavController,
    transactionId: String,
    viewModel: DemoViewModel
) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    val transaction = viewModel.getTransactionById(transactionId)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑交易") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.deleteTransaction(transactionId)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            transaction?.let {
                Text("编辑交易: ${it.category?.name}")
                Text("金额: ¥${it.amount}")
                Text("账户: ${it.account?.name}")
                Text("备注: ${it.note}")
            } ?: Text("交易不存在")
        }
    }
}

// ==================== 统计页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoStatisticsScreen(
    navController: NavController,
    viewModel: DemoViewModel
) {
    val monthStats = viewModel.getMonthStats()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("统计分析") })
        },
        bottomBar = {
            DemoBottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("本月统计", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("收入: ¥${monthStats.totalIncome}")
                    Text("支出: ¥${monthStats.totalExpense}")
                    Text("结余: ¥${monthStats.balance}")
                    Text("交易笔数: ${monthStats.transactionCount}")
                }
            }
            
            // TODO: 添加图表
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("图表区域（待实现）")
                }
            }
        }
    }
}

// ==================== 风格选择器页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoStyleSelectorScreen(
    navController: NavController,
    viewModel: DemoViewModel
) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    val currentStyle by viewModel.currentStyle.collectAsState()
    val currentDensity by viewModel.currentDensity.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("风格设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 参考样式预览入口（Expense Tracker 1:1）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("参考样式预览", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "ExpenseTracker（1:1复刻）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { navController.navigate(DemoScreen.ExpenseTrackerPreview.route) }) {
                            Text("打开预览")
                        }
                    }
                }
            }
            // 深色模式开关
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("深色模式")
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleDarkMode() }
                        )
                    }
                }
            }
            
            // 密度选择
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("显示密度", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DemoDensity.entries.forEach { density ->
                                FilterChip(
                                    selected = currentDensity == density,
                                    onClick = { viewModel.setDensity(density) },
                                    label = { Text(density.name) }
                                )
                            }
                        }
                    }
                }
            }
            
            // 风格选择标题
            item {
                Text(
                    "选择风格",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 风格列表
            items(DemoStyle.values().toList()) { style ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (currentStyle == style) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        CardDefaults.cardColors()
                    },
                    onClick = { viewModel.setStyle(style) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (currentStyle == style) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "已选中",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 其他页面存根 ====================
@Composable
fun DemoChartsScreen(navController: NavController) {
    StubScreen("图表", navController, showBottomBar = true)
}

@Composable
fun DemoReportsScreen(navController: NavController) {
    StubScreen("报表", navController, showBottomBar = true)
}

@Composable
fun DemoCategoriesScreen(navController: NavController) {
    StubScreen("分类管理", navController, showBottomBar = true)
}

@Composable
fun DemoAccountsScreen(navController: NavController) {
    StubScreen("账户管理", navController, showBottomBar = true)
}

@Composable
fun DemoBudgetScreen(navController: NavController) {
    StubScreen("预算管理", navController)
}

@Composable
fun DemoTagsScreen(navController: NavController) {
    StubScreen("标签管理", navController)
}

@Composable
fun DemoSettingsScreen(navController: NavController) {
    StubScreen("设置", navController, showBottomBar = true)
}

@Composable
fun DemoDensitySettingsScreen(navController: NavController) {
    StubScreen("密度设置", navController)
}

@Composable
fun DemoAboutScreen(navController: NavController) {
    StubScreen("关于", navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoSettingsAboutScreen(navController: NavController) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置·关于") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "设置·关于 页面",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "待实现",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DemoImportScreen(navController: NavController) {
    StubScreen("导入数据", navController)
}

@Composable
fun DemoExportScreen(navController: NavController) {
    StubScreen("导出数据", navController)
}

@Composable
fun DemoSearchScreen(navController: NavController) {
    StubScreen("搜索", navController)
}

// ==================== 通用存根页面 ====================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StubScreen(
    title: String,
    navController: NavController,
    showBottomBar: Boolean = false
) {
    SetStatusBar(color = MaterialTheme.colorScheme.surface)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = if (!showBottomBar) {
                    {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                } else {
                    { /* Empty */ }
                }
            )
        },
        bottomBar = if (showBottomBar) {
            { DemoBottomNavigationBar(navController) }
        } else {
            { /* Empty */ }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$title 页面",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "待实现",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
