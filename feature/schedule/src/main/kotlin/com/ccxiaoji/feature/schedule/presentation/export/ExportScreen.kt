package com.ccxiaoji.feature.schedule.presentation.export

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.statistics.TimeRange
import com.ccxiaoji.feature.schedule.presentation.components.CustomDateRangePickerDialog
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        weekStartDay = weekStartDay
    )
    
    // 文件分享启动器
    val shareFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_export_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.schedule_back))
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
                        Text(stringResource(R.string.schedule_export_button))
                    }
                }
            }
            
            // 导出历史
            if (uiState.exportHistory.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.schedule_export_history),
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
            title = { Text(stringResource(R.string.schedule_export_failed)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.schedule_confirm))
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
                stringResource(R.string.schedule_export_time_range),
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
                        label = { 
                            Text(when (range) {
                                TimeRange.THIS_WEEK -> stringResource(R.string.schedule_statistics_time_range_this_week)
                                TimeRange.THIS_MONTH -> stringResource(R.string.schedule_statistics_time_range_this_month)
                                TimeRange.LAST_MONTH -> stringResource(R.string.schedule_statistics_time_range_last_month)
                                TimeRange.CUSTOM -> stringResource(R.string.schedule_statistics_time_range_custom)
                            })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            FilterChip(
                selected = selectedRange == TimeRange.CUSTOM,
                onClick = { onRangeChange(TimeRange.CUSTOM) },
                label = { Text(stringResource(R.string.schedule_statistics_time_range_custom)) },
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
                                stringResource(R.string.schedule_export_start_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    Text(stringResource(R.string.schedule_export_to))
                    
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.schedule_export_end_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
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
                stringResource(R.string.schedule_export_format),
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
                            when (format) {
                                ExportFormat.CSV -> stringResource(R.string.schedule_export_format_csv)
                                ExportFormat.JSON -> stringResource(R.string.schedule_export_format_json)
                                ExportFormat.REPORT -> stringResource(R.string.schedule_export_format_report)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            when (format) {
                                ExportFormat.CSV -> stringResource(R.string.schedule_export_format_csv_desc)
                                ExportFormat.JSON -> stringResource(R.string.schedule_export_format_json_desc)
                                ExportFormat.REPORT -> stringResource(R.string.schedule_export_format_report_desc)
                            },
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
                stringResource(R.string.schedule_export_options),
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
                    stringResource(R.string.schedule_export_include_statistics),
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
                    stringResource(R.string.schedule_export_include_actual_time),
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
                    stringResource(R.string.schedule_export_time, exportInfo.exportTime.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    )),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { onShare(exportInfo.file) }) {
                Icon(Icons.Default.Share, contentDescription = stringResource(R.string.schedule_export_share))
            }
            
            IconButton(onClick = { onDelete(exportInfo.file) }) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.schedule_export_delete))
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
    
    val chooser = Intent.createChooser(intent, context.getString(R.string.schedule_export_share_title))
    launcher.launch(chooser)
}

/**
 * 导出格式
 */
enum class ExportFormat(
    val displayName: String,
    val description: String,
    val extension: String
) {
    CSV("", "", "csv"),
    JSON("", "", "json"),
    REPORT("", "", "txt")
}

/**
 * 导出信息
 */
data class ExportInfo(
    val file: File,
    val fileName: String,
    val format: ExportFormat,
    val exportTime: java.time.LocalDateTime
)