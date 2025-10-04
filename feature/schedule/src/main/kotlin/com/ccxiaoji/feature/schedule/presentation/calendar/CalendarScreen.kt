package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.presentation.components.CustomYearMonthPickerDialog
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewModel
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.navigation.Screen
import com.ccxiaoji.feature.schedule.presentation.calendar.components.MonthlyStatisticsCard
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.ui.theme.DesignTokens
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
    val statistics by viewModel.monthlyStatistics.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 年月选择对话框状态（由侧边栏触发）
    var showYearMonthPicker by remember { mutableStateOf(false) }
    // 侧边栏状态
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 移除长按快速选择流程：不再监听快速选择返回结果
    
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
                    icon = { Icon(Icons.Default.Schedule, null) },
                    label = { Text(stringResource(R.string.schedule_settings_shift_manage)) },
                    selected = false,
                    onClick = {
                        onNavigateToShiftManage()
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
            floatingActionButton = {
                if (viewMode == CalendarViewMode.COMFORTABLE) {
                    selectedDate?.let { date ->
                        FloatingActionButton(
                            onClick = { onNavigateToScheduleEdit(date) },
                            containerColor = DesignTokens.BrandColors.Schedule,
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

                    // 月度统计卡片
                    MonthlyStatisticsCard(
                        statistics = statistics,
                        modifier = Modifier.padding(
                            horizontal = DesignTokens.Spacing.medium,
                            vertical = DesignTokens.Spacing.small
                        )
                    )

                    // 使用完整的CalendarView组件
                    CalendarView(
                        yearMonth = currentYearMonth,
                        selectedDate = selectedDate,
                        schedules = schedules,
                        onDateSelected = { date: LocalDate -> 
                            viewModel.selectDate(date)
                        },
                        // 长按不再触发快速选择
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

                    // 紧凑模式：选中日期详情卡片
                    if (viewMode == CalendarViewMode.COMPACT) {
                        selectedDate?.let { date ->
                            val selectedSchedule = schedules.find { it.date == date }
                            SelectedDateDetailCard(
                                date = date,
                                schedule = selectedSchedule,
                                onEdit = { onNavigateToScheduleEdit(date) },
                                onDelete = { viewModel.deleteSchedule(date) },
                                modifier = Modifier.padding(
                                    horizontal = DesignTokens.Spacing.medium,
                                    vertical = DesignTokens.Spacing.small
                                )
                            )
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
            }
        }
    }

    // 已移除：长按进入快速班次选择页面的导航

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
