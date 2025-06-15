package com.ccxiaoji.feature.schedule.presentation.ui.export

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.ccxiaoji.feature.schedule.presentation.ui.components.CustomDateRangePickerDialog
import com.ccxiaoji.feature.schedule.presentation.ui.statistics.TimeRange
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ExportViewModel

/**
 * 数据导出界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weekStartDay by viewModel.weekStartDay.collectAsStateWithLifecycle()
    
    // 日期选择器状态
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    // 日期范围选择器
    CustomDateRangePickerDialog(
        showDialog = showDateRangePicker,
        initialStartDate = uiState.customStartDate,
        initialEndDate = uiState.customEndDate,
        onDateRangeSelected = { start, end ->
            viewModel.updateCustomDateRange(start, end)
        },
        onDismiss = { showDateRangePicker = false },
        weekStartDay = weekStartDay.value
    )
    
    // 文件分享启动器
    val shareFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导出") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 时间范围选择
            item {
                TimeRangeSection(
                    selectedRange = uiState.timeRange,
                    customStartDate = uiState.customStartDate,
                    customEndDate = uiState.customEndDate,
                    onRangeChange = viewModel::updateTimeRange,
                    onCustomDateChange = viewModel::updateCustomDateRange,
                    onCustomDateClick = { showDateRangePicker = true }
                )
            }
            
            // 导出格式选择
            item {
                ExportFormatSection(
                    selectedFormat = uiState.exportFormat,
                    onFormatChange = viewModel::updateExportFormat
                )
            }
            
            // 导出选项
            item {
                ExportOptionsSection(
                    includeStatistics = uiState.includeStatistics,
                    includeActualTime = uiState.includeActualTime,
                    onIncludeStatisticsChange = viewModel::updateIncludeStatistics,
                    onIncludeActualTimeChange = viewModel::updateIncludeActualTime
                )
            }
            
            // 导出按钮
            item {
                Button(
                    onClick = {
                        viewModel.exportData(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("导出数据")
                    }
                }
            }
            
            // 导出历史
            if (uiState.exportHistory.isNotEmpty()) {
                item {
                    Text(
                        "导出历史",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                uiState.exportHistory.forEach { exportInfo ->
                    item {
                        ExportHistoryItem(
                            exportInfo = exportInfo,
                            onShare = { file ->
                                shareFile(context, file, shareFileLauncher)
                            },
                            onDelete = { file ->
                                viewModel.deleteExportFile(file)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // 显示成功消息
    uiState.exportedFile?.let { file ->
        LaunchedEffect(file) {
            // 自动分享文件
            shareFile(context, file, shareFileLauncher)
            viewModel.clearExportedFile()
        }
    }
    
    // 显示错误消息
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("导出失败") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 时间范围选择部分
 */
@Composable
private fun TimeRangeSection(
    selectedRange: TimeRange,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    onRangeChange: (TimeRange) -> Unit,
    onCustomDateChange: (LocalDate, LocalDate) -> Unit,
    onCustomDateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "导出时间范围",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.values().filter { it != TimeRange.CUSTOM }.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeChange(range) },
                        label = { Text(range.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            FilterChip(
                selected = selectedRange == TimeRange.CUSTOM,
                onClick = { onRangeChange(TimeRange.CUSTOM) },
                label = { Text(TimeRange.CUSTOM.displayName) },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (selectedRange == TimeRange.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "开始日期",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customStartDate.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Text("至")
                    
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "结束日期",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customEndDate.format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 导出格式选择部分
 */
@Composable
private fun ExportFormatSection(
    selectedFormat: ExportFormat,
    onFormatChange: (ExportFormat) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "导出格式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            ExportFormat.values().forEach { format ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedFormat == format,
                        onClick = { onFormatChange(format) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            format.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            format.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        when (format) {
                            ExportFormat.CSV -> Icons.Default.TableChart
                            ExportFormat.JSON -> Icons.Default.Code
                            ExportFormat.REPORT -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 导出选项部分
 */
@Composable
private fun ExportOptionsSection(
    includeStatistics: Boolean,
    includeActualTime: Boolean,
    onIncludeStatisticsChange: (Boolean) -> Unit,
    onIncludeActualTimeChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "导出选项",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeStatistics,
                    onCheckedChange = onIncludeStatisticsChange
                )
                Text(
                    "包含统计信息",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeActualTime,
                    onCheckedChange = onIncludeActualTimeChange
                )
                Text(
                    "包含实际打卡时间",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 导出历史项
 */
@Composable
private fun ExportHistoryItem(
    exportInfo: ExportInfo,
    onShare: (File) -> Unit,
    onDelete: (File) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (exportInfo.format) {
                    ExportFormat.CSV -> Icons.Default.TableChart
                    ExportFormat.JSON -> Icons.Default.Code
                    ExportFormat.REPORT -> Icons.Default.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    exportInfo.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "导出时间: ${exportInfo.exportTime.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    )}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { onShare(exportInfo.file) }) {
                Icon(Icons.Default.Share, contentDescription = "分享")
            }
            
            IconButton(onClick = { onDelete(exportInfo.file) }) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}

/**
 * 分享文件
 */
private fun shareFile(
    context: Context,
    file: File,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = when (file.extension) {
            "csv" -> "text/csv"
            "json" -> "application/json"
            "txt" -> "text/plain"
            else -> "*/*"
        }
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    
    val chooser = Intent.createChooser(intent, "分享导出数据")
    launcher.launch(chooser)
}
