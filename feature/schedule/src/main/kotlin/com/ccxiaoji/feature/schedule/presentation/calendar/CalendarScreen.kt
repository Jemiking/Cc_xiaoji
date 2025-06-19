package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.components.QuickShiftSelector
import com.ccxiaoji.feature.schedule.presentation.components.CustomYearMonthPickerDialog
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 排班日历主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToShiftManage: () -> Unit,
    onNavigateToScheduleEdit: (LocalDate) -> Unit,
    onNavigateToSchedulePattern: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    android.util.Log.d("CalendarScreen", "CalendarScreen Composable called")
    
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val statistics by viewModel.monthlyStatistics.collectAsState()
    val quickShifts by viewModel.quickShifts.collectAsState()
    val quickSelectDate by viewModel.quickSelectDate.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    
    android.util.Log.d("CalendarScreen", "CurrentYearMonth: $currentYearMonth, SchedulesCount: ${schedules.size}")
    
    // 溢出菜单状态
    var showDropdownMenu by remember { mutableStateOf(false) }
    // 年月选择对话框状态
    var showYearMonthPicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    TextButton(
                        onClick = { showYearMonthPicker = true }
                    ) {
                        Text(
                            text = currentYearMonth.format(
                                DateTimeFormatter.ofPattern("yyyy年MM月")
                            ),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateToToday() }) {
                        Text(
                            text = "今",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (viewMode == CalendarViewMode.COMPACT) 
                                                Icons.Default.ViewComfy
                                            else 
                                                Icons.Default.ViewCompact,
                                            contentDescription = null
                                        )
                                        Text(
                                            if (viewMode == CalendarViewMode.COMPACT) 
                                                "舒适模式" 
                                            else 
                                                "紧凑模式"
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.toggleViewMode()
                                    showDropdownMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Analytics, contentDescription = null)
                                        Text("统计分析")
                                    }
                                },
                                onClick = {
                                    onNavigateToStatistics()
                                    showDropdownMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = null)
                                        Text("批量排班")
                                    }
                                },
                                onClick = {
                                    onNavigateToSchedulePattern()
                                    showDropdownMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                        Text("设置")
                                    }
                                },
                                onClick = {
                                    onNavigateToSettings()
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // 只在舒适模式下显示FAB，避免与紧凑模式的详情卡片功能重复
            if (viewMode == CalendarViewMode.COMFORTABLE) {
                selectedDate?.let { date ->
                    FloatingActionButton(
                        onClick = { onNavigateToScheduleEdit(date) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加排班")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计信息卡片 - 两个模式都显示
            statistics?.let { stats ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatisticItem("工作天数", "${stats.workDays}天")
                        StatisticItem("休息天数", "${stats.restDays}天")
                        StatisticItem("总工时", "${stats.totalHours.toInt()}小时")
                    }
                }
            }
            
            // 日历视图
            CalendarView(
                yearMonth = currentYearMonth,
                selectedDate = selectedDate,
                schedules = schedules,
                weekStartDay = weekStartDay,
                viewMode = viewMode,
                onDateSelected = { date ->
                    viewModel.selectDate(date)
                },
                onDateLongClick = { date ->
                    viewModel.showQuickSelector(date)
                },
                onMonthNavigate = { isNext ->
                    if (isNext) {
                        viewModel.navigateToNextMonth()
                    } else {
                        viewModel.navigateToPreviousMonth()
                    }
                },
                modifier = Modifier.padding(
                    top = if (viewMode == CalendarViewMode.COMPACT) 16.dp else 0.dp
                )
            )
            
            // 紧凑模式下显示选中日期详情卡片
            if (viewMode == CalendarViewMode.COMPACT) {
                selectedDate?.let { date ->
                    val selectedSchedule = schedules.find { it.date == date }
                    SelectedDateDetailCard(
                        date = date,
                        schedule = selectedSchedule,
                        onEdit = { onNavigateToScheduleEdit(date) },
                        onDelete = { viewModel.deleteSchedule(date) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示Snackbar
            viewModel.clearError()
        }
    }
    
    // 加载指示器
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // 快速选择对话框
    quickSelectDate?.let { date ->
        val currentSchedule = schedules.find { it.date == date }
        QuickShiftSelector(
            isVisible = true,
            selectedDate = date,
            quickShifts = quickShifts,
            currentShift = currentSchedule?.shift,
            onShiftSelected = { shift ->
                viewModel.quickSetSchedule(date, shift)
            },
            onDismiss = {
                viewModel.hideQuickSelector()
            },
            onNavigateToFullSelector = {
                viewModel.hideQuickSelector()
                onNavigateToScheduleEdit(date)
            }
        )
    }
    
    // 年月选择对话框
    if (showYearMonthPicker) {
        CustomYearMonthPickerDialog(
            showDialog = true,
            currentYearMonth = currentYearMonth,
            onYearMonthSelected = { yearMonth ->
                viewModel.navigateToYearMonth(yearMonth)
                showYearMonthPicker = false
            },
            onDismiss = { showYearMonthPicker = false }
        )
    }
}

// 旧的年月选择对话框已被 CustomYearMonthPickerDialog 替代

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * 选中日期详情卡片
 */
@Composable
private fun SelectedDateDetailCard(
    date: LocalDate,
    schedule: Schedule?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 日期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (schedule != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除排班",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑排班"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 班次信息
            if (schedule != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 班次颜色标识
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(schedule.shift.color),
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = schedule.shift.name.take(2),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 班次详情
                    Column {
                        Text(
                            text = schedule.shift.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (schedule.shift.startTime != null && schedule.shift.endTime != null) {
                            Text(
                                text = "${schedule.shift.startTime} - ${schedule.shift.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "工时：${String.format("%.1f", schedule.shift.duration)}小时",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // 无排班时的提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "当日无排班",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}