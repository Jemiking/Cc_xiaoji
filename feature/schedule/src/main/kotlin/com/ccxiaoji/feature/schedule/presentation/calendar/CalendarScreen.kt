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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.calendar.components.MonthlyStatisticsCard
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.feature.schedule.presentation.components.CustomYearMonthPickerDialog
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
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
    viewModel: CalendarViewModel = hiltViewModel(),
    navController: NavController? = null
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
    val snackbarHostState = remember { SnackbarHostState() }
    
    android.util.Log.d("CalendarScreen", "CurrentYearMonth: $currentYearMonth, SchedulesCount: ${schedules.size}")
    
    // 溢出菜单状态
    var showDropdownMenu by remember { mutableStateOf(false) }
    // 年月选择对话框状态
    var showYearMonthPicker by remember { mutableStateOf(false) }
    
    // 处理快速班次选择结果
    navController?.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val shiftIdObserver = Observer<Long> { shiftId ->
                quickSelectDate?.let { date ->
                    val shift = if (shiftId == null || shiftId == 0L) null else quickShifts.find { it.id == shiftId }
                    viewModel.quickSetSchedule(date, shift)
                }
                savedStateHandle.remove<Long>("selected_shift_id")
            }
            
            val navigateToFullObserver = Observer<Boolean> { shouldNavigate ->
                if (shouldNavigate == true) {
                    quickSelectDate?.let { date ->
                        viewModel.hideQuickSelector()
                        onNavigateToScheduleEdit(date)
                    }
                    savedStateHandle.remove<Boolean>("navigate_to_full_selector")
                }
            }
            
            savedStateHandle.getLiveData<Long>("selected_shift_id").observe(lifecycleOwner, shiftIdObserver)
            savedStateHandle.getLiveData<Boolean>("navigate_to_full_selector").observe(lifecycleOwner, navigateToFullObserver)
            
            onDispose {
                savedStateHandle.getLiveData<Long>("selected_shift_id").removeObserver(shiftIdObserver)
                savedStateHandle.getLiveData<Boolean>("navigate_to_full_selector").removeObserver(navigateToFullObserver)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    TextButton(
                        onClick = { showYearMonthPicker = true }
                    ) {
                        Text(
                            text = currentYearMonth.format(
                                DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_year_month))
                            ),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateToToday() }) {
                        Text(
                            text = stringResource(R.string.schedule_calendar_today_short),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.schedule_calendar_more))
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
                                                stringResource(R.string.schedule_calendar_view_mode_comfortable) 
                                            else 
                                                stringResource(R.string.schedule_calendar_view_mode_compact)
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
                                        Text(stringResource(R.string.schedule_calendar_statistics_analysis))
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
                                        Text(stringResource(R.string.schedule_calendar_batch_schedule))
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
                                        Text(stringResource(R.string.schedule_calendar_settings))
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
                        onClick = { onNavigateToScheduleEdit(date) },
                        containerColor = com.ccxiaoji.ui.theme.DesignTokens.BrandColors.Schedule,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 1.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = stringResource(R.string.schedule_calendar_add_schedule),
                            tint = Color.White
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计信息卡片 - 两个模式都显示
            MonthlyStatisticsCard(
                statistics = statistics,
                modifier = Modifier.padding(horizontal = com.ccxiaoji.ui.theme.DesignTokens.Spacing.medium, vertical = com.ccxiaoji.ui.theme.DesignTokens.Spacing.small)
            )
            
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
                        modifier = Modifier.padding(horizontal = com.ccxiaoji.ui.theme.DesignTokens.Spacing.medium, vertical = com.ccxiaoji.ui.theme.DesignTokens.Spacing.small)
                    )
                }
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
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
    
    // 快速选择页面导航
    LaunchedEffect(quickSelectDate) {
        quickSelectDate?.let { date ->
            navController?.navigate(Screen.QuickShiftSelection.createRoute(date.toString()))
            viewModel.hideQuickSelector()
        }
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