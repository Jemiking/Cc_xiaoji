package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarDayCell
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarWeekHeader
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
        CalendarWeekHeader(weekStartDay = weekStartDay)
        
        // 日历网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(gridSpacing),
            verticalArrangement = Arrangement.spacedBy(gridSpacing)
        ) {
            items(calendarDays) { date ->
                if (date != null) {
                    CalendarDayCell(
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