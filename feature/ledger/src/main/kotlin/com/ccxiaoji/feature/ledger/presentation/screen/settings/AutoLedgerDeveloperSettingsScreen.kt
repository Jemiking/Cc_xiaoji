package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoLedgerSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLedgerDeveloperSettingsScreen(
    navController: NavController,
    viewModel: AutoLedgerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("自动记账 · 开发者设置") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 去重设置
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("启用去重") },
                    trailingContent = {
                        Switch(
                            checked = uiState.dedupEnabled,
                            onCheckedChange = { viewModel.toggleDedupEnabled(it) }
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("去重窗口（秒）") },
                    supportingContent = { Text("建议 10~60 秒；过小可能导致重复入账") },
                    trailingContent = {
                        Row(modifier = Modifier.fillMaxWidth(0.7f), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = uiState.dedupWindowSec.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row {
                                TextButton(onClick = { viewModel.updateDedupWindowSec((uiState.dedupWindowSec - 1).coerceAtLeast(1)) }) { Text("-1") }
                                TextButton(onClick = { viewModel.updateDedupWindowSec((uiState.dedupWindowSec + 1).coerceAtMost(600)) }) { Text("+1") }
                            }
                        }
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("去重跳过仍解析（采样）") },
                    supportingContent = { Text("用于调试去重误判场景，默认关闭") },
                    trailingContent = {
                        Switch(
                            checked = uiState.dedupDebugParseOnSkip,
                            onCheckedChange = { viewModel.toggleDedupDebugParse(it) }
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("去重缓存") },
                    supportingContent = { Text("清空当前去重缓存（不影响历史流水）") },
                    trailingContent = {
                        TextButton(onClick = {
                            viewModel.clearDedupCache { cleared ->
                                scope.launch { snackbarHostState.showSnackbar("已清空去重缓存：$cleared 条") }
                            }
                        }) { Text("清空") }
                    }
                )
            }

            // 监听层过滤（调试）
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("放宽关键词过滤") },
                    supportingContent = { Text("对白名单应用（支付宝/微信/云闪付）即透传事件，降低漏判。") },
                    trailingContent = {
                        Switch(
                            checked = uiState.emitWithoutKeywords,
                            onCheckedChange = { viewModel.toggleEmitWithoutKeywords(it) }
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("透传群组摘要（高风险）") },
                    supportingContent = { Text("群组摘要多为聚合通知，默认跳过；仅在必要时临时开启调试。") },
                    trailingContent = {
                        Switch(
                            checked = uiState.emitGroupSummary,
                            onCheckedChange = { checked -> viewModel.toggleEmitGroupSummary(checked) }
                        )
                    }
                )
            }

            // 通用自动创建参数
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("自动创建置信度阈值") },
                    supportingContent = { Text(String.format("当前阈值：%.2f（建议0.80~0.90）", uiState.autoCreateConfidenceThreshold)) },
                    trailingContent = {
                        Slider(
                            value = uiState.autoCreateConfidenceThreshold,
                            onValueChange = { v -> viewModel.updateAutoCreateThreshold(v) },
                            valueRange = 0.5f..0.95f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("最小金额（分）") },
                    supportingContent = { Text("低于该金额不自动入账，可仅提示或降权") },
                    trailingContent = {
                        Row(modifier = Modifier.fillMaxWidth(0.6f), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = uiState.minAmountCents.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row {
                                TextButton(onClick = { viewModel.updateMinAmountCents((uiState.minAmountCents - 10).coerceAtLeast(0)) }) { Text("-10") }
                                TextButton(onClick = { viewModel.updateMinAmountCents(uiState.minAmountCents + 10) }) { Text("+10") }
                            }
                        }
                    }
                )
            }

            // 调试/诊断
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("记录未匹配通知日志（高风险）") },
                    supportingContent = { Text("关闭可降噪；开启用于诊断漏判来源") },
                    trailingContent = {
                        Switch(
                            checked = uiState.logUnmatchedNotifications,
                            onCheckedChange = { viewModel.toggleLogUnmatched(it) }
                        )
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("调试工具") },
                    supportingContent = { Text("打印最近5条交易，便于核对自动入账结果") },
                    trailingContent = {
                        TextButton(onClick = { viewModel.printRecentTransactions(5) }) { Text("打印最近5条交易") }
                    }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("恢复默认设置") },
                    supportingContent = { Text("重置开发者设置为推荐值（不影响用户基础设置）") },
                    trailingContent = {
                        TextButton(onClick = {
                            viewModel.resetDeveloperSettings()
                            scope.launch { snackbarHostState.showSnackbar("已恢复调试设置为默认") }
                        }) { Text("恢复默认") }
                    }
                )
            }

            if (uiState.debugRecentTx.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRecentPreview() },
                    title = { Text("最近5条交易") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            uiState.debugRecentTx.forEach { line -> Text(line) }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRecentPreview() }) { Text("关闭") }
                    }
                )
            }

            if (uiState.error != null) {
                Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

