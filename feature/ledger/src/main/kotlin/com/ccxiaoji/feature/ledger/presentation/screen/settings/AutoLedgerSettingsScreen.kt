package com.ccxiaoji.feature.ledger.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
// combinedClickable 不必使用，点击一次即可触发连点逻辑
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoLedgerSettingsViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoMode
import kotlinx.coroutines.launch
import com.ccxiaoji.feature.ledger.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLedgerSettingsScreen(
    navController: NavController,
    onNavigateToPermissionGuide: () -> Unit = {},
    viewModel: AutoLedgerSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDeveloperMode = BuildConfig.DEBUG || uiState.developerModeEnabled
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var devTapCount by remember { mutableStateOf(0) }
    var advancedExpanded by remember { mutableStateOf(false) }
    var showConfirmGroupSummary by remember { mutableStateOf(false) }
    var showConfirmLogUnmatched by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 标题支持“连点7次”启用/关闭开发者模式（仅 Release 生效）
                    Text(
                        text = "自动记账设置",
                        modifier = Modifier.clickable {
                            if (!BuildConfig.DEBUG) {
                                devTapCount += 1
                                if (devTapCount >= 7) {
                                    devTapCount = 0
                                    viewModel.toggleDeveloperMode()
                                    scope.launch {
                                        val on = !uiState.developerModeEnabled
                                        snackbarHostState.showSnackbar(
                                            if (on) "开发者模式已启用" else "开发者模式已关闭"
                                        )
                                    }
                                }
                            }
                        }
                    )
                },
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
            // 基础：总开关 + 模式选择
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
                Divider()
                ListItem(
                    headlineContent = { Text("模式选择") },
                    supportingContent = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row { 
                                RadioButton(selected = uiState.selectedMode == AutoMode.SEMI, onClick = { viewModel.setSelectedMode(AutoMode.SEMI) })
                                Text("半自动（默认）")
                            }
                            Row { 
                                RadioButton(selected = uiState.selectedMode == AutoMode.ALIPAY_AUTO, onClick = { viewModel.setSelectedMode(AutoMode.ALIPAY_AUTO) })
                                Text("支付宝自动入账（方案A）")
                            }
                            Row { 
                                RadioButton(selected = uiState.selectedMode == AutoMode.GEN_AUTO, onClick = { viewModel.setSelectedMode(AutoMode.GEN_AUTO) })
                                Text("通用自动创建（阈值/金额）")
                            }
                        }
                    }
                )
                Divider()
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

            // 通知渠道直达设置入口
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

            // 通用自动创建（仅在模式=GEN_AUTO时展示）
            if (uiState.selectedMode == AutoMode.GEN_AUTO) Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("自动创建交易") },
                    supportingContent = { Text("高于阈值的解析结果自动入账；可随时关闭降为半自动。") },
                    trailingContent = {
                        Switch(
                            checked = uiState.autoCreateEnabled,
                            onCheckedChange = { checked -> viewModel.toggleAutoCreate(checked) }
                        )
                    }
                )
            }

            // 方案A：支付宝默认自动入账（仅在模式=ALIPAY_AUTO时展示）
            if (uiState.selectedMode == AutoMode.ALIPAY_AUTO) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("支付宝自动入账（方案A）") },
                        supportingContent = { Text("仅凭金额与方向自动入账；需选择默认账户与默认分类。") },
                        trailingContent = {
                            Switch(
                                checked = uiState.alipayAutoOn,
                                onCheckedChange = { checked -> viewModel.toggleAlipayAuto(checked) }
                            )
                        }
                    )
                    Divider()
                    // 账户选择
                    val accExpanded = remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text("默认支付宝账户") },
                        supportingContent = { Text(uiState.accounts.firstOrNull { it.id == uiState.alipayDefaultAccountId }?.name ?: "未选择") },
                        trailingContent = {
                            TextButton(onClick = { accExpanded.value = true }) { Text("选择") }
                        }
                    )
                    DropdownMenu(expanded = accExpanded.value, onDismissRequest = { accExpanded.value = false }) {
                        uiState.accounts.take(50).forEach { opt ->
                            DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                viewModel.setAlipayDefaultAccount(opt.id)
                                accExpanded.value = false
                            })
                        }
                    }
                    Divider()
                    // 支出分类选择
                    val expExpanded = remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text("默认支出分类") },
                        supportingContent = { Text(uiState.expenseCategories.firstOrNull { it.id == uiState.defaultExpenseCategoryId }?.name ?: "未选择") },
                        trailingContent = {
                            TextButton(onClick = { expExpanded.value = true }) { Text("选择") }
                        }
                    )
                    DropdownMenu(expanded = expExpanded.value, onDismissRequest = { expExpanded.value = false }) {
                        uiState.expenseCategories.take(50).forEach { opt ->
                            DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                viewModel.setDefaultExpenseCategory(opt.id)
                                expExpanded.value = false
                            })
                        }
                    }
                    Divider()
                    // 收入分类选择
                    val incExpanded = remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text("默认收入分类") },
                        supportingContent = { Text(uiState.incomeCategories.firstOrNull { it.id == uiState.defaultIncomeCategoryId }?.name ?: "未选择") },
                        trailingContent = {
                            TextButton(onClick = { incExpanded.value = true }) { Text("选择") }
                        }
                    )
                    DropdownMenu(expanded = incExpanded.value, onDismissRequest = { incExpanded.value = false }) {
                        uiState.incomeCategories.take(50).forEach { opt ->
                            DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                viewModel.setDefaultIncomeCategory(opt.id)
                                incExpanded.value = false
                            })
                        }
                    }
                }
            }

            // 旧的“高级设置”块移除，统一收敛到下方“开发者设置”折叠区

            // 开发者设置（迁移为独立页面；原折叠区禁用）
            if (false) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    // 折叠区标题
                    ListItem(
                        headlineContent = { Text("开发者设置") },
                        supportingContent = { Text(if (advancedExpanded) "点击折叠" else "点击展开") },
                        trailingContent = {
                            TextButton(onClick = { advancedExpanded = !advancedExpanded }) {
                                Text(if (advancedExpanded) "收起" else "展开")
                            }
                        },
                        modifier = Modifier.clickable { advancedExpanded = !advancedExpanded }
                    )
                }

                if (advancedExpanded) {
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
                                        scope.launch {
                                            snackbarHostState.showSnackbar("已清空去重缓存：$cleared 条")
                                        }
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
                                    onCheckedChange = { checked -> viewModel.toggleEmitWithoutKeywords(checked) }
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
                                    onCheckedChange = { checked ->
                                        if (checked && !uiState.emitGroupSummary) {
                                            showConfirmGroupSummary = true
                                        } else {
                                            viewModel.toggleEmitGroupSummary(checked)
                                        }
                                    }
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
                                    onCheckedChange = { checked ->
                                        if (checked && !uiState.logUnmatchedNotifications) {
                                            showConfirmLogUnmatched = true
                                        } else {
                                            viewModel.toggleLogUnmatched(checked)
                                        }
                                    }
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
                }
            }

            // 新的开发者设置入口（仅开发者模式可见）
            if (isDeveloperMode) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("开发者设置") },
                        supportingContent = { Text("去重/过滤/日志/阈值等调试项") },
                        trailingContent = {
                            TextButton(onClick = { navController.navigate("auto_ledger_dev_settings") }) { Text("进入") }
                        }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("权限与兼容性") },
                    supportingContent = { Text("自动记账依赖通知使用权与通知权限。点击进入引导页进行设置。") },
                    trailingContent = {
                        TextButton(onClick = { onNavigateToPermissionGuide() }) { Text("去设置") }
                    }
                )
            }

            // 调试：最近5条交易弹窗
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

            // 高风险确认弹窗：群组摘要透传
            if (showConfirmGroupSummary) {
                AlertDialog(
                    onDismissRequest = { showConfirmGroupSummary = false },
                    title = { Text("开启高风险选项？") },
                    text = { Text("透传群组摘要可能误触发自动记账，确认开启？") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.toggleEmitGroupSummary(true)
                            showConfirmGroupSummary = false
                        }) { Text("确认开启") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmGroupSummary = false }) { Text("取消") }
                    }
                )
            }
            // 高风险确认弹窗：未匹配日志
            if (showConfirmLogUnmatched) {
                AlertDialog(
                    onDismissRequest = { showConfirmLogUnmatched = false },
                    title = { Text("开启高风险选项？") },
                    text = { Text("记录未匹配日志可能包含大量噪音与隐私，确认开启？") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.toggleLogUnmatched(true)
                            showConfirmLogUnmatched = false
                        }) { Text("确认开启") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmLogUnmatched = false }) { Text("取消") }
                    }
                )
            }
        }
    }
}
