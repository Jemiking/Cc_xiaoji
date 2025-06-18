package com.ccxiaoji.app.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.viewmodel.DataExportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 格式选择
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "导出格式",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.selectedFormat == ExportFormat.JSON,
                                onClick = { viewModel.selectFormat(ExportFormat.JSON) },
                                label = { Text("JSON") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.selectedFormat == ExportFormat.CSV,
                                onClick = { viewModel.selectFormat(ExportFormat.CSV) },
                                label = { Text("CSV") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.selectedFormat == ExportFormat.EXCEL,
                                onClick = { viewModel.selectFormat(ExportFormat.EXCEL) },
                                label = { Text("Excel") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // 模块选择
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "选择模块",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 记账模块
                        ExportModuleItem(
                            icon = Icons.Default.AccountBalance,
                            title = "记账数据",
                            subtitle = "交易记录、账户、分类等",
                            checked = uiState.exportLedger,
                            onCheckedChange = { viewModel.toggleLedger() }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // 待办模块
                        ExportModuleItem(
                            icon = Icons.Default.Task,
                            title = "待办任务",
                            subtitle = "所有任务记录",
                            checked = uiState.exportTodo,
                            onCheckedChange = { viewModel.toggleTodo() }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // 习惯模块
                        ExportModuleItem(
                            icon = Icons.Default.FitnessCenter,
                            title = "习惯数据",
                            subtitle = "习惯记录和打卡历史",
                            checked = uiState.exportHabit,
                            onCheckedChange = { viewModel.toggleHabit() }
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // 其他数据
                        ExportModuleItem(
                            icon = Icons.Default.MoreHoriz,
                            title = "其他数据",
                            subtitle = "倒计时、预算、储蓄目标等",
                            checked = uiState.exportOthers,
                            onCheckedChange = { viewModel.toggleOthers() }
                        )
                    }
                }
            }
            
            // 日期范围
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "日期范围",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.dateRange == DateRange.ALL,
                                onClick = { viewModel.selectDateRange(DateRange.ALL) },
                                label = { Text("全部") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.dateRange == DateRange.THIS_YEAR,
                                onClick = { viewModel.selectDateRange(DateRange.THIS_YEAR) },
                                label = { Text("本年") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = uiState.dateRange == DateRange.THIS_MONTH,
                                onClick = { viewModel.selectDateRange(DateRange.THIS_MONTH) },
                                label = { Text("本月") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // 导出按钮
            item {
                Button(
                    onClick = {
                        scope.launch {
                            val result = viewModel.exportData()
                            if (result) {
                                snackbarHostState.showSnackbar("导出成功")
                            } else {
                                snackbarHostState.showSnackbar("导出失败")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.canExport && !uiState.isExporting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始导出")
                    }
                }
            }
            
            // 导出历史
            if (uiState.exportHistory.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "导出历史",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            uiState.exportHistory.forEach { history ->
                                ExportHistoryItem(
                                    fileName = history.fileName,
                                    dateTime = history.dateTime,
                                    size = history.size,
                                    onClick = { viewModel.shareFile(history.filePath) }
                                )
                                if (history != uiState.exportHistory.last()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportModuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ExportHistoryItem(
    fileName: String,
    dateTime: String,
    size: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "$dateTime · $size",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.Share,
                contentDescription = "分享",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

enum class ExportFormat {
    JSON, CSV, EXCEL
}

enum class DateRange {
    ALL, THIS_YEAR, THIS_MONTH
}