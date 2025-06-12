package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.viewmodel.NotificationSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
        ) {
            // 主开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "启用通知",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "关闭后将不会收到任何通知",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NotificationSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    settings: List<NotificationSetting>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            settings.forEachIndexed { index, setting ->
                if (index > 0) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                NotificationSettingItem(setting)
            }
        }
    }
}

@Composable
private fun NotificationSettingItem(setting: NotificationSetting) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = setting.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (setting.enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )
            setting.description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (setting.enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
        }
        
        when {
            setting.onToggle != null -> {
                Switch(
                    checked = setting.enabled,
                    onCheckedChange = setting.onToggle,
                    enabled = true
                )
            }
            setting.onClick != null -> {
                TextButton(
                    onClick = setting.onClick,
                    enabled = setting.enabled
                ) {
                    Text(setting.value ?: "设置")
                }
            }
        }
    }
}

data class NotificationSetting(
    val title: String,
    val description: String? = null,
    val value: String? = null,
    val enabled: Boolean,
    val onToggle: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)