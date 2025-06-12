package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ccxiaoji.app.R
import com.ccxiaoji.app.presentation.ui.navigation.*
import com.ccxiaoji.app.presentation.ui.profile.components.PersonalInfoCard
import com.ccxiaoji.app.presentation.ui.profile.components.SettingsSection
import com.ccxiaoji.app.presentation.ui.profile.components.SettingsItem
import com.ccxiaoji.app.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "我的",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
            // Personal info card
            PersonalInfoCard(
                username = uiState.username ?: "用户",
                email = uiState.userEmail,
                signature = uiState.signature,
                onEditProfile = { /* TODO: Navigate to edit profile */ }
            )
            
            // Module Settings
            SettingsSection(
                title = "模块设置",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.AccountBalance,
                        title = "记账设置",
                        subtitle = "默认账户、记账提醒、快速记账",
                        onClick = { navController.navigate(LedgerSettingsRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Task,
                        title = "待办设置",
                        subtitle = "提醒设置、默认优先级",
                        onClick = { /* TODO: Navigate to todo settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.FitnessCenter,
                        title = "习惯设置",
                        subtitle = "打卡提醒、目标设置",
                        onClick = { /* TODO: Navigate to habit settings */ }
                    )
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Data Management
            SettingsSection(
                title = "数据管理",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Sync,
                        title = "数据同步",
                        subtitle = if (uiState.isSyncing) "同步中..." else "同步数据到云端",
                        value = uiState.lastSyncTime?.let { "上次同步: $it" },
                        onClick = { viewModel.syncData() }
                    ),
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        title = "数据备份",
                        subtitle = "备份数据到云端",
                        onClick = { viewModel.backupData() }
                    ),
                    SettingsItem(
                        icon = Icons.Default.CloudDownload,
                        title = "数据恢复",
                        subtitle = "从云端恢复数据",
                        onClick = { viewModel.restoreData() }
                    ),
                    SettingsItem(
                        icon = Icons.Default.FileDownload,
                        title = "数据导出",
                        subtitle = "导出各模块数据",
                        onClick = { navController.navigate(DataExportRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Build,
                        title = "批量操作",
                        subtitle = "批量删除、修改、导入",
                        onClick = { navController.navigate(BatchOperationRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "清理缓存",
                        subtitle = "清理应用缓存数据",
                        value = uiState.cacheSize,
                        onClick = { viewModel.clearCache() }
                    )
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // App Settings
            SettingsSection(
                title = "应用设置",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题设置",
                        subtitle = "选择应用主题",
                        value = uiState.currentTheme,
                        onClick = { navController.navigate(ThemeSettingsRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Language,
                        title = "语言设置",
                        value = "中文简体",
                        onClick = { /* TODO: Navigate to language settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "字体大小",
                        value = uiState.fontSize,
                        onClick = { /* TODO: Navigate to font size settings */ }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "通知设置",
                        subtitle = "管理应用通知",
                        onClick = { navController.navigate(NotificationSettingsRoute.route) }
                    )
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Security & Privacy
            SettingsSection(
                title = "安全隐私",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "应用锁",
                        subtitle = "设置应用访问密码",
                        value = if (uiState.isAppLockEnabled) "已开启" else "未开启",
                        onClick = { navController.navigate(AppLockSettingsRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Shield,
                        title = "隐私设置",
                        subtitle = "管理数据隐私",
                        onClick = { navController.navigate(PrivacySettingsRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Key,
                        title = "权限管理",
                        subtitle = "管理应用权限",
                        onClick = { /* TODO: Open system permissions */ }
                    )
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Other
            SettingsSection(
                title = "其他",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.HelpOutline,
                        title = "使用帮助",
                        onClick = { navController.navigate(HelpRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Feedback,
                        title = "意见反馈",
                        onClick = { navController.navigate(FeedbackRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于我们",
                        onClick = { navController.navigate(AboutRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.SystemUpdate,
                        title = "版本更新",
                        value = "v${uiState.appVersion}",
                        onClick = { viewModel.checkForUpdates() }
                    )
                )
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Logout
            SettingsSection(
                title = "",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "退出登录",
                        onClick = { showLogoutDialog = true }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}