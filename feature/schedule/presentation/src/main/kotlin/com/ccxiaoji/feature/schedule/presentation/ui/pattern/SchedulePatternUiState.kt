package com.ccxiaoji.feature.schedule.presentation.ui.pattern

import com.ccxiaoji.feature.schedule.domain.model.Shift
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 排班模式UI状态
 */
data class SchedulePatternUiState(
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(6),
    val patternType: PatternType = PatternType.SINGLE,
    
    // 单次模式
    val selectedShift: Shift? = null,
    
    // 循环模式（原周循环模式）
    val cycleDays: Int = 7, // 循环天数，默认7天
    val cyclePattern: Map<Int, Long?> = (0 until 7).associateWith { null }, // 循环中每天的班次
    val weekPattern: Map<DayOfWeek, Long?> = DayOfWeek.values().associateWith { null }, // 保留以兼容旧代码
    
    // 轮班模式
    val rotationShifts: List<Long> = emptyList(),
    val restDays: Int = 0,
    
    // 自定义模式
    val customPattern: List<Long?> = emptyList(),
    
    // 状态
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * 是否可以创建排班
     */
    val canCreate: Boolean
        get() = when (patternType) {
            PatternType.SINGLE -> selectedShift != null
            PatternType.CYCLE -> cyclePattern.values.any { it != null }
            PatternType.ROTATION -> rotationShifts.isNotEmpty()
            PatternType.CUSTOM -> customPattern.isNotEmpty()
        }
}