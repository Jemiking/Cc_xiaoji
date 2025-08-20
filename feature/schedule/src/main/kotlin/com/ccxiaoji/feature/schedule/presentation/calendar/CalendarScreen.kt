package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.presentation.debug.DefaultDebugParams
import com.ccxiaoji.feature.schedule.presentation.debug.CalendarViewParams

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
    onNavigateToDebug: () -> Unit = {},
    onNavigateToFlatDemo: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    viewModel: CalendarViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    // 同步调试参数到正式首页的默认布局
    val params = remember { DefaultDebugParams.default }

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
                            fontSize = params.topAppBar.titleFontSize,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let { onBack ->
                        IconButton(onClick = onBack, modifier = Modifier.size(params.topAppBar.actionButtonSize)) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.schedule_back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateToToday() }, modifier = Modifier.size(params.topAppBar.actionButtonSize)) {
                        Text(
                            text = stringResource(R.string.schedule_calendar_today_short),
                            fontSize = params.topAppBar.todayButtonTextSize,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }, modifier = Modifier.size(params.topAppBar.actionButtonSize)) {
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
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.BugReport, contentDescription = null)
                                        Text("UI调试器")
                                    }
                                },
                                onClick = {
                                    onNavigateToDebug()
                                    showDropdownMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.ViewDay, contentDescription = null)
                                        Text("扁平Demo")
                                    }
                                },
                                onClick = {
                                    onNavigateToFlatDemo()
                                    showDropdownMenu = false
                                }
                            )
                            
                        }
                    }
                }
                ,
                modifier = Modifier.height(params.topAppBar.height)
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
                            defaultElevation = params.fab.elevation,
                            pressedElevation = params.fab.elevation + 2.dp
                        ),
                        modifier = Modifier.size(params.fab.size)
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
                .padding(horizontal = params.layout.screenHorizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(params.layout.screenVerticalPadding))

            // 统计信息卡片（按参数包裹卡片）
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = params.layout.componentSpacing / 2),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(params.statisticsCard.cornerRadius),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = params.statisticsCard.elevation),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(modifier = Modifier.padding(params.statisticsCard.padding)) {
                    MonthlyStatisticsCard(
                        statistics = statistics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 日历视图
            CalendarView(
                yearMonth = currentYearMonth,
                selectedDate = selectedDate,
                schedules = schedules,
                weekStartDay = weekStartDay,
                viewMode = viewMode,
                debugParams = params.calendarView,
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
                    top = if (viewMode == CalendarViewMode.COMPACT) params.layout.componentSpacing else 0.dp
                )
            )
            
            // 紧凑模式下显示选中日期详情卡片
            if (viewMode == CalendarViewMode.COMPACT) {
                selectedDate?.let { date ->
                    val selectedSchedule = schedules.find { it.date == date }
                    Spacer(modifier = Modifier.height(params.layout.componentSpacing))
                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(params.detailCard.cornerRadius),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = params.detailCard.elevation),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(modifier = Modifier.padding(params.detailCard.padding)) {
                            SelectedDateDetailCard(
                                date = date,
                                schedule = selectedSchedule,
                                onEdit = { onNavigateToScheduleEdit(date) },
                                onDelete = { viewModel.deleteSchedule(date) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
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

// （已移除 UI Demo 调试组件）
