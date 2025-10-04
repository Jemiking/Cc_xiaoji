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
import com.ccxiaoji.app.presentation.ui.profile.components.*
import com.ccxiaoji.app.presentation.viewmodel.ProfileViewModel
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController = rememberNavController(),
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // 处理退出登录确认返回结果
    navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<Boolean> { confirmed ->
                confirmed?.let {
                    if (it) {
                        viewModel.logout()
                    }
                    savedStateHandle.remove<Boolean>("logout_confirmed")
                }
            }
            savedStateHandle.getLiveData<Boolean>("logout_confirmed").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<Boolean>("logout_confirmed").removeObserver(observer)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "我的",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
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
            // Personal info card
            PersonalInfoCard(
                username = uiState.username ?: "用户",
                email = uiState.userEmail,
                signature = uiState.signature,
                onEditProfile = { /* TODO: Navigate to edit profile */ }
            )
            
            // Search bar
            SettingsSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it }
            )
            
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
                        icon = Icons.Default.FolderOpen,
                        title = "数据导出中心",
                        subtitle = "统一导出所有模块数据",
                        onClick = { navController.navigate(DataExportRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.FileUpload,
                        title = "数据导入",
                        subtitle = "从文件导入各模块数据",
                        onClick = { navController.navigate(LedgerImportRoute.route) }
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
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
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
                        icon = Icons.Default.Apps,
                        title = "模块管理",
                        subtitle = "自定义首页模块显示",
                        onClick = { navController.navigate(ModuleManagementRoute.route) }
                    ),
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "通知设置",
                        subtitle = "管理应用通知",
                        onClick = { navController.navigate(NotificationSettingsRoute.route) }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // Other
            SettingsSection(
                title = "其他",
                items = buildList {
                    add(
                        SettingsItem(
                            icon = Icons.Default.Key,
                            title = "权限管理",
                            subtitle = "管理应用权限",
                            onClick = { navController.navigate(PermissionManagementRoute.route) }
                        )
                    )
                    add(
                        SettingsItem(
                            icon = Icons.Default.Info,
                            title = "关于我们",
                            onClick = { navController.navigate(AboutRoute.route) }
                        )
                    )
                    // 布局Demo已下线，入口移除
                    // 仅在开启标志时显示风格 Demo 入口
                    if (com.ccxiaoji.app.BuildConfig.SHOW_STYLE_DEMO) {
                        add(
                            SettingsItem(
                                icon = Icons.Default.Palette,
                                title = "风格Demo（记账）",
                                subtitle = "查看11种记账列表风格展示",
                                onClick = { navController.navigate(StyleCatalogDemoRoute.route) }
                            )
                        )
                    }
                    add(
                        SettingsItem(
                            icon = Icons.Default.SystemUpdate,
                            title = "版本更新",
                            value = "v${uiState.appVersion}",
                            onClick = { viewModel.checkForUpdates() }
                        )
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // Logout
            SettingsSection(
                title = "",
                items = listOf(
                    SettingsItem(
                        icon = Icons.Default.Logout,
                        title = "退出登录",
                        onClick = { navController.navigate(LogoutConfirmationRoute.route) }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
