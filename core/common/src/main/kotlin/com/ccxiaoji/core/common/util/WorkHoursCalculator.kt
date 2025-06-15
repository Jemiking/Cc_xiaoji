package com.ccxiaoji.core.common.util

/**
 * 排班工时计算器
 * 专门处理排班系统中的工时计算逻辑
 * 
 * 业务规则：
 * 1. 相同时间（如 00:00 到 00:00）表示24小时班次
 * 2. 支持跨天班次计算（如 22:00 到 06:00）
 * 3. 精确到分钟级别
 */
object WorkHoursCalculator {
    
    /**
     * 计算班次工时
     * @param startTime 上班时间，格式 "HH:mm"
     * @param endTime 下班时间，格式 "HH:mm"
     * @return 工时（小时），保留小数
     */
    fun calculateShiftHours(startTime: String, endTime: String): Double {
        return DateConverter.calculateHours(startTime, endTime)
    }
    
    /**
     * 格式化工时显示
     * @param hours 工时数
     * @return 格式化字符串，如 "8小时"、"8.5小时"
     */
    fun formatHours(hours: Double): String {
        return if (hours == hours.toInt().toDouble()) {
            "${hours.toInt()}小时"
        } else {
            "${hours}小时"
        }
    }
    
    /**
     * 判断是否为全天班次
     * @param startTime 上班时间
     * @param endTime 下班时间
     * @return 是否为24小时班次
     */
    fun isFullDayShift(startTime: String, endTime: String): Boolean {
        return startTime == endTime && startTime == "00:00"
    }
    
    /**
     * 判断是否为跨天班次
     * @param startTime 上班时间
     * @param endTime 下班时间
     * @return 是否跨天
     */
    fun isOvernightShift(startTime: String, endTime: String): Boolean {
        val start = parseHourMinute(startTime)
        val end = parseHourMinute(endTime)
        
        // 如果结束时间的小时数小于开始时间，说明跨天
        // 但要排除相同时间的情况（24小时班）
        return end.first < start.first && startTime != endTime
    }
    
    /**
     * 解析时间字符串为小时和分钟
     * @param time 时间字符串，格式 "HH:mm"
     * @return Pair<小时, 分钟>
     */
    private fun parseHourMinute(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        } else {
            Pair(0, 0)
        }
    }
    
    /**
     * 验证时间格式是否正确
     * @param time 时间字符串
     * @return 是否为有效的时间格式
     */
    fun isValidTimeFormat(time: String): Boolean {
        val regex = Regex("^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$")
        return regex.matches(time)
    }
}