package com.ccxiaoji.feature.schedule.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.components.CustomDateRangePickerDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * 排班统计界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleStatisticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
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
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.schedule_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToExport) {
                        Icon(Icons.Default.Download, contentDescription = stringResource(R.string.schedule_statistics_export_data))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            statistics?.let { stats: com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics ->
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

/**
 * 时间范围选择器
 */
@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    onRangeChange: (TimeRange) -> Unit,
    onCustomDateChange: (LocalDate, LocalDate) -> Unit,
    onCustomDateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_time_range),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 预设时间范围
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.values().filter { it != TimeRange.CUSTOM }.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeChange(range) },
                        label = { 
                            Text(when (range) {
                                TimeRange.THIS_WEEK -> stringResource(R.string.schedule_statistics_time_range_this_week)
                                TimeRange.THIS_MONTH -> stringResource(R.string.schedule_statistics_time_range_this_month)
                                TimeRange.LAST_MONTH -> stringResource(R.string.schedule_statistics_time_range_last_month)
                                TimeRange.CUSTOM -> stringResource(R.string.schedule_statistics_time_range_custom)
                            })
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // 自定义时间范围
            FilterChip(
                selected = selectedRange == TimeRange.CUSTOM,
                onClick = { onRangeChange(TimeRange.CUSTOM) },
                label = { Text(stringResource(R.string.schedule_statistics_time_range_custom)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            // 自定义日期选择
            if (selectedRange == TimeRange.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DateCard(
                        label = stringResource(R.string.schedule_export_start_date),
                        date = customStartDate,
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    )
                    
                    Text(stringResource(R.string.schedule_export_to), style = MaterialTheme.typography.bodyMedium)
                    
                    DateCard(
                        label = stringResource(R.string.schedule_export_end_date),
                        date = customEndDate,
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    )
                }
            }
        }
    }
}

/**
 * 日期卡片
 */
@Composable
private fun DateCard(
    label: String,
    date: LocalDate,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 统计概览
 */
@Composable
private fun StatisticsOverview(
    statistics: com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_total_days),
                    value = statistics.totalDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_work_days),
                    value = statistics.workDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_rest_days),
                    value = statistics.restDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
            }
            
            Divider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_total_hours),
                    value = "%.1f".format(statistics.totalHours),
                    unit = stringResource(R.string.schedule_statistics_hour_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_average_hours),
                    value = if (statistics.workDays > 0) 
                        "%.1f".format(statistics.totalHours / statistics.workDays)
                    else "0",
                    unit = stringResource(R.string.schedule_statistics_hour_per_day_unit)
                )
            }
        }
    }
}

/**
 * 统计卡片
 */
@Composable
private fun StatisticCard(
    title: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                unit,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

/**
 * 班次分布图表
 */
@Composable
private fun ShiftDistributionChart(
    distribution: Map<String, Int>,
    shifts: List<Shift>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_shift_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val total = distribution.values.sum()
            
            distribution.forEach { (shiftName, count) ->
                val shift = shifts.find { it.name == shiftName }
                val percentage = if (total > 0) (count * 100f / total) else 0f
                
                ShiftDistributionBar(
                    shiftName = shiftName,
                    count = count,
                    percentage = percentage,
                    color = shift?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 班次分布条形图
 */
@Composable
private fun ShiftDistributionBar(
    shiftName: String,
    count: Int,
    percentage: Float,
    color: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                shiftName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.schedule_statistics_days_percentage, count, percentage.roundToInt()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

/**
 * 详细班次统计
 */
@Composable
private fun DetailedShiftStatistics(
    distribution: Map<String, Int>,
    totalHours: Double,
    shifts: List<Shift>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_shift_details),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            distribution.forEach { (shiftName, days) ->
                val shift = shifts.find { it.name == shiftName }
                if (shift != null) {
                    ShiftDetailRow(
                        shift = shift,
                        days = days,
                        totalHours = days * shift.duration
                    )
                    if (shiftName != distribution.keys.last()) {
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

/**
 * 班次详情行
 */
@Composable
private fun ShiftDetailRow(
    shift: Shift,
    days: Int,
    totalHours: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        Color(shift.color),
                        shape = MaterialTheme.shapes.small
                    )
            )
            
            Column {
                Text(
                    shift.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${shift.startTime} - ${shift.endTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                stringResource(R.string.schedule_statistics_days_count, days),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                stringResource(R.string.schedule_statistics_hours_count, totalHours.toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyStatisticsState() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                stringResource(R.string.schedule_statistics_empty),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.schedule_statistics_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 时间范围枚举
 */
enum class TimeRange {
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    CUSTOM
}