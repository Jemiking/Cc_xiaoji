package com.ccxiaoji.feature.schedule.presentation.adapter

import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import com.ccxiaoji.feature.schedule.presentation.demo.parts.DisplayMode
import com.ccxiaoji.feature.schedule.presentation.demo.parts.LabelConfig
import com.ccxiaoji.feature.schedule.presentation.demo.parts.OverviewConfig
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.YearMonth

/**
 * Demo A3 适配器占位：提供所需转换与默认配置
 */
object CalendarDemoAdapter {
    fun viewModeToDisplayMode(viewMode: CalendarViewMode): DisplayMode = when (viewMode) {
        CalendarViewMode.COMPACT -> DisplayMode.Compact
        CalendarViewMode.COMFORTABLE -> DisplayMode.Expanded
    }

    /**
     * 将正式模块数据转换为 Demo 组件需要的结构（占位：直接返回原始对）
     */
    fun convertToDemoData(yearMonth: YearMonth, schedules: List<Schedule>): Any = Pair(yearMonth, schedules)

    /**
     * 返回 A3 的基线配置（占位返回）
     */
    fun getA3BaselineConfig(): Triple<LabelConfig, OverviewConfig, IndicatorStyle> {
        return Triple(LabelConfig(), OverviewConfig(), IndicatorStyle.Default)
    }
}

