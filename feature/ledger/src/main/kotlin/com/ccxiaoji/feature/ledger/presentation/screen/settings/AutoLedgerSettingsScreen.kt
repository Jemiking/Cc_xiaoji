package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoLedgerSettingsViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLedgerSettingsScreen(
    navController: NavController,
    onNavigateToPermissionGuide: () -> Unit = {},
    viewModel: AutoLedgerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "自动记账设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 占位UI的“支付结果捕获方式”分组移动到基础设置（启用/模式/监听控制）之后，再展示
            // 基础：总开关 + 模式选择 + 监听控制
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("启用自动记账") },
                        supportingContent = {
                            val tip = if (uiState.notificationListenerEnabled) "通知监听：已连接" else "通知监听：未连接（请在系统设置开启使用权）"
                            Text(tip, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            Switch(
                                checked = uiState.globalEnabled,
                                onCheckedChange = { checked -> viewModel.toggleGlobalEnabled(checked) }
                            )
                        }
                    )
                    HorizontalDivider()
                    // 软关闭：隐藏“模式选择”（始终以半自动生效，便于后续恢复）
                    ListItem(
                        headlineContent = { Text("当前模式") },
                        supportingContent = {
                            Text("半自动（全自动功能暂时隐藏，后续按需开放）",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("监听服务控制") },
                        supportingContent = {
                            Text(
                                "连接次数: ${uiState.listenerConnectCount} · 断开次数: ${uiState.listenerDisconnectCount} · 在线时长: ${"" + (uiState.listenerTotalConnectedMs / 1000)}s",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { viewModel.requestRebind() }) { Text("重连") }
                                TextButton(onClick = { viewModel.openNotificationAccessSettings() }) { Text("去授权") }
                            }
                        }
                    )
                }
            }

            // 通知渠道直达设置入口（还原为独立卡片）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("通知渠道设置") },
                        supportingContent = { Text("若未弹横幅，请为以下渠道开启‘悬浮/锁屏/横幅’。") },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { viewModel.openPromptChannelSettings() }) { Text("确认渠道") }
                                TextButton(onClick = { viewModel.openStatusChannelSettings() }) { Text("状态渠道") }
                            }
                        }
                    )
                }
            }

            // 一键自检（新增）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("一键自检") },
                        supportingContent = {
                            val status = if (uiState.notificationListenerEnabled) "通知监听：已连接"
                            else "通知监听：未连接（请点击‘去授权’并在系统中开启使用权）"
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (!uiState.error.isNullOrBlank()) {
                                    Text(uiState.error!!, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { viewModel.runSelfCheck() }) { Text("自检") }
                                TextButton(onClick = { viewModel.requestRebind() }) { Text("重连") }
                            }
                        }
                    )
                }
            }

            // 权限与兼容性（还原为独立卡片）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("权限与兼容性") },
                        supportingContent = { Text("自动记账依赖通知使用权与通知权限。点击进入引导页进行设置。") },
                        trailingContent = { TextButton(onClick = { onNavigateToPermissionGuide() }) { Text("去设置") } }
                    )
                }
            }

            // ——— 在此之后插入“支付结果捕获方式”分组与诊断迷你卡（占位） ———
            item {
                Text(text = "支付结果捕获方式", style = MaterialTheme.typography.titleMedium)
            }
            // 还原为两张独立卡片（通知栏监听 / 无障碍）
            item {
                // 通知栏监听卡片
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("通知栏监听") },
                        supportingContent = { Text("轻量方案。依赖系统通知，推荐默认开启；支持半自动/全自动。", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = {
                            Switch(
                                checked = uiState.captureNlEnabled,
                                onCheckedChange = { viewModel.toggleCaptureNlEnabled(it) }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = {
                            val status = if (uiState.notificationListenerEnabled) "监听服务：已连接" else "监听服务：未连接"
                            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { viewModel.requestRebind() }) { Text("重连") }
                                TextButton(onClick = { viewModel.openNotificationAccessSettings() }) { Text("去授权") }
                            }
                        }
                    )
                }
            }
            item {
                // 无障碍捕获卡片（实验）
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("支付结果页辅助识别（实验）") },
                        supportingContent = { Text("重量方案。仅在支付结果页读取必要可见文本用于记账预填，不采集/上传；支持半自动/全自动。", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        trailingContent = {
                            Switch(
                                checked = uiState.captureA11yEnabled,
                                onCheckedChange = { viewModel.toggleCaptureA11yEnabled(it) }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = {
                            val a11yStatus = if (uiState.a11yGranted) "无障碍：已授权" else "无障碍：未授权（请前往系统设置开启）"
                            Text(a11yStatus, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { onNavigateToPermissionGuide() }) { Text("权限与引导") }
                                TextButton(onClick = { viewModel.openAccessibilitySettings() }) { Text("去授权") }
                            }
                        }
                    )
                }
            }

            // 诊断迷你卡（占位）
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "监听与捕获诊断（占位）", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column { Text("捕获总数"); Text("-", color = MaterialTheme.colorScheme.primary) }
                            Column { Text("跳过总数"); Text("-", color = MaterialTheme.colorScheme.primary) }
                            Column { Text("平均时延"); Text("- ms", color = MaterialTheme.colorScheme.primary) }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { /* 占位：后续跳转调试面板 */ }) { Text("查看调试记录") }
                        }
                    }
                }
            }
        }
    }
}
