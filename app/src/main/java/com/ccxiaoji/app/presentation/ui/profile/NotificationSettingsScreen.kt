package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.ui.profile.notification.NotificationSetting
import com.ccxiaoji.app.presentation.ui.profile.notification.components.*
import com.ccxiaoji.app.presentation.viewmodel.NotificationSettingsViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 通知设置界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 主开关
            MainNotificationCard(
                notificationsEnabled = uiState.notificationsEnabled,
                onToggle = { viewModel.setNotificationsEnabled(it) }
            )
            
            if (uiState.notificationsEnabled) {
                // 任务提醒
                NotificationSection(
                    title = "任务提醒",
                    icon = Icons.Default.Task,
                    settings = listOf(
                        NotificationSetting(
                            title = "任务到期提醒",
                            description = "在任务到期前提醒",
                            enabled = uiState.taskDueReminder,
                            onToggle = { viewModel.setTaskDueReminder(it) }
                        ),
                        NotificationSetting(
                            title = "提前提醒时间",
                            value = "${uiState.taskReminderMinutes}分钟",
                            enabled = uiState.taskDueReminder,
                            onClick = { viewModel.showTaskReminderTimePicker() }
                        )
                    )
                )
                
                // 习惯提醒
                NotificationSection(
                    title = "习惯提醒",
                    icon = Icons.Default.FitnessCenter,
                    settings = listOf(
                        NotificationSetting(
                            title = "习惯打卡提醒",
                            description = "每天定时提醒打卡",
                            enabled = uiState.habitReminder,
                            onToggle = { viewModel.setHabitReminder(it) }
                        ),
                        NotificationSetting(
                            title = "提醒时间",
                            value = uiState.habitReminderTime,
                            enabled = uiState.habitReminder,
                            onClick = { viewModel.showHabitReminderTimePicker() }
                        )
                    )
                )
                
                // 预算提醒
                NotificationSection(
                    title = "预算提醒",
                    icon = Icons.Default.AccountBalance,
                    settings = listOf(
                        NotificationSetting(
                            title = "预算超支提醒",
                            description = "预算使用超过阈值时提醒",
                            enabled = uiState.budgetAlerts,
                            onToggle = { viewModel.setBudgetAlerts(it) }
                        ),
                        NotificationSetting(
                            title = "提醒阈值",
                            value = "${uiState.budgetAlertThreshold}%",
                            enabled = uiState.budgetAlerts,
                            onClick = { viewModel.showBudgetThresholdPicker() }
                        )
                    )
                )
                
                // 其他设置
                NotificationSection(
                    title = "其他设置",
                    icon = Icons.Default.MoreHoriz,
                    settings = listOf(
                        NotificationSetting(
                            title = "震动",
                            description = "收到通知时震动",
                            enabled = uiState.vibrationEnabled,
                            onToggle = { viewModel.setVibrationEnabled(it) }
                        ),
                        NotificationSetting(
                            title = "通知音",
                            description = "收到通知时播放声音",
                            enabled = uiState.soundEnabled,
                            onToggle = { viewModel.setSoundEnabled(it) }
                        ),
                        NotificationSetting(
                            title = "免打扰时段",
                            value = if (uiState.doNotDisturbEnabled) {
                                "${uiState.doNotDisturbStart} - ${uiState.doNotDisturbEnd}"
                            } else {
                                "关闭"
                            },
                            enabled = true,
                            onClick = { viewModel.showDoNotDisturbSettings() }
                        )
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))

            // 历史与诊断
            NotificationSection(
                title = "历史与诊断",
                icon = Icons.Default.History,
                settings = listOf(
                    NotificationSetting(
                        title = "查看通知历史",
                        description = "查看入队、发送与状态变化记录",
                        enabled = true,
                        onClick = onNavigateToHistory
                    )
                )
            )
        }
    }
}
