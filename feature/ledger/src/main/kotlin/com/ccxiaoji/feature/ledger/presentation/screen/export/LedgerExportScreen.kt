package com.ccxiaoji.feature.ledger.presentation.screen.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.export.ExportFormat
import com.ccxiaoji.feature.ledger.presentation.viewmodel.DataType
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ExportViewModel
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerExportScreen(
    navController: NavController,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // 显示错误或成功消息
    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "关闭",
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessage()
        }
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导出记账数据") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.export() },
                        enabled = !uiState.isExporting && uiState.selectedDataTypes.isNotEmpty()
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("导出")
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 数据类型选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "选择导出数据",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    DataType.values().forEach { dataType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = dataType in uiState.selectedDataTypes,
                                onCheckedChange = { viewModel.toggleDataType(dataType) }
                            )
                            Text(
                                text = dataType.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // 时间范围选择（仅对交易记录有效）
            if (DataType.TRANSACTIONS in uiState.selectedDataTypes) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "时间范围",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showStartDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "开始日期",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = uiState.startDate?.toString() ?: "不限",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            OutlinedCard(
                                modifier = Modifier.weight(1f),
                                onClick = { showEndDatePicker = true }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "结束日期",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = uiState.endDate?.toString() ?: "不限",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // 快速选择按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    val startOfMonth = LocalDate(now.year, now.monthNumber, 1)
                                    val endOfMonth = if (now.monthNumber == 12) {
                                        LocalDate(now.year + 1, 1, 1).minus(1, DateTimeUnit.DAY)
                                    } else {
                                        LocalDate(now.year, now.monthNumber + 1, 1).minus(1, DateTimeUnit.DAY)
                                    }
                                    viewModel.setDateRange(startOfMonth, endOfMonth)
                                },
                                label = { Text("本月") }
                            )
                            
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    val lastMonth = if (now.monthNumber == 1) {
                                        LocalDate(now.year - 1, 12, 1)
                                    } else {
                                        LocalDate(now.year, now.monthNumber - 1, 1)
                                    }
                                    val endOfLastMonth = LocalDate(now.year, now.monthNumber, 1).minus(1, DateTimeUnit.DAY)
                                    viewModel.setDateRange(lastMonth, endOfLastMonth)
                                },
                                label = { Text("上月") }
                            )
                            
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                    val startOfYear = LocalDate(now.year, 1, 1)
                                    val endOfYear = LocalDate(now.year, 12, 31)
                                    viewModel.setDateRange(startOfYear, endOfYear)
                                },
                                label = { Text("今年") }
                            )
                            
                            FilterChip(
                                selected = false,
                                onClick = {
                                    viewModel.setDateRange(null, null)
                                },
                                label = { Text("全部") }
                            )
                        }
                    }
                }
            }
            
            // 导出格式选择
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "导出格式",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExportFormat.values().forEach { format ->
                            FilterChip(
                                selected = uiState.exportFormat == format,
                                onClick = { viewModel.setExportFormat(format) },
                                label = { 
                                    Text(
                                        when(format) {
                                            ExportFormat.CSV -> "CSV"
                                            ExportFormat.JSON -> "JSON"
                                            ExportFormat.EXCEL -> "Excel"
                                        }
                                    )
                                },
                                leadingIcon = if (uiState.exportFormat == format) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                    
                    // 格式说明
                    Text(
                        text = when(uiState.exportFormat) {
                            ExportFormat.CSV -> "CSV格式：简单通用，可用Excel打开"
                            ExportFormat.JSON -> "JSON格式：结构化数据，适合备份"
                            ExportFormat.EXCEL -> "Excel格式：功能丰富，直接编辑（开发中）"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            
            // 导出说明
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "导出说明",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(start = 8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• 导出的文件将保存在应用私有目录\n" +
                              "• 导出完成后会自动弹出分享选项\n" +
                              "• 选择多个数据类型时将打包为ZIP文件\n" +
                              "• CSV格式可直接用Excel或WPS打开",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
    
    // 日期选择器
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.setDateRange(date, uiState.endDate)
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                viewModel.setDateRange(uiState.startDate, date)
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                        onDateSelected(localDateTime.date)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}