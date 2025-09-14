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
import androidx.activity.compose.BackHandler
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
    onNavigateBack: (() -> Unit)? = null,
    viewModel: HomeDisplaySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 系统返回
    BackHandler { onNavigateBack?.invoke() ?: navController.popBackStack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("首页显示设置") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack?.invoke() ?: navController.popBackStack() }) {
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
                            onNavigateBack?.invoke() ?: navController.popBackStack()
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
                    SectionHeader(title = "首页显示设置")
                    SwitchListItem(
                        title = "显示今日支出",
                        subtitle = "在首页概览中显示当日的总支出金额",
                        checked = uiState.showTodayExpense,
                        onCheckedChange = { viewModel.updateShowTodayExpense(it) }
                    )
                }
            }
            
            // 说明文字
            Text(
                text = "控制首页概览中显示的内容。当前只有今日支出统计功能已实现。",
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
