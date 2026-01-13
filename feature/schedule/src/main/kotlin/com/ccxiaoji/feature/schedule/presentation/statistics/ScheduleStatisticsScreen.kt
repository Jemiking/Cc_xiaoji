package com.ccxiaoji.feature.schedule.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.components.CustomDateRangePickerDialog
import com.ccxiaoji.feature.schedule.presentation.statistics.components.*
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleTopAppBar
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 排班统计界面 - 使用 UI Kit 组件
 */
@Composable
fun ScheduleStatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScheduleStatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val weekStartDay by viewModel.weekStartDay.collectAsStateWithLifecycle()
    
    // 日期选择器状态
    var showDateRangePicker by remember { mutableStateOf(false) }
    
    // 日期范围选择器
    CustomDateRangePickerDialog(
        showDialog = showDateRangePicker,
        initialStartDate = uiState.customStartDate,
        initialEndDate = uiState.customEndDate,
        onDateRangeSelected = { start, end ->
            viewModel.updateCustomDateRange(start, end)
        },
        onDismiss = { showDateRangePicker = false },
        weekStartDay = weekStartDay
    )
    
    Scaffold(
        topBar = {
            ScheduleTopAppBar(
                title = stringResource(R.string.schedule_statistics_title),
                onNavigationClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 时间范围选择
            item {
                TimeRangeSelector(
                    selectedRange = uiState.timeRange,
                    customStartDate = uiState.customStartDate,
                    customEndDate = uiState.customEndDate,
                    onRangeChange = viewModel::updateTimeRange,
                    onCustomDateChange = viewModel::updateCustomDateRange,
                    onCustomDateClick = { showDateRangePicker = true }
                )
            }
            
            // 统计概览
            statistics?.let { stats ->
                item {
                    StatisticsOverview(statistics = stats)
                }
                
                // 班次分布图表
                if (stats.shiftDistribution.isNotEmpty()) {
                    item {
                        ShiftDistributionChart(
                            distribution = stats.shiftDistribution,
                            shifts = shifts
                        )
                    }
                }
                
                // 详细班次统计
                item {
                    DetailedShiftStatistics(
                        distribution = stats.shiftDistribution,
                        totalHours = stats.totalHours,
                        shifts = shifts
                    )
                }
            }
            
            // 空状态
            if (statistics == null || statistics?.totalDays == 0) {
                item {
                    EmptyStatisticsState()
                }
            }
        }
    }
    
    // 加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}