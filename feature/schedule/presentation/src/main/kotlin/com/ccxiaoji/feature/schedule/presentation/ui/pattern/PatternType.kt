package com.ccxiaoji.feature.schedule.presentation.ui.pattern

/**
 * 排班模式类型
 */
enum class PatternType(val displayName: String) {
    SINGLE("单次排班"),
    CYCLE("循环排班"),  // 原 WEEKLY，现在支持任意天数循环
    ROTATION("轮班"),
    CUSTOM("自定义")
}