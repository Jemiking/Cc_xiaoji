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
import com.ccxiaoji.feature.schedule.presentation.demo.CalendarDemoApi
import com.ccxiaoji.feature.schedule.presentation.adapter.CalendarConfigBridge
import com.ccxiaoji.feature.schedule.presentation.adapter.CalendarInteractionBridge
import com.ccxiaoji.feature.schedule.presentation.demo.parts.MonthCalendarPanel
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.shape.RoundedCornerShape
import com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode

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
    onNavigateToStyleDemo: () -> Unit = {},
    onNavigateToHomeRedesignA3Demo: () -> Unit = {},
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
    
    // ★ Demo A3组件适配层
    val configBridge = CalendarConfigBridge.create(viewModel)
    val interactionBridge = CalendarInteractionBridge.create(
        viewModel = viewModel,
        configBridge = configBridge,
        onNavigateToScheduleEdit = onNavigateToScheduleEdit
    )
    
    // 同步ViewMode状态到配置桥接器
    LaunchedEffect(viewMode) {
        configBridge.syncFromViewModel(viewMode)
    }
    
    // 转换数据格式为Demo组件需要的DemoData
    val demoData = remember(currentYearMonth, schedules) {
        CalendarDemoApi.convertToDemoData(currentYearMonth, schedules)
    }
    
    // 获取A3基线配置
    val (labelConfig, overviewConfig, indicatorStyle) = remember {
        CalendarDemoApi.getBaselineConfig()
    }
    
    
    // 年月选择对话框状态（由侧边栏触发）
    var showYearMonthPicker by remember { mutableStateOf(false) }
    // 侧边栏状态
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    
    
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = currentYearMonth.format(
                        DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_year_month))
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("选择年月") },
                    selected = false,
                    onClick = {
                        showYearMonthPicker = true
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(if (viewMode == CalendarViewMode.COMPACT) Icons.Default.ViewComfy else Icons.Default.ViewCompact, null) },
                    label = { Text(if (viewMode == CalendarViewMode.COMPACT) stringResource(R.string.schedule_calendar_view_mode_comfortable) else stringResource(R.string.schedule_calendar_view_mode_compact)) },
                    selected = false,
                    onClick = {
                        viewModel.toggleViewMode()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, null) },
                    label = { Text(stringResource(R.string.schedule_calendar_statistics_analysis)) },
                    selected = false,
                    onClick = {
                        onNavigateToStatistics()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DateRange, null) },
                    label = { Text(stringResource(R.string.schedule_calendar_batch_schedule)) },
                    selected = false,
                    onClick = {
                        onNavigateToSchedulePattern()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(stringResource(R.string.schedule_calendar_settings)) },
                    selected = false,
                    onClick = {
                        onNavigateToSettings()
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("主页设计Demo (A3)") },
                    selected = false,
                    onClick = {
                        onNavigateToHomeRedesignA3Demo()
                        scope.launch { drawerState.close() }
                    }
                )
                
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    // 显示年月文本（不可点击）
                    Text(
                        text = currentYearMonth.format(
                            DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_year_month))
                        ),
                        fontSize = params.topAppBar.titleFontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.size(params.topAppBar.actionButtonSize)) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.schedule_calendar_more))
                    }
                }
                ,
                modifier = Modifier.height(params.topAppBar.height)
            )
        },
        // 去除 FAB，按 A3 设计仅保留三点菜单入口
        floatingActionButton = {},
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = params.layout.screenHorizontalPadding)
            ) {
                Spacer(modifier = Modifier.height(params.layout.screenVerticalPadding))

                // ★ Demo A3完整组件：替换原有CalendarView为MonthCalendarPanel（含统计与底部详情卡）
                CalendarDemoApi.MonthCalendarPanel(
                    data = demoData,
                    style = indicatorStyle,
                    emphasizeNight = false,
                    dotConfig = null,
                    labelConfig = labelConfig,
                    overviewConfig = overviewConfig,
                    displayMode = configBridge.displayMode,
                    rowHeightDp = null,
                    onRequestExpand = interactionBridge::onRequestExpand,
                    onRequestCompact = interactionBridge::onRequestCompact,
                    // 日期选择和交互状态同步
                    selectedDate = selectedDate,
                    onDateSelected = interactionBridge::onDateSelected,
                    onDateLongClick = interactionBridge::onDateLongClick,
                    // 星期开始日配置
                    weekStartDay = weekStartDay,
                    // A3 底卡动作
                    onEditSelectedDate = { date -> onNavigateToScheduleEdit(date) },
                    onDeleteSelectedDate = { date -> viewModel.deleteSchedule(date) },
                    // 左右滑动切换月份
                    onSwipePrevMonth = { viewModel.navigateToPreviousMonth() },
                    onSwipeNextMonth = { viewModel.navigateToNextMonth() }
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

// （已移除 UI Demo 调试组件）
