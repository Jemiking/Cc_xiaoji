package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import kotlin.math.abs
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * 日历视图组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit = {},
    onMonthNavigate: (Boolean) -> Unit = {}, // true表示下一月，false表示上一月
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    viewMode: CalendarViewMode = CalendarViewMode.COMFORTABLE,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("CalendarView", "Rendering calendar for: $yearMonth, schedules count: ${schedules.size}")
    
    // 根据视图模式动态调整尺寸参数
    val gridSpacing = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 4.dp   // 舒适模式：较小间距以增大格子
        CalendarViewMode.COMPACT -> 6.dp       // 紧凑模式：标准间距
    }
    
    val horizontalPadding = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 6.dp   // 舒适模式：较小边距以增大格子
        CalendarViewMode.COMPACT -> 8.dp       // 紧凑模式：标准边距
    }
    
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    
    // 计算第一天相对于一周起始日的偏移量
    val firstDayOffset = when (weekStartDay) {
        DayOfWeek.SUNDAY -> firstDayOfMonth.dayOfWeek.value % 7 // 周日开始：周日=0
        DayOfWeek.MONDAY -> (firstDayOfMonth.dayOfWeek.value - 1) % 7 // 周一开始：周一=0
        else -> (firstDayOfMonth.dayOfWeek.value - 1) % 7 // 默认周一开始
    }
    
    // 创建日历网格数据
    val calendarDays = remember(yearMonth, weekStartDay) {
        val days = mutableListOf<LocalDate?>()
        // 添加月初的空白天数
        repeat(firstDayOffset) {
            days.add(null)
        }
        // 添加当月所有天数
        for (day in 1..daysInMonth) {
            days.add(yearMonth.atDay(day))
        }
        days
    }
    
    // 创建日期到排班的映射
    val scheduleMap = remember(schedules) {
        schedules.associateBy { it.date }
    }
    
    // 滑动手势状态
    var totalDragAmount by remember { mutableFloatStateOf(0f) }
    
    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragAmount = 0f
                    },
                    onDragEnd = {
                        // 拖拽结束时判断是否切换月份
                        val threshold = 150f // 触发切换的最小拖拽距离
                        if (abs(totalDragAmount) > threshold) {
                            if (totalDragAmount > 0) {
                                // 向右拖拽，显示上一月
                                onMonthNavigate(false)
                            } else {
                                // 向左拖拽，显示下一月
                                onMonthNavigate(true)
                            }
                        }
                        totalDragAmount = 0f
                    }
                ) { change, dragAmount ->
                    // 累计拖拽距离
                    totalDragAmount += dragAmount
                }
            }
    ) {
        // 星期标题行
        WeekDayHeader(weekStartDay = weekStartDay)
        
        // 日历网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
        ) {
            items(calendarDays) { date ->
                if (date != null) {
                    DayCell(
                        date = date,
                        schedule = scheduleMap[date],
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        viewMode = viewMode,
                        onClick = { onDateSelected(date) },
                        onLongClick = { onDateLongClick(date) }
                    )
                } else {
                    Box(
                        modifier = Modifier.aspectRatio(
                            when (viewMode) {
                                CalendarViewMode.COMFORTABLE -> 0.5f   // 与实际格子保持一致
                                CalendarViewMode.COMPACT -> 1f
                            }
                        )
                    )
                }
            }
        }
    }
}

/**
 * 星期标题行
 */
@Composable
private fun WeekDayHeader(weekStartDay: DayOfWeek = DayOfWeek.MONDAY) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        val weekDays = when (weekStartDay) {
            DayOfWeek.SUNDAY -> listOf(
                stringResource(R.string.schedule_weekday_short_sunday),
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday)
            )
            DayOfWeek.MONDAY -> listOf(
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday),
                stringResource(R.string.schedule_weekday_short_sunday)
            )
            else -> listOf(
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday),
                stringResource(R.string.schedule_weekday_short_sunday)
            ) // 默认周一开始
        }
        
        weekDays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = when (day) {
                    stringResource(R.string.schedule_weekday_short_sunday), stringResource(R.string.schedule_weekday_short_saturday) -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 日期单元格
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCell(
    date: LocalDate,
    schedule: Schedule?,
    isSelected: Boolean,
    isToday: Boolean,
    viewMode: CalendarViewMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    // 根据视图模式动态调整文本样式和尺寸
    val dateTextStyle = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> MaterialTheme.typography.headlineMedium // 舒适模式：更大的日期文字
        CalendarViewMode.COMPACT -> MaterialTheme.typography.titleMedium        // 紧凑模式：较小的日期文字
    }
    
    val shiftLabelSize = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> Pair(60.dp, 28.dp)  // 舒适模式：更大的班次标签
        CalendarViewMode.COMPACT -> Pair(45.dp, 18.dp)      // 紧凑模式：较小的班次标签
    }
    
    val shiftLabelFontSize = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 16.sp  // 舒适模式：更大的班次文字
        CalendarViewMode.COMPACT -> 11.sp      // 紧凑模式：较小的班次文字
    }
    
    val spacingBetween = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 8.dp  // 舒适模式：更大的内部间距利用垂直空间
        CalendarViewMode.COMPACT -> 3.dp      // 紧凑模式：较小的内部间距
    }
    
    Card(
        modifier = Modifier
            .aspectRatio(
                when (viewMode) {
                    CalendarViewMode.COMFORTABLE -> 0.5f   // 舒适模式：高度是宽度的2倍
                    CalendarViewMode.COMPACT -> 1f         // 紧凑模式：保持正方形
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isToday -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isSelected -> 4.dp
                viewMode == CalendarViewMode.COMFORTABLE -> 2.dp  // 舒适模式：略高的阴影
                else -> 1.dp  // 紧凑模式：较低的阴影
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 日期数字
                Text(
                    text = date.dayOfMonth.toString(),
                    style = dateTextStyle,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                        date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> 
                            MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // 班次信息
                schedule?.let { sch ->
                    Spacer(modifier = Modifier.height(spacingBetween))
                    Box(
                        modifier = Modifier
                            .size(width = shiftLabelSize.first, height = shiftLabelSize.second)
                            .background(
                                color = Color(sch.shift.color),
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sch.shift.name.take(2),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = shiftLabelFontSize
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}