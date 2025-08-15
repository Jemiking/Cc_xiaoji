package com.ccxiaoji.feature.schedule.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.components.TimePickerDialog
import com.ccxiaoji.feature.schedule.presentation.settings.components.*
import com.ccxiaoji.ui.theme.DesignTokens
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen

/**
 * 设置界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToShiftManage: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 备份文件选择器
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.performBackup(it)
        }
    }
    
    // 恢复文件选择器
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.restoreDatabase(it)
        }
    }
    
    
    
    // 处理清除数据确认结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<Boolean> { cleared ->
                if (cleared == true) {
                    viewModel.clearAllData()
                    savedStateHandle.remove<Boolean>("data_cleared")
                }
            }
            savedStateHandle.getLiveData<Boolean>("data_cleared").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<Boolean>("data_cleared").removeObserver(observer)
            }
        }
    }
    
    // 处理备份位置选择结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<String> { location ->
                location?.let {
                    when (it) {
                        "external" -> {
                            backupLauncher.launch("schedule_backup_${System.currentTimeMillis()}.db")
                        }
                        "internal" -> {
                            viewModel.performBackup(null)
                        }
                    }
                    savedStateHandle.remove<String>("backup_location")
                }
            }
            savedStateHandle.getLiveData<String>("backup_location").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<String>("backup_location").removeObserver(observer)
            }
        }
    }
    
    // 处理周起始日选择结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<java.time.DayOfWeek> { selectedDay ->
                selectedDay?.let {
                    viewModel.setWeekStartDay(it)
                    savedStateHandle.remove<java.time.DayOfWeek>("week_start_day")
                }
            }
            savedStateHandle.getLiveData<java.time.DayOfWeek>("week_start_day").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<java.time.DayOfWeek>("week_start_day").removeObserver(observer)
            }
        }
    }
    
    // 处理时间选择结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.Observer<String> { selectedTime ->
                selectedTime?.let {
                    viewModel.updateNotificationTime(it)
                    savedStateHandle.remove<String>("selected_time")
                }
            }
            savedStateHandle.getLiveData<String>("selected_time").observe(lifecycleOwner, observer)
            onDispose {
                savedStateHandle.getLiveData<String>("selected_time").removeObserver(observer)
            }
        }
    }
    
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = DesignTokens.Spacing.small)
        ) {
            // 通用设置
            item {
                Column {
                    SettingsCategoryHeader(stringResource(R.string.schedule_settings_general))
                    
                    SettingsItem(
                        icon = Icons.Default.Schedule,
                        title = stringResource(R.string.schedule_settings_shift_manage),
                        subtitle = stringResource(R.string.schedule_settings_shift_manage_subtitle),
                        onClick = onNavigateToShiftManage
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.schedule_settings_reminder),
                        subtitle = if (uiState.notificationEnabled) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                        onClick = { },
                        trailing = {
                            Switch(
                                checked = uiState.notificationEnabled,
                                onCheckedChange = viewModel::updateNotificationEnabled
                            )
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.AccessTime,
                        title = stringResource(R.string.schedule_settings_reminder_time),
                        subtitle = uiState.notificationTime,
                        enabled = uiState.notificationEnabled,
                        onClick = {
                            if (uiState.notificationEnabled) {
                                navController?.navigate(Screen.TimePicker.createRoute(uiState.notificationTime))
                            }
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Weekend,
                        title = stringResource(R.string.schedule_settings_week_start),
                        subtitle = uiState.weekStartDay,
                        onClick = {
                            navController?.navigate(Screen.WeekStartDay.createRoute(uiState.weekStartDayValue.name))
                        }
                    )
                }
            }
            
            // 数据管理
            item {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.small),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    SettingsCategoryHeader(stringResource(R.string.schedule_settings_data_management))
                    
                    SettingsItem(
                        icon = Icons.Default.CloudUpload,
                        title = stringResource(R.string.schedule_settings_auto_backup),
                        subtitle = if (uiState.autoBackupEnabled) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                        onClick = { },
                        trailing = {
                            Switch(
                                checked = uiState.autoBackupEnabled,
                                onCheckedChange = viewModel::updateAutoBackupEnabled
                            )
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Backup,
                        title = stringResource(R.string.schedule_settings_backup_now),
                        subtitle = uiState.lastBackupTime?.let { stringResource(R.string.schedule_settings_last_backup, it) } ?: stringResource(R.string.schedule_settings_never_backup),
                        onClick = {
                            navController?.navigate(Screen.BackupLocation.route)
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Restore,
                        title = stringResource(R.string.schedule_settings_restore_data),
                        subtitle = stringResource(R.string.schedule_settings_restore_data_subtitle),
                        onClick = {
                            restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(R.string.schedule_settings_clear_data),
                        subtitle = stringResource(R.string.schedule_settings_clear_data_subtitle),
                        onClick = {
                            navController?.navigate(Screen.ClearData.route)
                        }
                    )
                }
            }
            
            // 外观
            item {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.small),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    SettingsCategoryHeader(stringResource(R.string.schedule_settings_appearance))
                    
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.schedule_settings_dark_mode),
                        subtitle = if (uiState.isDarkMode) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                        onClick = { },
                        trailing = {
                            Switch(
                                checked = uiState.isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode() }
                            )
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.ColorLens,
                        title = stringResource(R.string.schedule_settings_theme_color),
                        subtitle = stringResource(R.string.schedule_settings_theme_color_subtitle),
                        onClick = {
                            // TODO: 显示颜色选择器
                        }
                    )
                }
            }
            
            // 关于
            item {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.small),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )
                    SettingsCategoryHeader(stringResource(R.string.schedule_settings_about))
                    
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.schedule_settings_about_app),
                        subtitle = stringResource(R.string.schedule_settings_version, uiState.appVersion),
                        onClick = onNavigateToAbout
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Share,
                        title = stringResource(R.string.schedule_settings_share_app),
                        subtitle = stringResource(R.string.schedule_settings_share_app_subtitle),
                        onClick = {
                            // TODO: 分享应用
                        }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.schedule_settings_rate_app),
                        subtitle = stringResource(R.string.schedule_settings_rate_app_subtitle),
                        onClick = {
                            // TODO: 跳转到应用商店
                        }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            }
        }
    }
    
    // 显示加载状态
    LoadingOverlay(isLoading = uiState.isLoading)
    
    // 显示成功消息
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }
    
    // 显示错误消息
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            navController?.navigate(Screen.Error.createRoute(message))
            viewModel.clearErrorMessage()
        }
    }
}