package com.ccxiaoji.app.presentation.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.viewmodel.DataExportViewModel
import com.ccxiaoji.app.presentation.viewmodel.ExportHistory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit = {},
    viewModel: DataExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 文件保存选择器
    val fileExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/*")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                // 获取准备好的导出数据
                val exportData = viewModel.prepareExportData()
                if (exportData != null) {
                    val (content, suggestedFileName) = exportData
                    val success = viewModel.saveExportDataToUri(uri, content, suggestedFileName)
                    if (success) {
                        snackbarHostState.showSnackbar("导出成功到Download目录")
                    } else {
                        snackbarHostState.showSnackbar("导出失败")
                    }
                } else {
                    snackbarHostState.showSnackbar("准备导出数据失败")
                }
            }
        }
    }
    
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
            
            // 导出按钮（新的SAF方式）
            item {
                Button(
                    onClick = {
                        scope.launch {
                            // 准备导出数据
                            val exportData = viewModel.prepareExportData()
                            if (exportData != null) {
                                // 使用SAF让用户选择保存位置
                                val (_, fileName) = exportData
                                fileExportLauncher.launch(fileName)
                            } else {
                                snackbarHostState.showSnackbar("准备导出数据失败")
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
                        Text("选择保存位置")
                    }
                }
            }
            
            // 快速导出按钮（保持原有方式作为备选）
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val result = viewModel.exportData()
                            if (result) {
                                snackbarHostState.showSnackbar("导出成功到应用目录")
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
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("快速导出(应用目录)")
                }
            }
            
            // 导入按钮
            item {
                OutlinedButton(
                    onClick = onNavigateToImport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("导入数据")
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
                            // 导出历史头部
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.isSelectionMode) {
                                    Text(
                                        text = "已选择 ${uiState.selectedCount} 项",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        TextButton(
                                            onClick = { 
                                                if (uiState.allItemsSelected) {
                                                    viewModel.clearSelection()
                                                } else {
                                                    viewModel.selectAllItems()
                                                }
                                            }
                                        ) {
                                            Text(if (uiState.allItemsSelected) "取消全选" else "全选")
                                        }
                                        TextButton(
                                            onClick = { viewModel.exitSelectionMode() }
                                        ) {
                                            Text("取消")
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "导出历史",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            uiState.exportHistory.forEach { history ->
                                ExportHistoryItem(
                                    history = history,
                                    isSelectionMode = uiState.isSelectionMode,
                                    isSelected = uiState.selectedItems.contains(history),
                                    onLongClick = { viewModel.enterSelectionMode(history) },
                                    onClick = { 
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleItemSelection(history)
                                        } else {
                                            viewModel.shareFile(history) 
                                        }
                                    }
                                )
                                if (history != uiState.exportHistory.last()) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            // 选择模式底部操作栏
            if (uiState.isSelectionMode) {
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // 为底部固定栏留出空间
                }
            }
        }
        
        // 固定底部操作栏
        if (uiState.isSelectionMode) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SelectionBottomBar(
                    selectedCount = uiState.selectedCount,
                    hasLocalFiles = uiState.selectedItems.any { !it.isFromSAF },
                    isDeleting = uiState.isDeletingItems,
                    onDeleteRecords = {
                        scope.launch {
                            val result = viewModel.deleteSelectedItems(deleteFiles = false)
                            if (result.isSuccess) {
                                snackbarHostState.showSnackbar("已删除 ${result.deletedRecords} 条记录")
                            } else {
                                snackbarHostState.showSnackbar("删除失败：${result.error}")
                            }
                        }
                    },
                    onDeleteFiles = {
                        scope.launch {
                            val result = viewModel.deleteSelectedItems(deleteFiles = true)
                            if (result.isSuccess) {
                                val message = buildString {
                                    append("已删除 ${result.deletedRecords} 条记录")
                                    if (result.deletedFiles > 0) {
                                        append("，${result.deletedFiles} 个文件")
                                    }
                                    if (result.hasFailures) {
                                        append("，${result.failedFiles} 个文件删除失败")
                                    }
                                }
                                snackbarHostState.showSnackbar(message)
                            } else {
                                snackbarHostState.showSnackbar("删除失败：${result.error}")
                            }
                        }
                    }
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExportHistoryItem(
    history: ExportHistory,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择模式下显示复选框
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = history.fileName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (history.isFromSAF) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "云端文件",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "${history.dateTime} · ${history.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 非选择模式下显示分享按钮
        if (!isSelectionMode) {
            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SelectionBottomBar(
    selectedCount: Int,
    hasLocalFiles: Boolean,
    isDeleting: Boolean,
    onDeleteRecords: () -> Unit,
    onDeleteFiles: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "删除选项",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 删除记录按钮
            OutlinedButton(
                onClick = onDeleteRecords,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDeleting
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("仅删除记录 ($selectedCount 项)")
            }
            
            // 删除文件按钮（仅当有本地文件时显示）
            if (hasLocalFiles) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDeleteFiles,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除记录和本地文件")
                }
                
                Text(
                    text = "注意：云端文件(SAF)无法删除，仅删除记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "所选文件均为云端文件，仅可删除记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class ExportFormat {
    JSON, CSV, EXCEL
}

enum class DateRange {
    ALL, THIS_YEAR, THIS_MONTH
}