package com.ccxiaoji.feature.schedule.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarView
import com.ccxiaoji.feature.schedule.presentation.calendar.components.MonthlyStatisticsCard
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.feature.schedule.presentation.components.CustomYearMonthPickerDialog
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 可调节的排班日历主界面（调试版本）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugCalendarScreen(
    params: DebugCalendarParams,
    onNavigateToShiftManage: () -> Unit = {},
    onNavigateToScheduleEdit: (LocalDate) -> Unit = {},
    onNavigateToSchedulePattern: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDebug: () -> Unit = {},
    onNavigateToFlatDemo: () -> Unit = {},
    onNavigateToStyleDemo: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    
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
            // 应用TopAppBar调试参数
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
                            fontWeight = params.topAppBar.titleFontWeight,
                            color = if (params.topAppBar.titleTextColor != Color.Unspecified) 
                                params.topAppBar.titleTextColor 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateToToday() },
                        modifier = Modifier.size(params.topAppBar.actionButtonSize)
                    ) {
                        Text(
                            text = stringResource(R.string.schedule_calendar_today_short),
                            fontSize = params.topAppBar.todayButtonTextSize,
                            fontWeight = params.topAppBar.todayButtonFontWeight
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showDropdownMenu = true },
                            modifier = Modifier.size(params.topAppBar.actionButtonSize)
                        ) {
                            Icon(
                                Icons.Default.MoreVert, 
                                contentDescription = stringResource(R.string.schedule_calendar_more),
                                modifier = Modifier.size(params.topAppBar.moreIconSize)
                            )
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
                                        Icon(Icons.Default.ViewDay, contentDescription = null)
                                        Text("扁平Demo")
                                    }
                                },
                                onClick = {
                                    onNavigateToFlatDemo()
                                    showDropdownMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Palette, contentDescription = null)
                                        Text("设计风格Demo")
                                    }
                                },
                                onClick = {
                                    android.util.Log.d("ScheduleNavHost", "DebugCalendarScreen StyleDemo button clicked!")
                                    onNavigateToStyleDemo()
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (params.topAppBar.backgroundColor != Color.Transparent) 
                        params.topAppBar.backgroundColor 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .height(params.topAppBar.height)
                    .background(
                        if (params.topAppBar.backgroundColor != Color.Transparent) 
                            params.topAppBar.backgroundColor 
                        else 
                            Color.Transparent
                    )
            )
        },
        floatingActionButton = {
            // 应用FAB调试参数
            if (viewMode == CalendarViewMode.COMFORTABLE) {
                selectedDate?.let { date ->
                    FloatingActionButton(
                        onClick = { onNavigateToScheduleEdit(date) },
                        containerColor = if (params.fab.backgroundColor != Color.Unspecified) 
                            params.fab.backgroundColor 
                        else 
                            params.theme.scheduleModuleAccentColor,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = params.fab.elevation,
                            pressedElevation = params.fab.elevation + 2.dp
                        ),
                        modifier = Modifier.size(params.fab.size)
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            contentDescription = stringResource(R.string.schedule_calendar_add_schedule),
                            tint = params.fab.iconColor
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
            
            // 应用统计卡片调试参数
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = params.layout.componentSpacing / 2),
                shape = RoundedCornerShape(params.statisticsCard.cornerRadius),
                elevation = CardDefaults.cardElevation(defaultElevation = params.statisticsCard.elevation),
                colors = CardDefaults.cardColors(
                    containerColor = if (params.statisticsCard.backgroundColor != Color.Unspecified) 
                        params.statisticsCard.backgroundColor 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (params.statisticsCard.borderWidth > 0.dp) {
                    androidx.compose.foundation.BorderStroke(
                        params.statisticsCard.borderWidth, 
                        params.statisticsCard.borderColor
                    )
                } else null
            ) {
                Box(
                    modifier = Modifier.padding(params.statisticsCard.padding)
                ) {
                    MonthlyStatisticsCard(
                        statistics = statistics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(params.layout.componentSpacing))
            
            // 应用日历视图调试参数
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
            
            // 应用详情卡片调试参数
            if (viewMode == CalendarViewMode.COMPACT) {
                selectedDate?.let { date ->
                    val selectedSchedule = schedules.find { it.date == date }
                    Spacer(modifier = Modifier.height(params.layout.componentSpacing))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(params.detailCard.cornerRadius),
                        elevation = CardDefaults.cardElevation(defaultElevation = params.detailCard.elevation),
                        colors = CardDefaults.cardColors(
                            containerColor = if (params.detailCard.backgroundColor != Color.Unspecified) 
                                params.detailCard.backgroundColor 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(params.detailCard.padding)
                        ) {
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