package com.ccxiaoji.app.presentation.ui.profile

/**
 * 数据导出时间范围枚举
 */
enum class DateRange {
    /** 导出所有数据 */
    ALL,
    
    /** 导出本月数据 */
    THIS_MONTH,
    
    /** 导出本年数据 */
    THIS_YEAR,
    
    /** 导出最近三个月数据 */
    LAST_THREE_MONTHS,
    
    /** 自定义时间范围 */
    CUSTOM
}