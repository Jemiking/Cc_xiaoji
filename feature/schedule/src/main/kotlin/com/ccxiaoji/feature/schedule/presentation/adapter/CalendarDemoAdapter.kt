package com.ccxiaoji.feature.schedule.presentation.adapter

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode
import com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelRenderConfig
import com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelRenderMode
import com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelVisual
import com.ccxiaoji.feature.schedule.presentation.demo.parts.MultiShiftMode
import com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewConfig
import com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewMode
import com.ccxiaoji.feature.schedule.presentation.debug.DebugCalendarParams
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.YearMonth

/**
 * 日历Demo适配器
 * 负责将现有的Schedule数据和配置转换为Demo组件需要的格式
 */
object CalendarDemoAdapter {
    
    /**
     * 将Schedule列表转换为DemoData
     */
    fun convertToDemoData(
        yearMonth: YearMonth,
        schedules: List<Schedule>
    ): DemoData {
        // 从Schedule中提取唯一的Shift列表
        val shifts = schedules.map { it.shift }.distinctBy { it.id }
        
        return DemoData(
            yearMonth = yearMonth,
            schedules = schedules,
            shifts = shifts
        )
    }
    
    /**
     * 将DefaultDebugParams转换为Demo配置
     */
    fun convertToLabelConfig(params: DebugCalendarParams): LabelRenderConfig {
        return LabelRenderConfig(
            mode = LabelRenderMode.LightBoundary,
            biggerLabel = false,
            weekendAlphaAdjust = false,
            todayRing = false,
            selectedBgBoost = true, // 保持选中底色增强效果
            spacingTuning = false,
            visual = LabelVisual.AbbrevSmall, // 使用小标签缩写模式
            multiMode = MultiShiftMode.TwoChipsPlusMore, // 支持+N显示
            labelFontSp = params.calendarView.dateNumberTextSize.value.toInt().coerceAtLeast(10),
            labelHPaddingDp = 4,
            forcePlusNPreview = false
        )
    }
    
    /**
     * 创建统计概览配置
     */
    fun convertToOverviewConfig(params: DebugCalendarParams): OverviewConfig {
        return OverviewConfig(OverviewMode.CardBorder)
    }
    
    /**
     * ViewMode与DisplayMode双向转换
     */
    fun viewModeToDisplayMode(viewMode: CalendarViewMode): DisplayMode {
        return when (viewMode) {
            CalendarViewMode.COMPACT -> DisplayMode.Compact
            CalendarViewMode.COMFORTABLE -> DisplayMode.Expanded
        }
    }
    
    fun displayModeToViewMode(displayMode: DisplayMode): CalendarViewMode {
        return when (displayMode) {
            DisplayMode.Compact -> CalendarViewMode.COMPACT
            DisplayMode.Expanded -> CalendarViewMode.COMFORTABLE
        }
    }
    
    /**
     * 获取固定的A3基线配置
     * 这是Demo A3的核心配置，提供最佳的用户体验
     */
    fun getA3BaselineConfig(): Triple<LabelRenderConfig, OverviewConfig, IndicatorStyle> {
        val labelConfig = LabelRenderConfig(
            mode = LabelRenderMode.LightBoundary,
            biggerLabel = false,
            weekendAlphaAdjust = false,
            todayRing = false,
            selectedBgBoost = true, // A3核心特性：选中底色增强
            spacingTuning = false,
            visual = LabelVisual.AbbrevSmall,
            multiMode = MultiShiftMode.TwoChipsPlusMore,
            labelFontSp = 11,
            labelHPaddingDp = 4,
            forcePlusNPreview = false
        )
        
        val overviewConfig = OverviewConfig(OverviewMode.CardBorder)
        val indicatorStyle = IndicatorStyle.Label // A3使用标签模式
        
        return Triple(labelConfig, overviewConfig, indicatorStyle)
    }
}