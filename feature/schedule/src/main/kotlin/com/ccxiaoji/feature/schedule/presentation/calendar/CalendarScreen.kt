package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.components.CustomYearMonthPickerDialog
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

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
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val quickShifts by viewModel.quickShifts.collectAsState()
    val quickSelectDate by viewModel.quickSelectDate.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
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
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = currentYearMonth.format(
                                DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_year_month))
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.schedule_calendar_more))
                        }
                    }
                )
            },
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
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 使用完整的CalendarView组件
                    CalendarView(
                        yearMonth = currentYearMonth,
                        selectedDate = selectedDate,
                        schedules = schedules,
                        onDateSelected = { date: LocalDate -> 
                            viewModel.selectDate(date)
                        },
                        onDateLongClick = { date: LocalDate -> 
                            viewModel.showQuickSelector(date)
                        },
                        onMonthNavigate = { isNext ->
                            if (isNext) {
                                viewModel.navigateToNextMonth()
                            } else {
                                viewModel.navigateToPreviousMonth()
                            }
                        },
                        weekStartDay = weekStartDay,
                        viewMode = viewMode
                    )
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
            }
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