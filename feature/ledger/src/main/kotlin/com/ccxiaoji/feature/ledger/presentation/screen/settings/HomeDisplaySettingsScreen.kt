package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.HomeDisplaySettingsViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 首页显示设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDisplaySettingsScreen(
    navController: NavController,
    viewModel: HomeDisplaySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("首页显示设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveSettings()
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("home_display_settings_updated", true)
                            navController.popBackStack()
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.medium)
                    .padding(top = DesignTokens.Spacing.medium),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    // 今日数据显示
                    SectionHeader(title = "今日数据")
                    SwitchListItem(
                        title = "显示今日支出",
                        checked = uiState.showTodayExpense,
                        onCheckedChange = { viewModel.updateShowTodayExpense(it) }
                    )
                    SwitchListItem(
                        title = "显示今日收入",
                        checked = uiState.showTodayIncome,
                        onCheckedChange = { viewModel.updateShowTodayIncome(it) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    
                    // 本月数据显示
                    SectionHeader(title = "本月数据")
                    SwitchListItem(
                        title = "显示本月支出",
                        checked = uiState.showMonthExpense,
                        onCheckedChange = { viewModel.updateShowMonthExpense(it) }
                    )
                    SwitchListItem(
                        title = "显示本月收入",
                        checked = uiState.showMonthIncome,
                        onCheckedChange = { viewModel.updateShowMonthIncome(it) }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                    
                    // 其他显示设置
                    SectionHeader(title = "其他显示")
                    SwitchListItem(
                        title = "显示账户余额",
                        checked = uiState.showAccountBalance,
                        onCheckedChange = { viewModel.updateShowAccountBalance(it) }
                    )
                    SwitchListItem(
                        title = "显示预算进度",
                        checked = uiState.showBudgetProgress,
                        onCheckedChange = { viewModel.updateShowBudgetProgress(it) }
                    )
                    SwitchListItem(
                        title = "显示最近交易",
                        checked = uiState.showRecentTransactions,
                        onCheckedChange = { viewModel.updateShowRecentTransactions(it) }
                    )
                    
                    if (uiState.showRecentTransactions) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "最近交易数量",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.decreaseRecentTransactionCount() },
                                    enabled = uiState.recentTransactionCount > 1
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "减少",
                                        tint = if (uiState.recentTransactionCount > 1) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                }
                                Text(
                                    text = uiState.recentTransactionCount.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
                                )
                                IconButton(
                                    onClick = { viewModel.increaseRecentTransactionCount() },
                                    enabled = uiState.recentTransactionCount < 10
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "增加",
                                        tint = if (uiState.recentTransactionCount < 10) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 说明文字
            Text(
                text = "自定义首页显示的内容，让记账更符合您的使用习惯",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(DesignTokens.Spacing.medium)
            )
        }
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
        headlineContent = { 
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            ) 
        },
        supportingContent = subtitle?.let { 
            { 
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ) 
            } 
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }
    )
}

/**
 * 小节标题
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            horizontal = DesignTokens.Spacing.medium,
            vertical = DesignTokens.Spacing.small
        )
    )
}