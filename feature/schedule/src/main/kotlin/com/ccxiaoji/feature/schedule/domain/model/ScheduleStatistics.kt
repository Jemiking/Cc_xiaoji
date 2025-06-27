package com.ccxiaoji.feature.schedule.domain.model

/**
 * 排班统计信息
 */
data class ScheduleStatistics(
    val totalDays: Int,
    val workDays: Int,
    val restDays: Int,
    val shiftDistribution: Map<String, Int>, // 班次名称 -> 天数
    val totalHours: Double
)