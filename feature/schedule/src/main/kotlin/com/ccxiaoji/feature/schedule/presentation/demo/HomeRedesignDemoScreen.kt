package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import java.time.*

/**
 * 排班主页 UI 设计 Demo 容器
 * - 下拉菜单切换 12 个方案
 * - “夜班突出”开关全局生效
 * - 统一示例数据（含跨天夜班）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRedesignDemoScreen(
    onNavigateBack: () -> Unit = {}
) {
    val demoData by remember {
        mutableStateOf(buildDemoData())
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("排班主页 · A3（小标签）演示", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        // 支持下拉/上推切换显示模式（紧凑/展开）
        var displayMode by remember { mutableStateOf(com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode.Compact) }
        var expandedTipShown by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 固定渲染 A3：轻量边界 + 选中底色增强；其它细节默认关闭
            com.ccxiaoji.feature.schedule.presentation.demo.parts.MonthCalendarPanel(
                data = demoData,
                style = IndicatorStyle.Label,
                emphasizeNight = false,
                dotConfig = null,
                labelConfig = com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelRenderConfig(
                    mode = com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelRenderMode.LightBoundary,
                    biggerLabel = false,
                    weekendAlphaAdjust = false,
                    todayRing = false,
                    selectedBgBoost = true,
                    spacingTuning = false,
                    visual = com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelVisual.AbbrevSmall,
                    multiMode = com.ccxiaoji.feature.schedule.presentation.demo.parts.MultiShiftMode.TwoChipsPlusMore,
                    labelFontSp = 11,
                    labelHPaddingDp = 4,
                    forcePlusNPreview = false
                ),
                overviewConfig = com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewConfig(
                    com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewMode.CardBorder
                ),
                displayMode = displayMode,
                rowHeightDp = null,
                onRequestExpand = {
                    if (displayMode != com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode.Expanded) {
                        displayMode = com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode.Expanded
                        if (!expandedTipShown) {
                            expandedTipShown = true
                            // 提示：上滑收起
                            scope.launch { snackbarHostState.showSnackbar("上滑收起") }
                        }
                    }
                },
                onRequestCompact = {
                    if (displayMode != com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode.Compact) {
                        displayMode = com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode.Compact
                    }
                }
            )
        }
    }
}

// 选择器已移除（固定展示 A3 基线配置）

// 指示风格：点/横条/标签
enum class IndicatorStyle { Dot, Bar, Label }

// 简化的演示数据容器
data class DemoData(
    val yearMonth: YearMonth,
    val schedules: List<Schedule>,
    val shifts: List<Shift>
)

/**
 * 构造演示数据：包含日班、晚班、跨天夜班、休息等
 */
private fun buildDemoData(): DemoData {
    val yearMonth = YearMonth.now()
    val today = LocalDate.now()

    // 班次：日班、晚班、夜班（跨天）
    val dayShift = Shift(1, "日班", LocalTime.of(9, 0), LocalTime.of(18, 0), 0xFF2196F3.toInt(), "日间")
    val eveShift = Shift(2, "晚班", LocalTime.of(14, 0), LocalTime.of(22, 0), 0xFFFF9800.toInt(), "傍晚")
    val nightShift = Shift(3, "夜班", LocalTime.of(22, 0), LocalTime.of(6, 0), 0xFF7C4DFF.toInt(), "跨天")
    val otShift = Shift(4, "加班", LocalTime.of(17, 30), LocalTime.of(20, 30), 0xFF4CAF50.toInt(), "临时")

    // 构造本月若干示例：工作日轮换、周末部分休息、插入夜班序列
    val days = yearMonth.lengthOfMonth()
    val schedules = buildList {
        for (d in 1..days) {
            val date = yearMonth.atDay(d)
            val dow = date.dayOfWeek
            when {
                // 周末多数休息，偶尔日班
                dow == DayOfWeek.SATURDAY && d % 2 == 0 -> add(Schedule(d.toLong(), date, dayShift))
                dow == DayOfWeek.SUNDAY -> {
                    if (d % 3 == 0) add(Schedule(d.toLong(), date, eveShift))
                }
                // 工作日：日/晚 交替
                d % 2 == 0 -> add(Schedule(d.toLong(), date, dayShift))
                else -> add(Schedule(d.toLong(), date, eveShift))
            }
        }
        // 插入 4 组夜班（跨天）
        val start = maxOf(1, today.dayOfMonth - 6)
        for (base in listOf(start, start + 3, start + 7, start + 14)) {
            val safe = base.coerceIn(1, days)
            add(Schedule((100 + safe).toLong(), yearMonth.atDay(safe), nightShift))
        }

        // 为演示 +N：在 5、12、19 号追加多班次
        val extraDays = listOf(5, 12, 19).filter { it in 1..days }
        extraDays.forEachIndexed { idx, d ->
            val date = yearMonth.atDay(d)
            // 确保基础有一个班次，再追加
            add(Schedule((200 + d).toLong(), date, nightShift))
            if (idx >= 0) add(Schedule((300 + d).toLong(), date, otShift))
            if (idx >= 1) add(Schedule((400 + d).toLong(), date, dayShift))
        }
    }
    return DemoData(yearMonth, schedules, listOf(dayShift, eveShift, nightShift, otShift))
}

// =============== 月视图（A 组） ===============
// 保留历史辅助函数（不再使用）已移除
// =============== Agenda（B 组） ===============
@Composable
private fun AgendaDemo(data: DemoData, style: IndicatorStyle, emphasizeNight: Boolean) {
    com.ccxiaoji.feature.schedule.presentation.demo.parts.AgendaPanel(
        data = data,
        style = style,
        emphasizeNight = emphasizeNight
    )
}

// =============== 双栏（C 组） ===============
@Composable
private fun SplitDemo(data: DemoData, style: IndicatorStyle, emphasizeNight: Boolean) {
    com.ccxiaoji.feature.schedule.presentation.demo.parts.SplitPanel(
        data = data,
        style = style,
        emphasizeNight = emphasizeNight
    )
}

// =============== 快捷录入（S1） ===============
@Composable
private fun QuickEntryDemo(data: DemoData, emphasizeNight: Boolean) {
    com.ccxiaoji.feature.schedule.presentation.demo.parts.QuickEntryPanel(
        data = data,
        emphasizeNight = emphasizeNight
    )
}

// =============== 批量套用（S2） ===============
@Composable
private fun BulkApplyDemo(data: DemoData, emphasizeNight: Boolean) {
    com.ccxiaoji.feature.schedule.presentation.demo.parts.BulkApplyPanel(
        data = data,
        emphasizeNight = emphasizeNight
    )
}

// =============== 统计样式对比（S3） ===============
@Composable
private fun StatsStylesDemo(data: DemoData, emphasizeNight: Boolean) {
    com.ccxiaoji.feature.schedule.presentation.demo.parts.StatsStylesPanel(
        data = data,
        emphasizeNight = emphasizeNight
    )
}
