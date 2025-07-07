package com.ccxiaoji.feature.plan.domain.model

/**
 * 图表类型枚举
 * 用于进度分析页面的图表展示类型选择
 */
enum class ChartType {
    /**
     * 状态分布饼图
     */
    STATUS_PIE,
    
    /**
     * 进度分布条形图
     */
    PROGRESS_BAR,
    
    /**
     * 月度趋势图
     */
    MONTHLY_TREND,
    
    /**
     * 标签分析图
     */
    TAG_ANALYSIS
}