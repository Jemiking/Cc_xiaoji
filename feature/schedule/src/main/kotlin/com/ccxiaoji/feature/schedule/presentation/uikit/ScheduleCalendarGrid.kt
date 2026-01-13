package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

/**
 * Schedule 模块月视图日历网格
 *
 * 用于显示整月的日历视图，包含：
 * - 星期标题行
 * - 日期网格（使用 ScheduleDayCell）
 * - 左右滑动手势切换月份
 *
 * @param yearMonth 当前显示的年月
 * @param selectedDate 选中的日期
 * @param schedules 当月的排班列表
 * @param onDateClick 日期点击回调
 * @param modifier Modifier
 * @param onDateLongClick 日期长按回调
 * @param onMonthSwipe 左右滑动切换月份回调（true=下个月, false=上个月）
 * @param weekStartDay 一周起始日
 * @param cellSize 日格子尺寸模式
 * @param showWeekHeader 是否显示星期标题
 *
 * @see ScheduleDayCell 日期格子组件
 * @see ScheduleWeekHeader 星期标题组件
 */
@Composable
fun ScheduleCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    schedules: List<Schedule>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    onDateLongClick: (LocalDate) -> Unit = {},
    onMonthSwipe: ((Boolean) -> Unit)? = null,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    cellSize: DayCellSize = DayCellSize.Medium,
    showWeekHeader: Boolean = true
) {
    // 创建日期到排班的映射
    val scheduleMap = remember(schedules) {
        schedules.associateBy { it.date }
    }

    // 获取网格布局参数
    val gridParams = cellSize.toGridParams()

    // 滑动手势状态
    var totalDragAmount by remember { mutableFloatStateOf(0f) }

    // 记录滑动方向用于动画
    var swipeDirection by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .then(
                if (onMonthSwipe != null) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { totalDragAmount = 0f },
                            onDragEnd = {
                                val threshold = 150f
                                if (abs(totalDragAmount) > threshold) {
                                    // 记录滑动方向：左滑(负值)=下月(1)，右滑(正值)=上月(-1)
                                    swipeDirection = if (totalDragAmount < 0) 1 else -1
                                    onMonthSwipe(totalDragAmount < 0)
                                }
                                totalDragAmount = 0f
                            }
                        ) { _, dragAmount ->
                            totalDragAmount += dragAmount
                        }
                    }
                } else Modifier
            )
    ) {
        // 星期标题行
        if (showWeekHeader) {
            ScheduleWeekHeader(
                weekStartDay = weekStartDay,
                modifier = Modifier.padding(horizontal = gridParams.horizontalPadding)
            )
            Spacer(modifier = Modifier.height(gridParams.cellSpacing))
        }

        // 使用 AnimatedContent 实现翻月动画
        AnimatedContent(
            targetState = yearMonth,
            transitionSpec = {
                // 根据滑动方向决定动画方向
                val direction = swipeDirection.takeIf { it != 0 }
                    ?: if (targetState > initialState) 1 else -1

                val enterTransition = slideInHorizontally(
                    animationSpec = ScheduleDesignSpecs.Motion.monthSwipeSpec(),
                    initialOffsetX = { fullWidth -> direction * fullWidth }
                ) + fadeIn(animationSpec = ScheduleDesignSpecs.Motion.monthSwipeSpec())

                val exitTransition = slideOutHorizontally(
                    animationSpec = ScheduleDesignSpecs.Motion.monthSwipeSpec(),
                    targetOffsetX = { fullWidth -> -direction * fullWidth }
                ) + fadeOut(animationSpec = ScheduleDesignSpecs.Motion.monthSwipeSpec())

                enterTransition togetherWith exitTransition
            },
            label = "monthTransition"
        ) { targetYearMonth ->
            // 为每个月份状态重新计算日历数据
            val calendarDays = remember(targetYearMonth, weekStartDay) {
                buildCalendarDays(targetYearMonth, weekStartDay)
            }

            // 日历网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                contentPadding = PaddingValues(horizontal = gridParams.horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(gridParams.cellSpacing),
                verticalArrangement = Arrangement.spacedBy(gridParams.cellSpacing)
            ) {
                itemsIndexed(
                    items = calendarDays,
                    key = { index, date -> date?.toEpochDay() ?: -index.toLong() }
                ) { _, date ->
                    if (date != null) {
                        ScheduleDayCell(
                            date = date,
                            schedule = scheduleMap[date],
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            size = cellSize,
                            onClick = { onDateClick(date) },
                            onLongClick = { onDateLongClick(date) }
                        )
                    } else {
                        EmptyDayCell(size = cellSize)
                    }
                }
            }
        }
    }
}

/**
 * 星期标题行
 *
 * 显示一周的星期名称，支持自定义起始日。
 *
 * @param weekStartDay 一周起始日
 * @param modifier Modifier
 */
@Composable
fun ScheduleWeekHeader(
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    modifier: Modifier = Modifier
) {
    val weekDays = remember(weekStartDay) {
        buildWeekDays(weekStartDay)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { dayOfWeek ->
            WeekDayLabel(
                dayOfWeek = dayOfWeek,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 星期标签
 */
@Composable
private fun WeekDayLabel(
    dayOfWeek: DayOfWeek,
    modifier: Modifier = Modifier
) {
    val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    val textColor = if (isWeekend) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier.padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 构建日历网格数据
 *
 * @param yearMonth 年月
 * @param weekStartDay 一周起始日
 * @return 日期列表（null 表示空白占位）
 */
private fun buildCalendarDays(
    yearMonth: YearMonth,
    weekStartDay: DayOfWeek
): List<LocalDate?> {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)

    // 计算第一天相对于一周起始日的偏移量
    val firstDayOffset = when (weekStartDay) {
        DayOfWeek.SUNDAY -> firstDayOfMonth.dayOfWeek.value % 7
        else -> (firstDayOfMonth.dayOfWeek.value - weekStartDay.value + 7) % 7
    }

    val days = mutableListOf<LocalDate?>()

    // 添加月初的空白天数
    repeat(firstDayOffset) {
        days.add(null)
    }

    // 添加当月所有天数
    for (day in 1..daysInMonth) {
        days.add(yearMonth.atDay(day))
    }

    return days
}

/**
 * 构建星期列表
 *
 * @param weekStartDay 一周起始日
 * @return 按顺序排列的星期列表
 */
private fun buildWeekDays(weekStartDay: DayOfWeek): List<DayOfWeek> {
    val days = DayOfWeek.entries
    val startIndex = days.indexOf(weekStartDay)
    return days.drop(startIndex) + days.take(startIndex)
}

/**
 * 网格布局参数
 */
@Stable
private data class CalendarGridParams(
    val horizontalPadding: Dp,
    val cellSpacing: Dp
)

/**
 * 根据日格子尺寸获取网格布局参数
 */
private fun DayCellSize.toGridParams(): CalendarGridParams = when (this) {
    DayCellSize.Compact -> CalendarGridParams(
        horizontalPadding = 4.dp,
        cellSpacing = 2.dp
    )
    DayCellSize.Medium -> CalendarGridParams(
        horizontalPadding = 6.dp,
        cellSpacing = 4.dp
    )
    DayCellSize.Comfortable -> CalendarGridParams(
        horizontalPadding = 8.dp,
        cellSpacing = 6.dp
    )
}
