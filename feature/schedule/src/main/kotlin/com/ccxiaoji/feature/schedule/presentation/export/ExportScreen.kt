package com.ccxiaoji.feature.schedule.presentation.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.statistics.TimeRange
import com.ccxiaoji.feature.schedule.presentation.components.CustomDateRangePickerDialog
import com.ccxiaoji.feature.schedule.presentation.export.components.*
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens
import java.io.File
import java.time.LocalDate

/**
 * 数据导出界面 - 扁平化设计
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 时间范围选择
            item {
                TimeRangeSection(
                    selectedRange = uiState.timeRange,
                    customStartDate = uiState.customStartDate,
                    customEndDate = uiState.customEndDate,
                    onRangeChange = viewModel::updateTimeRange,
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
                ExportButton(
                    isLoading = uiState.isLoading,
                    onClick = { viewModel.exportData(context) }
                )
            }
            
            // 导出历史
            if (uiState.exportHistory.isNotEmpty()) {
                item {
                    ExportHistorySection(
                        exportHistory = uiState.exportHistory,
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
        FlatDialog(
            onDismissRequest = { viewModel.clearError() },
            title = stringResource(R.string.schedule_export_failed),
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            }
        ) {
            Text(
                error,
                color = MaterialTheme.colorScheme.onSurface
            )
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
    CSV("", "", "csv")
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