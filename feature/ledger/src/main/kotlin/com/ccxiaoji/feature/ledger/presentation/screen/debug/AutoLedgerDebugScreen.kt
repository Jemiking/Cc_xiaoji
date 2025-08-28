package com.ccxiaoji.feature.ledger.presentation.screen.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.AutoLedgerDebugRecord
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AutoLedgerDebugViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * 自动记账调试面板
 * 
 * 显示最近的通知处理记录，支持查看详情、导出数据等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoLedgerDebugScreen(
    navController: NavController,
    viewModel: AutoLedgerDebugViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("自动记账调试面板") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                // 脱敏开关
                IconButton(
                    onClick = { viewModel.toggleMaskingMode() }
                ) {
                    Icon(
                        if (uiState.maskSensitiveData) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (uiState.maskSensitiveData) "显示敏感信息" else "隐藏敏感信息"
                    )
                }
                
                // 导出按钮
                IconButton(onClick = { viewModel.exportDebugData() }) {
                    Icon(Icons.Default.FileDownload, contentDescription = "导出数据")
                }
                
                // 刷新按钮
                IconButton(onClick = { viewModel.refreshDebugData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        )
        
        // 内容区域
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error?.isNotEmpty() == true -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "未知错误",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refreshDebugData() }) {
                            Text("重试")
                        }
                    }
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 统计信息卡片
                    item {
                        StatisticsCard(statistics = uiState.statistics)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 监听配置与透传统计
                    item {
                        ListenerDiagnosticsCard(
                            diagnostics = uiState.listenerDiagnostics,
                            emitWithoutKeywords = uiState.emitWithoutKeywords,
                            emitGroupSummary = uiState.emitGroupSummary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 记录列表
                    items(uiState.debugRecords) { record ->
                        DebugRecordCard(
                            record = if (uiState.maskSensitiveData) record.masked() else record
                        )
                    }
                    
                    // 加载更多提示
                    if (uiState.debugRecords.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "暂无调试记录",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 导出成功提示
    uiState.exportSuccess?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示成功Toast
            viewModel.clearExportSuccess()
        }
    }
}

/**
 * 统计信息卡片
 */
@Composable
private fun StatisticsCard(
    statistics: AutoLedgerDebugViewModel.DebugStatistics?
) {
    if (statistics == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem("总记录", statistics.totalRecords.toString())
                StatisticItem("成功", statistics.successCount.toString())
                StatisticItem("失败", statistics.failureCount.toString())
                StatisticItem("重复", statistics.duplicateCount.toString())
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem("24h内", statistics.last24HoursRecords.toString())
                StatisticItem("平均时长", "${statistics.averageProcessingTime.toInt()}ms")
                StatisticItem("平均置信度", "${(statistics.averageConfidence * 100).toInt()}%")
            }
        }
    }
}

/**
 * 统计数据项
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 调试记录卡片
 */
@Composable
private fun DebugRecordCard(
    record: AutoLedgerDebugRecord
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 状态和时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(record.status)
                Text(
                    text = formatTimestamp(record.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 通知信息
            Text(
                text = record.notificationTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (record.parsedMerchant != null) {
                Text(
                    text = "商户: ${record.parsedMerchant}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (record.parsedAmount != null) {
                Text(
                    text = "金额: ¥${record.parsedAmount / 100.0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 处理信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "置信度: ${(record.parseConfidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${record.processingTimeMs}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 错误信息
            record.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 状态芯片
 */
@Composable
private fun StatusChip(status: AutoLedgerDebugRecord.ProcessingStatus) {
    val backgroundColor = Color(android.graphics.Color.parseColor(
        when (status) {
            AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_AUTO -> "#4CAF50"
            AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_SEMI -> "#FF9800"
            AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_DUPLICATE -> "#2196F3"
            AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_LOW_CONFIDENCE -> "#9C27B0"
            AutoLedgerDebugRecord.ProcessingStatus.FAILED_PARSE -> "#F44336"
            AutoLedgerDebugRecord.ProcessingStatus.FAILED_PROCESS -> "#F44336"
            AutoLedgerDebugRecord.ProcessingStatus.FAILED_UNKNOWN -> "#757575"
        }
    ))
    
    Surface(
        color = backgroundColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = when (status) {
                AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_AUTO -> "自动成功"
                AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_SEMI -> "半自动"
                AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_DUPLICATE -> "重复跳过"
                AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_LOW_CONFIDENCE -> "置信度低"
                AutoLedgerDebugRecord.ProcessingStatus.FAILED_PARSE -> "解析失败"
                AutoLedgerDebugRecord.ProcessingStatus.FAILED_PROCESS -> "处理失败"
                AutoLedgerDebugRecord.ProcessingStatus.FAILED_UNKNOWN -> "未知错误"
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor
        )
    }
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val date = Date(instant.toEpochMilliseconds())
    val formatter = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}

/**
 * 监听配置与透传统计卡片
 */
@Composable
private fun ListenerDiagnosticsCard(
    diagnostics: AutoLedgerDebugViewModel.ListenerDiagnostics?,
    emitWithoutKeywords: Boolean,
    emitGroupSummary: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "监听配置与透传统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "无关键词也透传: ${if (emitWithoutKeywords) "开启" else "关闭"} / 透传群组摘要: ${if (emitGroupSummary) "开启" else "关闭"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (diagnostics != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem("透传总数", diagnostics.totalEmitted.toString())
                    StatisticItem("关键词透传", diagnostics.emittedByKeywords.toString())
                    StatisticItem("无关键词透传", diagnostics.emittedWithoutKeywords.toString())
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatisticItem("跳过摘要", diagnostics.skippedGroupSummary.toString())
                    StatisticItem("跳过非白名单", diagnostics.skippedUnsupportedPackage.toString())
                    StatisticItem("配置拒绝无关键词", diagnostics.skippedNoKeywordsByConfig.toString())
                }
            } else {
                Text(
                    text = "暂无诊断数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
