package com.ccxiaoji.feature.schedule.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.components.TimePickerDialog
import java.time.DayOfWeek

/**
 * 设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToShiftManage: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showBackupLocationDialog by remember { mutableStateOf(false) }
    var showWeekStartDayDialog by remember { mutableStateOf(false) }
    
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
    
    // 时间选择器对话框
    TimePickerDialog(
        showDialog = showTimePickerDialog,
        initialTime = uiState.notificationTime,
        onTimeSelected = { time ->
            viewModel.updateNotificationTime(time)
        },
        onDismiss = { showTimePickerDialog = false }
    )
    
    // 一周起始日选择对话框
    if (showWeekStartDayDialog) {
        AlertDialog(
            onDismissRequest = { showWeekStartDayDialog = false },
            title = {
                Text(stringResource(R.string.schedule_settings_week_start_dialog_title))
            },
            text = {
                Column {
                    val weekDays = listOf(
                        DayOfWeek.MONDAY to stringResource(R.string.schedule_settings_week_start_monday),
                        DayOfWeek.SUNDAY to stringResource(R.string.schedule_settings_week_start_sunday)
                    )
                    
                    weekDays.forEach { (dayOfWeek, displayName) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setWeekStartDay(dayOfWeek)
                                    showWeekStartDayDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.weekStartDayValue == dayOfWeek,
                                onClick = {
                                    viewModel.setWeekStartDay(dayOfWeek)
                                    showWeekStartDayDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showWeekStartDayDialog = false }
                ) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        )
    }
    
    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(stringResource(R.string.schedule_settings_confirm_clear_title)) },
            text = { Text(stringResource(R.string.schedule_settings_confirm_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text(stringResource(R.string.schedule_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        )
    }
    
    // 备份位置选择对话框
    if (showBackupLocationDialog) {
        AlertDialog(
            onDismissRequest = { showBackupLocationDialog = false },
            title = { Text(stringResource(R.string.schedule_settings_backup_location_dialog_title)) },
            text = { Text(stringResource(R.string.schedule_settings_backup_location_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        backupLauncher.launch("schedule_backup_${System.currentTimeMillis()}.db")
                        showBackupLocationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.schedule_settings_backup_external))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.performBackup(null)
                        showBackupLocationDialog = false
                    }
                ) {
                    Text(stringResource(R.string.schedule_settings_backup_internal))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.schedule_back))
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
            // 通用设置
            item {
                SettingsCategoryHeader(stringResource(R.string.schedule_settings_general))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Schedule,
                    title = stringResource(R.string.schedule_settings_shift_manage),
                    subtitle = stringResource(R.string.schedule_settings_shift_manage_subtitle),
                    onClick = onNavigateToShiftManage
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.schedule_settings_reminder),
                    subtitle = if (uiState.notificationEnabled) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                    onClick = { }
                ) {
                    Switch(
                        checked = uiState.notificationEnabled,
                        onCheckedChange = viewModel::updateNotificationEnabled
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.AccessTime,
                    title = stringResource(R.string.schedule_settings_reminder_time),
                    subtitle = uiState.notificationTime,
                    enabled = uiState.notificationEnabled,
                    onClick = {
                        if (uiState.notificationEnabled) {
                            showTimePickerDialog = true
                        }
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Weekend,
                    title = stringResource(R.string.schedule_settings_week_start),
                    subtitle = uiState.weekStartDay,
                    onClick = {
                        showWeekStartDayDialog = true
                    }
                )
            }
            
            // 数据管理
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsCategoryHeader(stringResource(R.string.schedule_settings_data_management))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = stringResource(R.string.schedule_settings_auto_backup),
                    subtitle = if (uiState.autoBackupEnabled) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                    onClick = { }
                ) {
                    Switch(
                        checked = uiState.autoBackupEnabled,
                        onCheckedChange = viewModel::updateAutoBackupEnabled
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = stringResource(R.string.schedule_settings_backup_now),
                    subtitle = uiState.lastBackupTime?.let { stringResource(R.string.schedule_settings_last_backup, it) } ?: stringResource(R.string.schedule_settings_never_backup),
                    onClick = {
                        showBackupLocationDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Restore,
                    title = stringResource(R.string.schedule_settings_restore_data),
                    subtitle = stringResource(R.string.schedule_settings_restore_data_subtitle),
                    onClick = {
                        restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.schedule_settings_clear_data),
                    subtitle = stringResource(R.string.schedule_settings_clear_data_subtitle),
                    onClick = {
                        showClearDataDialog = true
                    }
                )
            }
            
            // 外观
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsCategoryHeader(stringResource(R.string.schedule_settings_appearance))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.schedule_settings_dark_mode),
                    subtitle = if (uiState.isDarkMode) stringResource(R.string.schedule_settings_reminder_enabled) else stringResource(R.string.schedule_settings_reminder_disabled),
                    onClick = { }
                ) {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.ColorLens,
                    title = stringResource(R.string.schedule_settings_theme_color),
                    subtitle = stringResource(R.string.schedule_settings_theme_color_subtitle),
                    onClick = {
                        // TODO: 显示颜色选择器
                    }
                )
            }
            
            // 关于
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsCategoryHeader(stringResource(R.string.schedule_settings_about))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.schedule_settings_about_app),
                    subtitle = stringResource(R.string.schedule_settings_version, uiState.appVersion),
                    onClick = onNavigateToAbout
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.schedule_settings_share_app),
                    subtitle = stringResource(R.string.schedule_settings_share_app_subtitle),
                    onClick = {
                        // TODO: 分享应用
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.schedule_settings_rate_app),
                    subtitle = stringResource(R.string.schedule_settings_rate_app_subtitle),
                    onClick = {
                        // TODO: 跳转到应用商店
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // 显示加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
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
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text(stringResource(R.string.schedule_settings_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            }
        )
    }
}

/**
 * 设置分类标题
 */
@Composable
private fun SettingsCategoryHeader(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 设置项
 */
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }
            
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailing()
            } else if (!onClick.equals({})) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * 深色模式选项
 */
enum class DarkModeOption {
    SYSTEM,
    LIGHT,
    DARK
}