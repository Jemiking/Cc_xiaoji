package com.ccxiaoji.feature.schedule.presentation.debug

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight


/**
 * 排班主页调试参数配置
 */
data class DebugCalendarParams(
    // 1. TopAppBar 参数
    val topAppBar: TopAppBarParams = TopAppBarParams(),
    
    // 2. MonthlyStatisticsCard 参数
    val statisticsCard: StatisticsCardParams = StatisticsCardParams(),
    
    // 3. CalendarView 参数
    val calendarView: CalendarViewParams = CalendarViewParams(),
    
    // 4. SelectedDateDetailCard 参数
    val detailCard: DetailCardParams = DetailCardParams(),
    
    // 5. 整体布局参数
    val layout: LayoutParams = LayoutParams(),
    
    // 6. FloatingActionButton 参数
    val fab: FabParams = FabParams(),
    
    // 7. 主题色彩参数
    val theme: ThemeParams = ThemeParams()
)

/**
 * TopAppBar 相关参数
 */
data class TopAppBarParams(
    val height: Dp = 64.dp,
    val backgroundColor: Color = Color.Transparent,
    val elevation: Dp = 0.dp,
    val contentPadding: Dp = 16.dp,
    
    // 标题参数
    val titleFontSize: TextUnit = 20.sp,
    val titleFontWeight: FontWeight = FontWeight.Normal,
    val titleTextColor: Color = Color.Unspecified,
    
    // 操作按钮参数
    val actionButtonSize: Dp = 48.dp,
    val actionButtonPadding: Dp = 12.dp,
    val todayButtonTextSize: TextUnit = 16.sp,
    val todayButtonFontWeight: FontWeight = FontWeight.Bold,
    val moreIconSize: Dp = 24.dp
)

/**
 * 月度统计卡片参数
 */
data class StatisticsCardParams(
    // 卡片外观
    val cornerRadius: Dp = 12.dp,
    val backgroundColor: Color = Color.Unspecified,
    val elevation: Dp = 2.dp,
    val borderWidth: Dp = 0.dp,
    val borderColor: Color = Color.Transparent,
    
    // 内容布局
    val padding: Dp = 16.dp,
    val itemSpacing: Dp = 12.dp,
    val iconSize: Dp = 20.dp,
    val textSize: TextUnit = 14.sp,
    val numberSize: TextUnit = 18.sp,
    
    // 数据展示
    val primaryDataColor: Color = Color.Unspecified,
    val secondaryDataColor: Color = Color.Unspecified,
    val progressBarHeight: Dp = 6.dp,
    val progressBarColor: Color = Color.Unspecified
)

/**
 * 日历视图参数
 */
data class CalendarViewParams(
    // 网格布局
    val cellSize: Dp = 48.dp,
    val cellSpacing: Dp = 4.dp,
    val rowHeight: Dp = 56.dp,
    val weekHeaderHeight: Dp = 40.dp,
    val cornerRadius: Dp = 8.dp,

    // 日期格子样式
    val normalDateTextColor: Color = Color.Unspecified,
    val selectedDateBackgroundColor: Color = Color.Unspecified,
    val selectedDateTextColor: Color = Color.Unspecified,
    val todayDateBackgroundColor: Color = Color.Unspecified,
    // 新增：日期数字字号
    val dateNumberTextSize: TextUnit = 16.sp,
    val otherMonthDateOpacity: Float = 0.5f,
    
    // 新增：lineHeight调试参数
    val dateNumberLineHeightMultiplier: Float = 1.25f,  // lineHeight = fontSize * 此倍数
    val minContainerHeightMultiplier: Float = 2.5f,     // 最小容器高度 = fontSize * 此倍数  
    val fontWeightBoldExtraSpace: Float = 0.1f,         // Bold字体额外空间倍数
    val characterSpaceAdjustment: Float = 0.0f,         // 字符空间微调，正值增加负值减少
    
    // 日程标记
    val scheduleIndicatorSize: Dp = 6.dp,
    val scheduleIndicatorColor: Color = Color.Unspecified,
    val maxScheduleIndicators: Int = 3,
    
    // 交互效果
    val dateClickRippleColor: Color = Color.Unspecified,
    val dateHoverBackgroundColor: Color = Color.Unspecified,
    val dateLongPressScale: Float = 0.95f
)

/**
 * 选中日期详情卡片参数
 */
data class DetailCardParams(
    // 卡片样式
    val cornerRadius: Dp = 12.dp,
    val backgroundColor: Color = Color.Unspecified,
    val elevation: Dp = 2.dp,
    val padding: Dp = 16.dp,
    
    // 内容布局
    val dateHeaderTextSize: TextUnit = 16.sp,
    val scheduleInfoTextSize: TextUnit = 14.sp,
    val actionButtonSpacing: Dp = 8.dp,
    val scheduleTimeTextSize: TextUnit = 12.sp,
    
    // 操作按钮
    val editButtonColor: Color = Color.Unspecified,
    val deleteButtonColor: Color = Color.Unspecified,
    val buttonCornerRadius: Dp = 8.dp,
    val buttonPadding: Dp = 8.dp
)

/**
 * 整体布局参数
 */
data class LayoutParams(
    // 页面边距
    val screenHorizontalPadding: Dp = 16.dp,
    val screenVerticalPadding: Dp = 8.dp,
    val componentSpacing: Dp = 16.dp,
    
    // 响应式参数
    val compactModeBreakpoint: Dp = 600.dp,
    val landscapeLayoutPadding: Dp = 32.dp,
    
    // 动画参数
    val monthTransitionDuration: Int = 300,
    val dateSelectionAnimationDuration: Int = 200,
    val cardAppearAnimationDuration: Int = 250,
    val fabAnimationDuration: Int = 150
)

/**
 * FloatingActionButton 参数
 */
data class FabParams(
    // 按钮样式
    val size: Dp = 56.dp,
    val backgroundColor: Color = Color.Unspecified,
    val iconColor: Color = Color.White,
    val elevation: Dp = 6.dp,
    
    // 位置参数
    val bottomMargin: Dp = 16.dp,
    val endMargin: Dp = 16.dp,
    val offsetOnScroll: Dp = 0.dp
)

/**
 * 主题色彩参数
 */
data class ThemeParams(
    // 品牌色彩
    val primaryBrandColor: Color = Color(0xFF66BB6A),
    val secondaryBrandColor: Color = Color(0xFF81C784),
    val scheduleModuleAccentColor: Color = Color(0xFFFFB74D),
    
    // 语义色彩
    val workDayColor: Color = Color(0xFF2196F3),
    val restDayColor: Color = Color(0xFF4CAF50),
    val overtimeColor: Color = Color(0xFFFF9800),
    val vacationColor: Color = Color(0xFF9C27B0)
)

/**
 * 默认调试参数实例
 */
object DefaultDebugParams {
    val default = DebugCalendarParams(
        topAppBar = TopAppBarParams(
            height = 48.dp,
            titleFontSize = 20.sp,
            actionButtonSize = 48.dp,
            todayButtonTextSize = 16.sp
        ),
        statisticsCard = StatisticsCardParams(
            cornerRadius = 12.dp,
            elevation = 0.dp,
            padding = 8.dp,
            itemSpacing = 11.808083.dp,
            textSize = 13.804218.sp
        ),
        calendarView = CalendarViewParams(
            cellSize = 50.266678.dp,
            cellSpacing = 0.dp,
            rowHeight = 50.266678.dp,
            cornerRadius = 9.880794.dp,
            scheduleIndicatorSize = 9.271944.dp,
            dateNumberTextSize = 11.784809.sp,
            otherMonthDateOpacity = 0.49955687f,
            // 新增的lineHeight调试参数
            dateNumberLineHeightMultiplier = 1.3f,
            minContainerHeightMultiplier = 2.8f,
            fontWeightBoldExtraSpace = 0.15f,
            characterSpaceAdjustment = 0.05f
        ),
        detailCard = DetailCardParams(
            cornerRadius = 12.dp,
            padding = 16.dp,
            dateHeaderTextSize = 16.sp,
            scheduleInfoTextSize = 14.sp
        ),
        layout = LayoutParams(
            screenHorizontalPadding = 16.dp,
            componentSpacing = 16.dp,
            monthTransitionDuration = 300
        ),
        fab = FabParams(
            size = 56.dp,
            elevation = 6.dp,
            bottomMargin = 16.dp
        )
    )
    
    val modern = DebugCalendarParams(
        statisticsCard = StatisticsCardParams(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            padding = 20.dp
        ),
        calendarView = CalendarViewParams(
            cellSize = 52.dp,
            cornerRadius = 8.dp
        ),
        detailCard = DetailCardParams(
            cornerRadius = 16.dp,
            elevation = 4.dp
        )
    )
    
    val compact = DebugCalendarParams(
        layout = LayoutParams(
            screenHorizontalPadding = 12.dp,
            componentSpacing = 12.dp
        ),
        calendarView = CalendarViewParams(
            cellSize = 44.dp,
            cellSpacing = 2.dp
        ),
        statisticsCard = StatisticsCardParams(
            padding = 12.dp,
            itemSpacing = 8.dp
        )
    )
}

/**
 * 简单的JSON序列化扩展（手动实现）
 */
fun DebugCalendarParams.toJsonString(): String {
    return buildString {
        append("{\n")
        append("  \"topAppBar\": {\n")
        append("    \"height\": ${topAppBar.height.value},\n")
        append("    \"titleFontSize\": ${topAppBar.titleFontSize.value},\n")
        append("    \"actionButtonSize\": ${topAppBar.actionButtonSize.value},\n")
        append("    \"todayButtonTextSize\": ${topAppBar.todayButtonTextSize.value}\n")
        append("  },\n")
        append("  \"statisticsCard\": {\n")
        append("    \"cornerRadius\": ${statisticsCard.cornerRadius.value},\n")
        append("    \"elevation\": ${statisticsCard.elevation.value},\n")
        append("    \"padding\": ${statisticsCard.padding.value},\n")
        append("    \"itemSpacing\": ${statisticsCard.itemSpacing.value},\n")
        append("    \"textSize\": ${statisticsCard.textSize.value}\n")
        append("  },\n")
        append("  \"calendarView\": {\n")
        append("    \"cellSize\": ${calendarView.cellSize.value},\n")
        append("    \"cellSpacing\": ${calendarView.cellSpacing.value},\n")
        append("    \"rowHeight\": ${calendarView.rowHeight.value},\n")
        append("    \"cornerRadius\": ${calendarView.cornerRadius.value},\n")
        append("    \"scheduleIndicatorSize\": ${calendarView.scheduleIndicatorSize.value},\n")
        append("    \"dateNumberTextSize\": ${calendarView.dateNumberTextSize.value},\n")
        append("    \"otherMonthDateOpacity\": ${calendarView.otherMonthDateOpacity}\n")
        append("  },\n")
        append("  \"detailCard\": {\n")
        append("    \"cornerRadius\": ${detailCard.cornerRadius.value},\n")
        append("    \"padding\": ${detailCard.padding.value},\n")
        append("    \"dateHeaderTextSize\": ${detailCard.dateHeaderTextSize.value},\n")
        append("    \"scheduleInfoTextSize\": ${detailCard.scheduleInfoTextSize.value}\n")
        append("  },\n")
        append("  \"layout\": {\n")
        append("    \"screenHorizontalPadding\": ${layout.screenHorizontalPadding.value},\n")
        append("    \"componentSpacing\": ${layout.componentSpacing.value},\n")
        append("    \"monthTransitionDuration\": ${layout.monthTransitionDuration}\n")
        append("  },\n")
        append("  \"fab\": {\n")
        append("    \"size\": ${fab.size.value},\n")
        append("    \"elevation\": ${fab.elevation.value},\n")
        append("    \"bottomMargin\": ${fab.bottomMargin.value}\n")
        append("  }\n")
        append("}")
    }
}

fun String.toDebugParams(): DebugCalendarParams? {
    return try {
        // 简单的JSON解析（仅支持基本格式）
        val json = this.trim()
        if (!json.startsWith("{") || !json.endsWith("}")) return null
        
        // 提取各部分的数值
        val topAppBarHeight = extractFloatValue(json, "topAppBar", "height") ?: 64f
        val topAppBarTitleFontSize = extractFloatValue(json, "topAppBar", "titleFontSize") ?: 20f
        val topAppBarActionButtonSize = extractFloatValue(json, "topAppBar", "actionButtonSize") ?: 48f
        val topAppBarTodayButtonTextSize = extractFloatValue(json, "topAppBar", "todayButtonTextSize") ?: 16f
        
        val statisticsCornerRadius = extractFloatValue(json, "statisticsCard", "cornerRadius") ?: 12f
        val statisticsElevation = extractFloatValue(json, "statisticsCard", "elevation") ?: 2f
        val statisticsPadding = extractFloatValue(json, "statisticsCard", "padding") ?: 16f
        val statisticsItemSpacing = extractFloatValue(json, "statisticsCard", "itemSpacing") ?: 8f
        val statisticsTextSize = extractFloatValue(json, "statisticsCard", "textSize") ?: 14f
        
        val calendarCellSize = extractFloatValue(json, "calendarView", "cellSize") ?: 48f
        val calendarCellSpacing = extractFloatValue(json, "calendarView", "cellSpacing") ?: 4f
        val calendarRowHeight = extractFloatValue(json, "calendarView", "rowHeight") ?: 56f
        val calendarCornerRadius = extractFloatValue(json, "calendarView", "cornerRadius") ?: 8f
        val calendarScheduleIndicatorSize = extractFloatValue(json, "calendarView", "scheduleIndicatorSize") ?: 6f
        val calendarDateNumberTextSize = extractFloatValue(json, "calendarView", "dateNumberTextSize") ?: 16f
        val calendarOtherMonthOpacity = extractFloatValue(json, "calendarView", "otherMonthDateOpacity") ?: 0.5f
        
        val detailCornerRadius = extractFloatValue(json, "detailCard", "cornerRadius") ?: 12f
        val detailPadding = extractFloatValue(json, "detailCard", "padding") ?: 16f
        val detailDateHeaderTextSize = extractFloatValue(json, "detailCard", "dateHeaderTextSize") ?: 16f
        val detailScheduleInfoTextSize = extractFloatValue(json, "detailCard", "scheduleInfoTextSize") ?: 14f
        
        val layoutScreenHorizontalPadding = extractFloatValue(json, "layout", "screenHorizontalPadding") ?: 16f
        val layoutComponentSpacing = extractFloatValue(json, "layout", "componentSpacing") ?: 16f
        val layoutMonthTransitionDuration = extractIntValue(json, "layout", "monthTransitionDuration") ?: 300
        
        val fabSize = extractFloatValue(json, "fab", "size") ?: 56f
        val fabElevation = extractFloatValue(json, "fab", "elevation") ?: 6f
        val fabBottomMargin = extractFloatValue(json, "fab", "bottomMargin") ?: 16f
        
        DebugCalendarParams(
            topAppBar = TopAppBarParams(
                height = topAppBarHeight.dp,
                titleFontSize = topAppBarTitleFontSize.sp,
                actionButtonSize = topAppBarActionButtonSize.dp,
                todayButtonTextSize = topAppBarTodayButtonTextSize.sp
            ),
            statisticsCard = StatisticsCardParams(
                cornerRadius = statisticsCornerRadius.dp,
                elevation = statisticsElevation.dp,
                padding = statisticsPadding.dp,
                itemSpacing = statisticsItemSpacing.dp,
                textSize = statisticsTextSize.sp
            ),
            calendarView = CalendarViewParams(
                cellSize = calendarCellSize.dp,
                cellSpacing = calendarCellSpacing.dp,
                rowHeight = calendarRowHeight.dp,
                cornerRadius = calendarCornerRadius.dp,
                scheduleIndicatorSize = calendarScheduleIndicatorSize.dp,
                dateNumberTextSize = calendarDateNumberTextSize.sp,
                otherMonthDateOpacity = calendarOtherMonthOpacity
            ),
            detailCard = DetailCardParams(
                cornerRadius = detailCornerRadius.dp,
                padding = detailPadding.dp,
                dateHeaderTextSize = detailDateHeaderTextSize.sp,
                scheduleInfoTextSize = detailScheduleInfoTextSize.sp
            ),
            layout = LayoutParams(
                screenHorizontalPadding = layoutScreenHorizontalPadding.dp,
                componentSpacing = layoutComponentSpacing.dp,
                monthTransitionDuration = layoutMonthTransitionDuration
            ),
            fab = FabParams(
                size = fabSize.dp,
                elevation = fabElevation.dp,
                bottomMargin = fabBottomMargin.dp
            )
        )
    } catch (e: Exception) {
        null
    }
}

private fun extractFloatValue(json: String, section: String, key: String): Float? {
    return try {
        val sectionStart = json.indexOf("\"$section\"")
        if (sectionStart == -1) return null
        val sectionEnd = json.indexOf("}", sectionStart)
        if (sectionEnd == -1) return null
        val sectionText = json.substring(sectionStart, sectionEnd)
        val keyPattern = "\"$key\"\\s*:\\s*([0-9.]+)".toRegex()
        val match = keyPattern.find(sectionText)
        match?.groupValues?.get(1)?.toFloatOrNull()
    } catch (e: Exception) {
        null
    }
}

private fun extractIntValue(json: String, section: String, key: String): Int? {
    return extractFloatValue(json, section, key)?.toInt()
}
