package com.ccxiaoji.feature.schedule.presentation.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarDayCell
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarWeekHeader
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import kotlin.math.abs
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * ⚠️ DEPRECATED: 该组件已被 Demo A3 的 MonthCalendarPanel 取代
 * 该文件仅保留 CalendarWeekHeader 等公共组件
 * 请使用 com.ccxiaoji.feature.schedule.presentation.demo.parts.MonthCalendarPanel
 */
@Deprecated(
    message = "已被MonthCalendarPanel取代，请使用Demo A3组件",
    replaceWith = ReplaceWith("MonthCalendarPanel", "com.ccxiaoji.feature.schedule.presentation.demo.parts.MonthCalendarPanel")
)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    schedules: List<Schedule>,
    onDateSelected: (LocalDate) -> Unit,
    onDateLongClick: (LocalDate) -> Unit = {},
    onMonthNavigate: (Boolean) -> Unit = {},
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    viewMode: CalendarViewMode = CalendarViewMode.COMFORTABLE,
    debugParams: com.ccxiaoji.feature.schedule.presentation.debug.CalendarViewParams? = null,
    modifier: Modifier = Modifier
) {
    // ⚠️ 该组件已被Demo A3的MonthCalendarPanel取代
    error("该组件已废弃，请使用MonthCalendarPanel")
}

// ⚠️ 已删除 EnhancedMonthCalendarPanel - 使用 Demo A3 的 MonthCalendarPanel 替代

// ⚠️ 已删除 MonthGrid - 使用 Demo A3 的 DemoCalendarGrid 替代

// ⚠️ 已删除 EnhancedDayCell - 使用 Demo A3 的 DayCellLabel 替代

// ⚠️ 已删除 ExpandedScheduleDisplay - Demo A3 组件包含更优秀的实现

// ⚠️ 已删除 CompactScheduleDisplay - Demo A3 组件包含更智能的+N显示逻辑
