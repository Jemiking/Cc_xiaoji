package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ReminderSettingsViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 提醒设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSettingsScreen(
    navController: NavController,
    viewModel: ReminderSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记账提醒设置") },
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
                                ?.set("reminder_settings_updated", true)
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
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            // 每日记账提醒
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    SwitchListItem(
                        title = "每日记账提醒",
                        checked = uiState.enableDailyReminder,
                        onCheckedChange = { viewModel.updateEnableDailyReminder(it) }
                    )
                    
                    if (uiState.enableDailyReminder) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                        
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = "提醒时间",
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            supportingContent = { 
                                Text(
                                    text = uiState.dailyReminderTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                ) 
                            },
                            trailingContent = {
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = "选择时间",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                        
                        SwitchListItem(
                            title = "周末提醒",
                            subtitle = "周末也发送提醒",
                            checked = uiState.enableWeekendReminder,
                            onCheckedChange = { viewModel.updateEnableWeekendReminder(it) }
                        )
                    }
                }
            }
            
            // 月末提醒
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    SwitchListItem(
                        title = "月末提醒",
                        subtitle = "月末提醒查看月度报表",
                        checked = uiState.enableMonthEndReminder,
                        onCheckedChange = { viewModel.updateEnableMonthEndReminder(it) }
                    )
                    
                    if (uiState.enableMonthEndReminder) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.medium),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignTokens.Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "提前天数",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.decreaseMonthEndReminderDays() },
                                    enabled = uiState.monthEndReminderDays > 1
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "减少",
                                        tint = if (uiState.monthEndReminderDays > 1) 
                                            MaterialTheme.colorScheme.onSurfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                                }
                                Text(
                                    text = "${uiState.monthEndReminderDays} 天",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
                                )
                                IconButton(
                                    onClick = { viewModel.increaseMonthEndReminderDays() },
                                    enabled = uiState.monthEndReminderDays < 7
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "增加",
                                        tint = if (uiState.monthEndReminderDays < 7) 
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
                text = "提醒功能需要授予通知权限才能正常工作",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = DesignTokens.Spacing.small)
            )
        }
    }
    
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