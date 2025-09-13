package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import java.time.LocalDate
import java.time.YearMonth

/**
 * 展示模式（紧凑/展开）占位枚举
 */
enum class DisplayMode { Compact, Expanded }

/** 占位的标签配置 */
data class LabelConfig(val dummy: Boolean = true)

/** 占位的总览配置 */
data class OverviewConfig(val dummy: Boolean = true)

/**
 * 月视图日历占位组件（A3 Demo）
 * 仅保证参数与调用方匹配，内部简单渲染占位文案。
 */
@Composable
fun MonthCalendarPanel(
    data: Any?,
    style: IndicatorStyle,
    emphasizeNight: Boolean,
    dotConfig: Any?,
    labelConfig: LabelConfig,
    overviewConfig: OverviewConfig,
    displayMode: DisplayMode,
    rowHeightDp: Int?,
    onRequestExpand: () -> Unit,
    onRequestCompact: () -> Unit,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit,
    weekStartDay: java.time.DayOfWeek,
    onEditSelectedDate: (LocalDate) -> Unit,
    onDeleteSelectedDate: (LocalDate) -> Unit,
    onSwipePrevMonth: () -> Unit,
    onSwipeNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(8.dp)) {
        Text(
            text = "MonthCalendarPanel (stub) — Mode=$displayMode, WeekStart=$weekStartDay"
        )
    }
}

