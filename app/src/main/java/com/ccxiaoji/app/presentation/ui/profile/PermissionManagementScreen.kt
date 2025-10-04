package com.ccxiaoji.app.presentation.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 权限管理页面
 * 展示应用所需权限及其用途，引导用户前往系统设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionManagementScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // 用于触发重组以更新权限状态
    var refreshTrigger by remember { mutableStateOf(0) }

    // 权限请求Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // 权限请求结果回调，触发重组更新UI
        refreshTrigger++
    }

    // 权限信息列表
    val permissions = remember {
        listOf(
            PermissionInfo(
                name = "通知权限",
                permission = Manifest.permission.POST_NOTIFICATIONS,
                purpose = "发送任务提醒、习惯打卡提醒和记账提醒",
                icon = Icons.Default.Notifications,
                isRequired = true
            ),
            PermissionInfo(
                name = "位置权限",
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                purpose = "记录消费地点，提供位置相关的统计功能",
                icon = Icons.Default.LocationOn,
                isRequired = false
            ),
            PermissionInfo(
                name = "相机权限",
                permission = Manifest.permission.CAMERA,
                purpose = "扫描二维码导入数据或拍摄账单照片",
                icon = Icons.Default.CameraAlt,
                isRequired = false
            ),
            PermissionInfo(
                name = "精确闹钟权限",
                permission = Manifest.permission.SCHEDULE_EXACT_ALARM,
                purpose = "用于待办、习惯、还款等定时提醒（功能开发中）",
                icon = Icons.Default.Alarm,
                isRequired = true
            ),
            PermissionInfo(
                name = "悬浮窗权限",
                permission = Manifest.permission.SYSTEM_ALERT_WINDOW,
                purpose = "显示快速记账悬浮球",
                icon = Icons.Default.Widgets,
                isRequired = false
            ),
            PermissionInfo(
                name = "网络权限",
                permission = Manifest.permission.INTERNET,
                purpose = "数据同步和云端备份功能（部分开发中）",
                icon = Icons.Default.Wifi,
                isRequired = true,
                isSystemGranted = true
            ),
            PermissionInfo(
                name = "网络状态权限",
                permission = Manifest.permission.ACCESS_NETWORK_STATE,
                purpose = "检测网络连接状态，优化同步策略",
                icon = Icons.Default.NetworkCheck,
                isRequired = true,
                isSystemGranted = true
            ),
            PermissionInfo(
                name = "开机启动权限",
                permission = Manifest.permission.RECEIVE_BOOT_COMPLETED,
                purpose = "开机后刷新桌面小部件和同步数据",
                icon = Icons.Default.PowerSettingsNew,
                isRequired = false,
                isSystemGranted = true
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 前往系统设置按钮
                    TextButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("系统设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
            // 说明卡片
            ModernCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "权限说明",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "以下权限用于提供完整的应用功能，您可以在系统设置中管理这些权限。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))

            // 权限列表
            permissions.forEach { permission ->
                // 检查权限状态（refreshTrigger用于触发重组）
                val isGranted = remember(refreshTrigger) {
                    permission.isSystemGranted || checkPermission(context, permission.permission)
                }

                PermissionItemCard(
                    permission = permission,
                    isGranted = isGranted,
                    onClick = {
                        handlePermissionClick(
                            context = context,
                            permission = permission,
                            isGranted = isGranted,
                            onRequestPermission = { permissionLauncher.launch(permission.permission) }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        }
    }
}

/**
 * 权限信息数据类
 */
data class PermissionInfo(
    val name: String,
    val permission: String,
    val purpose: String,
    val icon: ImageVector,
    val isRequired: Boolean = false,
    val isSystemGranted: Boolean = false  // 系统自动授予的权限（如网络权限）
)

/**
 * 权限项卡片
 */
@Composable
private fun PermissionItemCard(
    permission: PermissionInfo,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    val isClickable = !permission.isSystemGranted // 系统自动授予的权限不可点击

    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.xs
            ),
        onClick = if (isClickable) onClick else null // 传递onClick给ModernCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 权限图标
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (isGranted)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = permission.icon,
                        contentDescription = null,
                        tint = if (isGranted)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))

            // 权限信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = permission.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (permission.isRequired) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "必需",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = permission.purpose,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))

            // 权限状态指示器
            StatusIndicator(isGranted = isGranted)
        }
    }
}

/**
 * 权限状态指示器
 */
@Composable
private fun StatusIndicator(isGranted: Boolean) {
    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = if (isGranted)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已授予",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "未授予",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 检查权限是否已授予
 */
private fun checkPermission(context: android.content.Context, permission: String): Boolean {
    // 特殊权限需要特殊检测方法
    return when (permission) {
        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            // 悬浮窗权限需要用Settings.canDrawOverlays检测
            Settings.canDrawOverlays(context)
        }
        Manifest.permission.SCHEDULE_EXACT_ALARM -> {
            // 精确闹钟权限在Android 12+需要特殊检测
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(android.app.AlarmManager::class.java)
                alarmManager?.canScheduleExactAlarms() ?: false
            } else {
                true // Android 12以下自动授予
            }
        }
        else -> {
            // 普通权限使用标准检测
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
}

/**
 * 处理权限点击事件
 */
private fun handlePermissionClick(
    context: android.content.Context,
    permission: PermissionInfo,
    isGranted: Boolean,
    onRequestPermission: () -> Unit
) {
    // 特殊权限需要跳转到特殊设置页面
    when (permission.permission) {
        Manifest.permission.SYSTEM_ALERT_WINDOW -> {
            // 悬浮窗权限需要跳转到特殊设置页面
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
            return
        }
        Manifest.permission.SCHEDULE_EXACT_ALARM -> {
            // Android 12+的精确闹钟权限需要跳转到特殊设置
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
                return
            }
        }
    }

    // 普通权限处理
    if (isGranted) {
        // 已授予 -> 跳转到系统设置页面
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    } else {
        // 未授予 -> 请求权限
        onRequestPermission()
    }
}
