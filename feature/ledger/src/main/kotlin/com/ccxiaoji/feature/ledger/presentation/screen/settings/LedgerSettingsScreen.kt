package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.presentation.viewmodel.LedgerSettingsViewModel
import kotlinx.coroutines.launch

/**
 * 记账设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onNavigateToBudgetManagement: () -> Unit,
    onNavigateToDataExport: () -> Unit,
    onNavigateToRecurringTransactions: () -> Unit,
    viewModel: LedgerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.resetAllSettings()
                            scope.launch {
                                snackbarHostState.showSnackbar("已重置所有设置")
                            }
                        }
                    ) {
                        Text("重置")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // 基础设置部分
            item {
                SettingSectionHeader(title = "基础设置")
            }
            
            item {
                BasicSettingsSection(
                    basicSettings = settings.basicSettings,
                    accounts = uiState.accounts,
                    onUpdateBasicSettings = viewModel::updateBasicSettings
                )
            }
            
            // 高级设置部分
            item {
                SettingSectionHeader(title = "高级设置")
            }
            
            item {
                AdvancedSettingsSection(
                    advancedSettings = settings.advancedSettings,
                    onUpdateAdvancedSettings = viewModel::updateAdvancedSettings,
                    onNavigateToCategoryManagement = onNavigateToCategoryManagement,
                    onNavigateToAccountManagement = onNavigateToAccountManagement,
                    onNavigateToBudgetManagement = onNavigateToBudgetManagement,
                    onNavigateToDataExport = onNavigateToDataExport
                )
            }
            
            // 自动化设置部分
            item {
                SettingSectionHeader(title = "自动化设置")
            }
            
            item {
                AutomationSettingsSection(
                    automationSettings = settings.automationSettings,
                    onUpdateAutomationSettings = viewModel::updateAutomationSettings,
                    onNavigateToRecurringTransactions = onNavigateToRecurringTransactions
                )
            }
        }
    }
}

/**
 * 设置部分标题
 */
@Composable
private fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 基础设置部分
 */
@Composable
private fun BasicSettingsSection(
    basicSettings: BasicSettings,
    accounts: List<Account>,
    onUpdateBasicSettings: (BasicSettings) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 默认账户选择
            var showAccountDialog by remember { mutableStateOf(false) }
            val selectedAccount = accounts.find { it.id == basicSettings.defaultAccountId?.toString() }
            
            SettingItem(
                icon = Icons.Default.AccountBalance,
                title = "默认账户",
                subtitle = selectedAccount?.name ?: "未选择",
                onClick = { showAccountDialog = true }
            )
            
            if (showAccountDialog) {
                AccountSelectionDialog(
                    accounts = accounts,
                    selectedAccountId = basicSettings.defaultAccountId,
                    onAccountSelected = { accountId ->
                        onUpdateBasicSettings(basicSettings.copy(defaultAccountId = accountId))
                        showAccountDialog = false
                    },
                    onDismiss = { showAccountDialog = false }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 默认币种
            var showCurrencyDialog by remember { mutableStateOf(false) }
            
            SettingItem(
                icon = Icons.Default.AttachMoney,
                title = "默认币种",
                subtitle = basicSettings.defaultCurrency,
                onClick = { showCurrencyDialog = true }
            )
            
            if (showCurrencyDialog) {
                CurrencySelectionDialog(
                    selectedCurrency = basicSettings.defaultCurrency,
                    onCurrencySelected = { currency ->
                        onUpdateBasicSettings(basicSettings.copy(defaultCurrency = currency))
                        showCurrencyDialog = false
                    },
                    onDismiss = { showCurrencyDialog = false }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 首页显示设置
            var showHomeDisplayDialog by remember { mutableStateOf(false) }
            
            SettingItem(
                icon = Icons.Default.Dashboard,
                title = "首页显示设置",
                subtitle = "自定义首页显示内容",
                onClick = { showHomeDisplayDialog = true }
            )
            
            if (showHomeDisplayDialog) {
                HomeDisplaySettingsDialog(
                    homeDisplaySettings = basicSettings.homeDisplaySettings,
                    onUpdateSettings = { settings ->
                        onUpdateBasicSettings(basicSettings.copy(homeDisplaySettings = settings))
                    },
                    onDismiss = { showHomeDisplayDialog = false }
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 记账提醒
            var showReminderDialog by remember { mutableStateOf(false) }
            
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "记账提醒",
                subtitle = if (basicSettings.reminderSettings.enableDailyReminder) 
                    "每日 ${basicSettings.reminderSettings.dailyReminderTime}" else "已关闭",
                onClick = { showReminderDialog = true }
            )
            
            if (showReminderDialog) {
                ReminderSettingsDialog(
                    reminderSettings = basicSettings.reminderSettings,
                    onUpdateSettings = { settings ->
                        onUpdateBasicSettings(basicSettings.copy(reminderSettings = settings))
                    },
                    onDismiss = { showReminderDialog = false }
                )
            }
        }
    }
}

/**
 * 高级设置部分
 */
@Composable
private fun AdvancedSettingsSection(
    advancedSettings: AdvancedSettings,
    onUpdateAdvancedSettings: (AdvancedSettings) -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToAccountManagement: () -> Unit,
    onNavigateToBudgetManagement: () -> Unit,
    onNavigateToDataExport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 分类管理
            SettingItem(
                icon = Icons.Default.Category,
                title = "分类管理",
                subtitle = "管理收支分类",
                onClick = onNavigateToCategoryManagement
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 账户管理
            SettingItem(
                icon = Icons.Default.AccountBalanceWallet,
                title = "账户管理",
                subtitle = "管理账户信息",
                onClick = onNavigateToAccountManagement
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 预算管理
            SettingItem(
                icon = Icons.Default.Savings,
                title = "预算管理",
                subtitle = "设置和管理预算",
                onClick = onNavigateToBudgetManagement
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 数据导入导出
            SettingItem(
                icon = Icons.Default.ImportExport,
                title = "数据导入导出",
                subtitle = "导入或导出记账数据",
                onClick = onNavigateToDataExport
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 小数位数设置
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Pin,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text("小数位数", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${advancedSettings.decimalPlaces} 位",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (advancedSettings.decimalPlaces > 0) {
                                onUpdateAdvancedSettings(
                                    advancedSettings.copy(decimalPlaces = advancedSettings.decimalPlaces - 1)
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "减少")
                    }
                    Text(advancedSettings.decimalPlaces.toString())
                    IconButton(
                        onClick = {
                            if (advancedSettings.decimalPlaces < 4) {
                                onUpdateAdvancedSettings(
                                    advancedSettings.copy(decimalPlaces = advancedSettings.decimalPlaces + 1)
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "增加")
                    }
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 显示图标开关
            SwitchSettingItem(
                icon = Icons.Default.Image,
                title = "显示分类图标",
                checked = advancedSettings.enableCategoryIcons,
                onCheckedChange = { checked ->
                    onUpdateAdvancedSettings(advancedSettings.copy(enableCategoryIcons = checked))
                }
            )
            
            SwitchSettingItem(
                icon = Icons.Default.AccountCircle,
                title = "显示账户图标",
                checked = advancedSettings.enableAccountIcons,
                onCheckedChange = { checked ->
                    onUpdateAdvancedSettings(advancedSettings.copy(enableAccountIcons = checked))
                }
            )
        }
    }
}

/**
 * 自动化设置部分
 */
@Composable
private fun AutomationSettingsSection(
    automationSettings: AutomationSettings,
    onUpdateAutomationSettings: (AutomationSettings) -> Unit,
    onNavigateToRecurringTransactions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 定期交易管理
            SettingItem(
                icon = Icons.Default.Repeat,
                title = "定期交易管理",
                subtitle = "管理自动记账规则",
                onClick = onNavigateToRecurringTransactions
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 智能分类开关
            SwitchSettingItem(
                icon = Icons.Default.AutoAwesome,
                title = "智能分类",
                subtitle = "根据描述自动选择分类",
                checked = automationSettings.enableSmartCategorization,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableSmartCategorization = checked))
                }
            )
            
            // 智能记账建议开关
            SwitchSettingItem(
                icon = Icons.Default.Lightbulb,
                title = "智能记账建议",
                subtitle = "基于历史记录提供建议",
                checked = automationSettings.enableSmartSuggestions,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableSmartSuggestions = checked))
                }
            )
            
            // 自动创建定期交易开关
            SwitchSettingItem(
                icon = Icons.Default.Schedule,
                title = "自动创建定期交易",
                subtitle = "到期自动创建定期交易",
                checked = automationSettings.enableAutoRecurring,
                onCheckedChange = { checked ->
                    onUpdateAutomationSettings(automationSettings.copy(enableAutoRecurring = checked))
                }
            )
        }
    }
}

/**
 * 设置项组件
 */
@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 开关设置项组件
 */
@Composable
private fun SwitchSettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * 账户选择对话框
 */
@Composable
private fun AccountSelectionDialog(
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择默认账户") },
        text = {
            LazyColumn {
                items(accounts) { account ->
                    ListItem(
                        headlineContent = { Text(account.name) },
                        supportingContent = { Text("余额：¥${account.balanceYuan}") },
                        leadingContent = {
                            RadioButton(
                                selected = account.id == selectedAccountId?.toString(),
                                onClick = { onAccountSelected(account.id.toLong()) }
                            )
                        },
                        modifier = Modifier.clickable {
                            onAccountSelected(account.id.toLong())
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 币种选择对话框
 */
@Composable
private fun CurrencySelectionDialog(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf("CNY", "USD", "EUR", "JPY", "GBP", "HKD", "TWD")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择默认币种") },
        text = {
            LazyColumn {
                items(currencies) { currency ->
                    ListItem(
                        headlineContent = { Text(currency) },
                        leadingContent = {
                            RadioButton(
                                selected = currency == selectedCurrency,
                                onClick = { onCurrencySelected(currency) }
                            )
                        },
                        modifier = Modifier.clickable {
                            onCurrencySelected(currency)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 首页显示设置对话框
 */
@Composable
private fun HomeDisplaySettingsDialog(
    homeDisplaySettings: HomeDisplaySettings,
    onUpdateSettings: (HomeDisplaySettings) -> Unit,
    onDismiss: () -> Unit
) {
    var settings by remember { mutableStateOf(homeDisplaySettings) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("首页显示设置") },
        text = {
            Column {
                SwitchListItem(
                    title = "显示今日支出",
                    checked = settings.showTodayExpense,
                    onCheckedChange = { settings = settings.copy(showTodayExpense = it) }
                )
                SwitchListItem(
                    title = "显示今日收入",
                    checked = settings.showTodayIncome,
                    onCheckedChange = { settings = settings.copy(showTodayIncome = it) }
                )
                SwitchListItem(
                    title = "显示本月支出",
                    checked = settings.showMonthExpense,
                    onCheckedChange = { settings = settings.copy(showMonthExpense = it) }
                )
                SwitchListItem(
                    title = "显示本月收入",
                    checked = settings.showMonthIncome,
                    onCheckedChange = { settings = settings.copy(showMonthIncome = it) }
                )
                SwitchListItem(
                    title = "显示账户余额",
                    checked = settings.showAccountBalance,
                    onCheckedChange = { settings = settings.copy(showAccountBalance = it) }
                )
                SwitchListItem(
                    title = "显示预算进度",
                    checked = settings.showBudgetProgress,
                    onCheckedChange = { settings = settings.copy(showBudgetProgress = it) }
                )
                SwitchListItem(
                    title = "显示最近交易",
                    checked = settings.showRecentTransactions,
                    onCheckedChange = { settings = settings.copy(showRecentTransactions = it) }
                )
                
                if (settings.showRecentTransactions) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("最近交易数量")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (settings.recentTransactionCount > 1) {
                                        settings = settings.copy(recentTransactionCount = settings.recentTransactionCount - 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "减少")
                            }
                            Text(settings.recentTransactionCount.toString())
                            IconButton(
                                onClick = {
                                    if (settings.recentTransactionCount < 10) {
                                        settings = settings.copy(recentTransactionCount = settings.recentTransactionCount + 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "增加")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdateSettings(settings)
                    onDismiss()
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
    )
}

/**
 * 提醒设置对话框
 */
@Composable
private fun ReminderSettingsDialog(
    reminderSettings: ReminderSettings,
    onUpdateSettings: (ReminderSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var settings by remember { mutableStateOf(reminderSettings) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记账提醒设置") },
        text = {
            Column {
                SwitchListItem(
                    title = "每日记账提醒",
                    checked = settings.enableDailyReminder,
                    onCheckedChange = { settings = settings.copy(enableDailyReminder = it) }
                )
                
                if (settings.enableDailyReminder) {
                    ListItem(
                        headlineContent = { Text("提醒时间") },
                        supportingContent = { Text(settings.dailyReminderTime) },
                        trailingContent = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Schedule, contentDescription = "选择时间")
                            }
                        }
                    )
                    
                    SwitchListItem(
                        title = "周末提醒",
                        subtitle = "周末也发送提醒",
                        checked = settings.enableWeekendReminder,
                        onCheckedChange = { settings = settings.copy(enableWeekendReminder = it) }
                    )
                }
                
                SwitchListItem(
                    title = "月末提醒",
                    subtitle = "月末提醒查看月度报表",
                    checked = settings.enableMonthEndReminder,
                    onCheckedChange = { settings = settings.copy(enableMonthEndReminder = it) }
                )
                
                if (settings.enableMonthEndReminder) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("提前天数")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (settings.monthEndReminderDays > 1) {
                                        settings = settings.copy(monthEndReminderDays = settings.monthEndReminderDays - 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "减少")
                            }
                            Text("${settings.monthEndReminderDays} 天")
                            IconButton(
                                onClick = {
                                    if (settings.monthEndReminderDays < 7) {
                                        settings = settings.copy(monthEndReminderDays = settings.monthEndReminderDays + 1)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "增加")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdateSettings(settings)
                    onDismiss()
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
    )
    
    // TODO: 实现时间选择器
    if (showTimePicker) {
        // 这里应该显示时间选择器对话框
        showTimePicker = false
    }
}

/**
 * 带开关的列表项
 */
@Composable
private fun SwitchListItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}