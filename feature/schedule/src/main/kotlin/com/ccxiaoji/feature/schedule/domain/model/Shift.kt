package com.ccxiaoji.feature.schedule.domain.model

import java.time.LocalTime

/**
 * 班次领域模型
 * 业务层使用的班次数据结构
 */
data class Shift(
    val id: Long = 0,
    val name: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Int,
    val description: String? = null,
    val isActive: Boolean = true
) {
    /**
     * 计算班次时长（小时）
     */
    val duration: Double
        get() {
            val start = startTime.toSecondOfDay()
            var end = endTime.toSecondOfDay()
            if (end < start) {
                // 跨天班次
                end += 24 * 60 * 60
            }
            return (end - start) / 3600.0
        }
    
    /**
     * 是否为跨天班次
     */
    val isOvernight: Boolean
        get() = endTime.isBefore(startTime)
    
    /**
     * 格式化的时间范围显示
     */
    val timeRangeText: String
        get() = "${startTime.format(TIME_FORMATTER)} - ${endTime.format(TIME_FORMATTER)}"
    
    companion object {
        private val TIME_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        
        /**
         * 预定义的班次颜色
         */
        val PRESET_COLORS = listOf(
            0xFF4CAF50.toInt(), // 绿色 - 早班
            0xFF2196F3.toInt(), // 蓝色 - 中班
            0xFF9C27B0.toInt(), // 紫色 - 晚班
            0xFFFF9800.toInt(), // 橙色 - 特殊班
            0xFFF44336.toInt(), // 红色 - 加班
        )
    }
}