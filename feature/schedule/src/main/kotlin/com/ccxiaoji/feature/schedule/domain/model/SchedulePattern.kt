package com.ccxiaoji.feature.schedule.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 排班模式
 * 用于定义循环排班规则
 */
sealed class SchedulePattern {
    
    /**
     * 单次排班
     */
    data class Single(
        val date: LocalDate,
        val shiftId: Long
    ) : SchedulePattern()
    
    /**
     * 循环排班（支持任意天数周期）
     * 原名 Weekly，现在扩展为支持任意天数的循环
     */
    data class Cycle(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val cycleDays: Int, // 循环天数（2-365）
        val cyclePattern: Map<Int, Long> // 循环中的第几天(0-based) -> ShiftId
    ) : SchedulePattern()
    
    /**
     * 按周循环排班（已废弃，使用 Cycle 代替）
     */
    @Deprecated("使用 Cycle 代替", ReplaceWith("Cycle"))
    data class Weekly(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val weekPattern: Map<DayOfWeek, Long>
    ) : SchedulePattern() {
        // 转换为新的 Cycle 格式
        fun toCycle(): Cycle = Cycle(
            startDate = startDate,
            endDate = endDate,
            cycleDays = 7,
            cyclePattern = weekPattern.mapKeys { (dayOfWeek, _) ->
                (dayOfWeek.value - 1) % 7 // 转换为 0-based 索引
            }
        )
    }
    
    /**
     * 固定循环排班（如三班倒）
     */
    data class Rotation(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val shiftIds: List<Long>, // 班次ID列表，按顺序循环
        val restDays: Int = 0 // 每个循环后的休息天数
    ) : SchedulePattern()
    
    /**
     * 自定义模式
     */
    data class Custom(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val pattern: List<Long?> // 每天对应的班次ID，null表示休息
    ) : SchedulePattern()
}