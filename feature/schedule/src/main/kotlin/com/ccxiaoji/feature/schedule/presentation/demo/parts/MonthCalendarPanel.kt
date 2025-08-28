package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
 
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.feature.schedule.presentation.calendar.components.CalendarWeekHeader
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * 月视图面板（支持三种指示风格）
 */
@Composable
fun MonthCalendarPanel(
    data: DemoData,
    style: IndicatorStyle,
    emphasizeNight: Boolean,
    dotConfig: DotRenderConfig? = null,
    labelConfig: LabelRenderConfig? = null,
    overviewConfig: OverviewConfig = OverviewConfig(OverviewMode.CardBorder),
    displayMode: DisplayMode = DisplayMode.Compact,
    rowHeightDp: Dp? = null,
    onRequestExpand: () -> Unit = {},
    onRequestCompact: () -> Unit = {},
    // 新增：支持外部状态管理
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit = {},
    onDateLongClick: (LocalDate) -> Unit = {},
    // 新增：星期开始日支持
    weekStartDay: java.time.DayOfWeek = java.time.DayOfWeek.MONDAY,
    // 新增：底部卡片动作回调（编辑/删除）
    onEditSelectedDate: ((LocalDate) -> Unit)? = null,
    onDeleteSelectedDate: ((LocalDate) -> Unit)? = null,
    // 新增：左右滑动切换月份
    onSwipePrevMonth: (() -> Unit)? = null,
    onSwipeNextMonth: (() -> Unit)? = null
) {
    var internalSelectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    val currentSelectedDate = selectedDate ?: internalSelectedDate

    val density = LocalDensity.current
    // 放大格子尺寸：左右更宽、上下更长一些
    val minRow = rowHeightDp ?: 60.dp
    val maxRow = 120.dp
    val dragRangePx = with(density) { (maxRow - minRow).toPx() }

    // 连续进度（0=紧凑，1=展开）
    val scope = rememberCoroutineScope()
    val expandProgressAnim = remember { Animatable(if (displayMode == DisplayMode.Expanded) 1f else 0f) }
    var progressPx by remember { mutableStateOf(expandProgressAnim.value * dragRangePx) }
    val expandProgress by remember { derivedStateOf { (progressPx / dragRangePx).coerceIn(0f, 1f) } }
    

    val debugTag = "A3GridGesture"

    Column(
        modifier = Modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    // 跟手更新进度
                    progressPx = (progressPx + delta).coerceIn(0f, dragRangePx)
                    scope.launch { expandProgressAnim.snapTo((progressPx / dragRangePx).coerceIn(0f, 1f)) }
                    // debug: progress updated
                },
                onDragStarted = {
                    // debug: drag start
                },
                onDragStopped = { velocity ->
                    // 根据进度与速度决定目标停靠
                    val v = velocity
                    val p = expandProgressAnim.value
                    val target = when {
                        v > 500f -> 1f
                        v < -500f -> 0f
                        p >= 0.5f -> 1f
                        else -> 0f
                    }
                    // debug: drag stop
                    scope.launch {
                        expandProgressAnim.animateTo(target, animationSpec = tween(180))
                        progressPx = dragRangePx * target
                        if (target == 1f) onRequestExpand() else onRequestCompact()
                    }
                }
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顶部统计：始终显示，不随进度隐藏
        OverviewStatsBar(schedules = data.schedules, config = overviewConfig)

        // 自定义网格（行高跟手插值；Bars↔Names 平滑过渡）
        val rowHeight = lerp(minRow, maxRow, expandProgressAnim.value)
        DemoCalendarGrid(
            yearMonth = data.yearMonth,
            schedules = data.schedules,
            selectedDate = currentSelectedDate,
            onSelected = { date -> 
                if (selectedDate != null) {
                    onDateSelected(date) // 使用外部回调
                } else {
                    internalSelectedDate = date // 内部状态管理
                }
            },
            onLongClick = onDateLongClick,
            indicator = style,
            weekStartDay = weekStartDay,
            emphasizeNight = emphasizeNight,
            dotConfig = dotConfig,
            labelConfig = labelConfig,
            // contentMode 不用于瞬时切换，内部按 progress 做 Crossfade
            contentMode = DayContentMode.BarsOnly,
            rowHeight = rowHeight,
            expandProgress = expandProgressAnim.value
        )

        // 横向滑动手势层（覆盖在网格之上）：近似以6行高度覆盖
        val swipeThresholdPx: Float = with(density) { 56.dp.toPx() }
        var sumDx by remember { mutableStateOf(0f) }
        var sumDy by remember { mutableStateOf(0f) }
        var decided by remember { mutableStateOf(false) }
        var horizontal by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight * 6)
                .pointerInput(swipeThresholdPx) {
                    detectDragGestures(
                        onDragStart = {
                            sumDx = 0f; sumDy = 0f; decided = false; horizontal = false
                        },
                        onDragEnd = {
                            if (horizontal && kotlin.math.abs(sumDx) > swipeThresholdPx) {
                                if (sumDx > 0) onSwipePrevMonth?.invoke() else onSwipeNextMonth?.invoke()
                            }
                            sumDx = 0f; sumDy = 0f; decided = false; horizontal = false
                        }
                    ) { change, dragAmount ->
                        val (dx, dy) = dragAmount
                        sumDx += dx; sumDy += dy
                        if (!decided) {
                            if (kotlin.math.abs(sumDx) > kotlin.math.abs(sumDy) * 1.2f) {
                                horizontal = true; decided = true
                            } else if (kotlin.math.abs(sumDy) > kotlin.math.abs(sumDx) * 1.2f) {
                                horizontal = false; decided = true
                            }
                        }
                        // 若需要阻止事件传递，可在此消费事件；为避免依赖额外API暂不消费
                    }
                }
        ) {}

        // 底部当日卡：始终展示，确保有“添加/编辑”入口
        currentSelectedDate?.let { date ->
            val schedulesForDate = data.schedules.filter { it.date == date }
            EnhancedSelectedDateCard(
                date = date,
                schedules = schedulesForDate,
                onEdit = onEditSelectedDate?.let { { it(date) } },
                onDelete = onDeleteSelectedDate?.let { { it(date) } }
            )
        }
    }
}

@Composable
private fun SimpleStatsBar(schedules: List<Schedule>) {
    val workDays = schedules.size
    val nightDays = schedules.count { it.shift.isNight() }
    val restDays = 30 - workDays
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatBlock("工作日", "$workDays 天")
        StatBlock("夜班", "$nightDays 天")
        StatBlock("休息", "$restDays 天")
    }
}

@Composable
private fun StatBlock(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, fontSize = 16.sp)
    }
}

private fun Shift.isNight(): Boolean {
    val s = startTime
    val e = endTime
    return if (s != null && e != null) {
        // 跨天或深夜/清晨
        e.isBefore(s) || s.hour >= 21 || e.hour <= 6
    } else false
}

// ============== 自定义月历网格（Dot/Bar） ==============
@Composable
private fun DemoCalendarGrid(
    yearMonth: YearMonth,
    schedules: List<Schedule>,
    selectedDate: LocalDate?,
    onSelected: (LocalDate) -> Unit,
    onLongClick: (LocalDate) -> Unit = {},
    indicator: IndicatorStyle,
    weekStartDay: java.time.DayOfWeek = java.time.DayOfWeek.MONDAY,
    emphasizeNight: Boolean,
    dotConfig: DotRenderConfig?,
    labelConfig: LabelRenderConfig?,
    contentMode: DayContentMode,
    rowHeight: Dp,
    expandProgress: Float
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val first = yearMonth.atDay(1)
    val firstOffset = (first.dayOfWeek.value - weekStartDay.value + 7) % 7
    val cells = remember(yearMonth) {
        val list = mutableListOf<LocalDate?>()
        repeat(firstOffset) { list.add(null) }
        for (d in 1..daysInMonth) list.add(yearMonth.atDay(d))
        val pad = (7 - (list.size % 7)) % 7
        repeat(pad) { list.add(null) }
        list.toList()
    }
    val map = remember(schedules) { schedules.groupBy { it.date } }

    Column {
        // 星期标题行
        CalendarWeekHeader(weekStartDay = weekStartDay)
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            // Demo 月视图通常不滚动，禁用滚动以避免与手势竞争
            userScrollEnabled = false
        ) {
            items(cells) { date ->
                if (date == null) {
                    Spacer(Modifier.height(rowHeight))
                } else {
                    val schList = map[date]
                    when (indicator) {
                        IndicatorStyle.Dot -> DayCellDot(date, schList?.firstOrNull(), selectedDate == date, emphasizeNight, dotConfig, rowHeight, onClick = { onSelected(date) }, onLongClick = { onLongClick(date) })
                        IndicatorStyle.Bar -> DayCellBar(date, schList?.firstOrNull(), selectedDate == date, emphasizeNight, rowHeight, onClick = { onSelected(date) }, onLongClick = { onLongClick(date) })
                        IndicatorStyle.Label -> DayCellLabel(
                            date = date,
                            schedules = schList,
                            selected = selectedDate == date,
                            emphasizeNight = emphasizeNight,
                            cfg = labelConfig,
                            contentMode = contentMode,
                            height = rowHeight,
                            expandProgress = expandProgress,
                            onClick = { onSelected(date) },
                            onLongClick = { onLongClick(date) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCellDot(
    date: LocalDate,
    schedule: Schedule?,
    selected: Boolean,
    emphasizeNight: Boolean,
    cfg: DotRenderConfig?,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (cfg?.selectedBgBoost == true) 0.14f else 0.1f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = 0.dp
    ) {
        val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        val borderModifier = if (cfg?.mode == DotRenderMode.LightBoundary) Modifier.border(1.dp, outline, RoundedCornerShape(10.dp)) else Modifier
        Box(Modifier.fillMaxSize().then(borderModifier)) {
            // 今日环（可选）
            if (cfg?.todayRing == true && date == LocalDate.now()) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(50))
                )
            }
            // 日期数字（交互联动：选中时与点同色）
            val baseDateColor = MaterialTheme.colorScheme.onSurface
            val shiftColor = schedule?.let { ShiftColorMapper.getColorForShift(it.shift.color) } ?: baseDateColor
            val nightColor = Color(0xFF7C4DFF)
            val linkageColor = when {
                cfg?.mode == DotRenderMode.InteractiveLinkage && selected && schedule != null && emphasizeNight && schedule.shift.isNight() -> nightColor
                cfg?.mode == DotRenderMode.InteractiveLinkage && selected && schedule != null -> shiftColor
                else -> baseDateColor
            }
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = linkageColor,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
            )
            if (schedule != null) {
                val rawColor = if (emphasizeNight && schedule.shift.isNight()) nightColor else shiftColor
                val weekendAdjust = cfg?.weekendAlphaAdjust == true && (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY)
                val alphaAdjusted = if (weekendAdjust) 0.7f else 1f
                val dotBase = rawColor.copy(alpha = alphaAdjusted)
                val baseSize: Dp = if (cfg?.biggerDot == true) 8.dp else 6.dp
                val targetSize: Dp = if (cfg?.mode == DotRenderMode.InteractiveLinkage && selected) baseSize * 1.15f else baseSize
                val animatedSize by androidx.compose.animation.core.animateDpAsState(targetValue = targetSize, label = "dotSize")
                if (cfg?.mode == DotRenderMode.LightBoundary && (cfg?.spacingTuning == true)) {
                    // 对齐辅助线
                    Box(
                        modifier = Modifier
                            .size(width = 12.dp, height = 1.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-12).dp)
                            .background(outline, RoundedCornerShape(50))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(animatedSize)
                        .align(Alignment.BottomCenter)
                        .let { if (cfg?.spacingTuning == true) it.offset(y = (-2).dp) else it }
                        .background(dotBase, shape = RoundedCornerShape(50))
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCellBar(
    date: LocalDate,
    schedule: Schedule?,
    selected: Boolean,
    emphasizeNight: Boolean,
    height: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
            )
            if (schedule != null) {
                val shiftColor = ShiftColorMapper.getColorForShift(schedule.shift.color)
                val barColor = if (emphasizeNight && schedule.shift.isNight()) Color(0xFF7C4DFF) else shiftColor.copy(alpha = 0.6f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .align(Alignment.BottomCenter)
                        .background(barColor, shape = RoundedCornerShape(0.dp))
                )
            }
        }
    }
}

// A1 渲染配置（仅 Dot 使用）
data class DotRenderConfig(
    val mode: DotRenderMode,
    val biggerDot: Boolean,
    val weekendAlphaAdjust: Boolean,
    val todayRing: Boolean,
    val selectedBgBoost: Boolean,
    val spacingTuning: Boolean
)

enum class DotRenderMode { LightBoundary, InteractiveLinkage }

// A3 渲染配置（仅 Label 使用）
data class LabelRenderConfig(
    val mode: LabelRenderMode,
    val biggerLabel: Boolean,
    val weekendAlphaAdjust: Boolean,
    val todayRing: Boolean,
    val selectedBgBoost: Boolean,
    val spacingTuning: Boolean,
    val visual: LabelVisual = LabelVisual.AbbrevSmall,
    val multiMode: MultiShiftMode = MultiShiftMode.TwoChipsPlusMore,
    val labelFontSp: Int = 11,
    val labelHPaddingDp: Int = 4,
    val forcePlusNPreview: Boolean = false
)

enum class LabelRenderMode { LightBoundary, InteractiveLinkage }

enum class LabelVisual { AbbrevSmall, IconLetter, MinimalWeak }

enum class MultiShiftMode { TwoChipsPlusMore, FlowTwoLines, Summary }

// 信息总览样式
data class OverviewConfig(val mode: OverviewMode)
enum class OverviewMode { CardBorder, TopBottomDividers, InnerBorder }

@Composable
private fun OverviewStatsBar(schedules: List<Schedule>, config: OverviewConfig) {
    when (config.mode) {
        OverviewMode.CardBorder -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) { Box(Modifier.padding(12.dp)) { SimpleStatsBar(schedules) } }
        }
        OverviewMode.TopBottomDividers -> {
            Column(Modifier.fillMaxWidth()) {
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                Box(Modifier.padding(vertical = 8.dp)) { SimpleStatsBar(schedules) }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            }
        }
        OverviewMode.InnerBorder -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) { SimpleStatsBar(schedules) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayCellLabel(
    date: LocalDate,
    schedules: List<Schedule>?,
    selected: Boolean,
    emphasizeNight: Boolean,
    cfg: LabelRenderConfig?,
    contentMode: DayContentMode,
    height: Dp,
    expandProgress: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (cfg?.selectedBgBoost == true) 0.14f else 0.1f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = 0.dp
    ) {
        val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        val borderModifier = if (cfg?.mode == LabelRenderMode.LightBoundary) Modifier.border(1.dp, outline, RoundedCornerShape(10.dp)) else Modifier
        Box(Modifier.fillMaxSize().then(borderModifier)) {
            // 今日环
            if (cfg?.todayRing == true && date == LocalDate.now()) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(50))
                )
            }
            val baseDateColor = MaterialTheme.colorScheme.onSurface
            val firstSchedule = schedules?.firstOrNull()
            val shiftColor = firstSchedule?.let { ShiftColorMapper.getColorForShift(it.shift.color) } ?: baseDateColor
            val nightColor = Color(0xFF7C4DFF)
            val hasSchedule = firstSchedule != null
            val isNight = firstSchedule?.shift?.isNight() == true
            val linkageColor = when {
                cfg?.mode == LabelRenderMode.InteractiveLinkage && selected && hasSchedule && emphasizeNight && isNight -> nightColor
                cfg?.mode == LabelRenderMode.InteractiveLinkage && selected && hasSchedule -> shiftColor
                else -> baseDateColor
            }
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = linkageColor,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
            )
            schedules?.let { list ->
                // 平滑过渡：Bars → Names
                val p = smoothstep(expandProgress, 0.35f, 0.65f)
                Box(Modifier.fillMaxSize().graphicsLayer { alpha = 1f - p }) {
                    RenderBarsForDay(list)
                }
                Box(Modifier.fillMaxSize().graphicsLayer { alpha = p }) {
                    RenderNamesForDay(list)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun BoxScope.RenderLabelsForDay(items: List<Schedule>, emphasizeNight: Boolean, cfg: LabelRenderConfig?) {
    val mode = cfg?.multiMode ?: MultiShiftMode.TwoChipsPlusMore
    when (mode) {
        MultiShiftMode.TwoChipsPlusMore -> {
            val maxShow = if (cfg?.forcePlusNPreview == true) 1 else 2
            val show = items.take(maxShow)
            val more = (items.size - show.size).coerceAtLeast(0)
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (cfg?.spacingTuning == true) 8.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                show.forEach { LabelChip(it, emphasizeNight, cfg) }
                if (more > 0 && cfg?.forcePlusNPreview == true) MoreBadge(more)
            }
        }
        MultiShiftMode.FlowTwoLines -> {
            FlowRow(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = if (cfg?.spacingTuning == true) 8.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items.take(4).forEach { LabelChip(it, emphasizeNight, cfg) }
                val more = (items.size - 4).coerceAtLeast(0)
                if (more > 0 && cfg?.forcePlusNPreview == true) MoreBadge(more)
            }
        }
        MultiShiftMode.Summary -> {
            val head = items.map { abbrev(it.shift.name) }.take(3).joinToString("+")
            val more = (items.size - 3).coerceAtLeast(0)
            val text = if (more > 0) "$head+${more}" else head
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (cfg?.spacingTuning == true) 8.dp else 6.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) { Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun MoreBadge(more: Int) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = "+$more", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BoxScope.RenderBarsForDay(items: List<Schedule>) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            items.isEmpty() -> { /* 无内容 */ }
            items.size == 1 -> {
                // 单班次：显示完整长条
                val sch = items.first()
                val color = ShiftColorMapper.getColorForShift(sch.shift.color)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(color, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sch.shift.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.White
                    )
                }
            }
            else -> {
                // 多班次：一个加高长条，显示"班次名称+N"格式
                val firstSch = items.first()
                val firstColor = ShiftColorMapper.getColorForShift(firstSch.shift.color)
                val additionalCount = items.size - 1
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(firstColor, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${firstSch.shift.name}+$additionalCount",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.RenderNamesForDay(items: List<Schedule>) {
    val maxRows = 6
    val rows = items.sortedBy { it.shift.startTime ?: java.time.LocalTime.MIN }.take(maxRows)
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 6.dp, end = 6.dp, bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        rows.forEach { sch ->
            val color = ShiftColorMapper.getColorForShift(sch.shift.color)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(color, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sch.shift.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White
                )
            }
        }
    }
}

enum class DayContentMode { BarsOnly, NamesList }
enum class DisplayMode { Compact, Expanded }

@Composable
private fun LabelChip(sch: Schedule, emphasizeNight: Boolean, cfg: LabelRenderConfig?) {
    val fgBase = if (emphasizeNight && sch.shift.isNight()) Color(0xFF7C4DFF) else ShiftColorMapper.getColorForShift(sch.shift.color)
    val weekendAdjust = cfg?.weekendAlphaAdjust == true
    val fg = if (weekendAdjust) fgBase.copy(alpha = 0.95f) else fgBase
    val bg = ShiftColorMapper.getBackgroundColorForShift(sch.shift.color, 0.1f)
    val scaleTarget = if (cfg?.mode == LabelRenderMode.InteractiveLinkage) 1.0f else 1.0f
    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = scaleTarget, label = "labelChipScale")
    when (cfg?.visual ?: LabelVisual.AbbrevSmall) {
        LabelVisual.AbbrevSmall -> {
            Box(
                modifier = Modifier
                    .scale(scale)
                    .background(bg, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = (cfg?.labelHPaddingDp ?: 4).dp, vertical = 2.dp)
            ) { Text(abbrev(sch.shift.name), color = fg, fontSize = (cfg?.labelFontSp ?: 11).sp) }
        }
        LabelVisual.IconLetter -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(fg, shape = RoundedCornerShape(50)))
                Spacer(Modifier.width(2.dp))
                Text(abbrev(sch.shift.name, 1), color = fg, fontSize = (cfg?.labelFontSp ?: 11).sp)
            }
        }
        LabelVisual.MinimalWeak -> {
            Box(modifier = Modifier.size(6.dp).background(fg.copy(alpha = 0.8f), shape = RoundedCornerShape(50)))
        }
    }
}

private fun abbrev(name: String, count: Int = 2): String = when {
    name.contains("夜") -> "夜"
    name.contains("晚") -> "晚"
    name.contains("早") -> "早"
    name.contains("日") -> "日"
    else -> name.take(count)
}

private fun lerp(start: Dp, end: Dp, t: Float): Dp {
    val clamped = t.coerceIn(0f, 1f)
    return start + (end - start) * clamped
}

private fun smoothstep(x: Float, edge0: Float, edge1: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3 - 2 * t)
}
