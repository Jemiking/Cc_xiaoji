package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.runtime.Composable
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode
import com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelConfig
import com.ccxiaoji.feature.schedule.presentation.demo.parts.MonthCalendarPanel
import com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewConfig
import com.ccxiaoji.feature.schedule.presentation.adapter.CalendarDemoAdapter
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.LocalDate
import java.time.YearMonth

/**
 * Demo 依赖的统一接口（门面）。
 * 正式代码仅依赖此 API，具体实现可在 Demo 模块中替换或在这里提供占位版本。
 */
object CalendarDemoApi {
    fun viewModeToDisplayMode(viewMode: CalendarViewMode): DisplayMode =
        CalendarDemoAdapter.viewModeToDisplayMode(viewMode)

    fun convertToDemoData(yearMonth: YearMonth, schedules: List<Schedule>): Any =
        CalendarDemoAdapter.convertToDemoData(yearMonth, schedules)

    fun getBaselineConfig(): Triple<LabelConfig, OverviewConfig, IndicatorStyle> =
        CalendarDemoAdapter.getA3BaselineConfig()

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
        onSwipeNextMonth: () -> Unit
    ) {
        MonthCalendarPanel(
            data = data,
            style = style,
            emphasizeNight = emphasizeNight,
            dotConfig = dotConfig,
            labelConfig = labelConfig,
            overviewConfig = overviewConfig,
            displayMode = displayMode,
            rowHeightDp = rowHeightDp,
            onRequestExpand = onRequestExpand,
            onRequestCompact = onRequestCompact,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onDateLongClick = onDateLongClick,
            weekStartDay = weekStartDay,
            onEditSelectedDate = onEditSelectedDate,
            onDeleteSelectedDate = onDeleteSelectedDate,
            onSwipePrevMonth = onSwipePrevMonth,
            onSwipeNextMonth = onSwipeNextMonth
        )
    }

    // Demo 入口页面（可被真实 Demo 替换）
    @Composable fun CalendarUiDemo(onNavigateBack: () -> Unit) =
        com.ccxiaoji.feature.schedule.presentation.demo.CalendarUiDemoScreen(onNavigateBack)
    @Composable fun CalendarFlatDemo(onNavigateBack: () -> Unit) =
        com.ccxiaoji.feature.schedule.presentation.demo.FlatScheduleDemoScreen(onNavigateBack)
    @Composable fun StyleDemo(onNavigateBack: () -> Unit) =
        com.ccxiaoji.feature.schedule.presentation.demo.StyleDemoScreen(onNavigateBack)
    @Composable fun HomeRedesignDemo(onNavigateBack: () -> Unit) =
        com.ccxiaoji.feature.schedule.presentation.demo.HomeRedesignDemoScreen(onNavigateBack)
}

