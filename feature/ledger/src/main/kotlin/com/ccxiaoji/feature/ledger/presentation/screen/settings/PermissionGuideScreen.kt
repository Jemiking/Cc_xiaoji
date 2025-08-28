package com.ccxiaoji.feature.ledger.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.ccxiaoji.feature.ledger.presentation.viewmodel.PermissionGuideViewModel

/**
 * 权限引导页面
 * 
 * 引导用户开启自动记账功能所需的各项权限和设置
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuideScreen(
    onNavigateBack: () -> Unit,
    viewModel: PermissionGuideViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限与兼容性") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 说明文字
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "为实现自动记账，需要以下权限与设置：",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "所有数据仅在本地处理，绝不上传。您可以随时在设置中关闭自动记账功能。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 通知使用权（必须）
            PermissionItem(
                title = "通知使用权（必须）",
                description = "仅用于读取支付成功等相关通知，所有数据本地处理。",
                isGranted = state.notificationListenerEnabled,
                isRequired = true,
                onAction = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )
            
            // 通知发布权限（Android 13+）
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                PermissionItem(
                    title = "通知发布权限（Android 13+）",
                    description = "用于展示\"已记账/撤销\"等通知。",
                    isGranted = state.postNotificationsGranted,
                    isRequired = false,
                    onAction = {
                        viewModel.requestPostNotificationPermission(context)
                    }
                )
            }
            
            // 悬浮窗权限（可选）
            PermissionItem(
                title = "悬浮窗权限（可选，高级功能）",
                description = "仅在您启用\"悬浮窗弹窗模式\"时，用于就地确认。",
                isGranted = state.canDrawOverlays,
                isRequired = false,
                onAction = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            )
            
            // 电池优化（建议）
            PermissionItem(
                title = "电池优化（建议）",
                description = "避免系统在后台回收通知监听服务。",
                isGranted = state.ignoreBatteryOptimizations,
                isRequired = false,
                onAction = {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 完成按钮
            Button(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成")
            }
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    isRequired: Boolean,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (isRequired) {
                            Text(
                                text = "必需",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                if (isGranted) {
                    Text(
                        text = "已开启",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    OutlinedButton(onClick = onAction) {
                        Text("去开启")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}