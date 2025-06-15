package com.ccxiaoji.feature.schedule.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

/**
 * 排班领域模型
 * 业务层使用的排班数据结构
 */
@Immutable
data class Schedule(
    val id: Long = 0,
    val date: LocalDate,
    val shift: Shift,
    val note: String? = null,
    val actualStartTime: LocalTime? = null,
    val actualEndTime: LocalTime? = null
) {
    /**
     * 是否已打卡
     */
    val isCheckedIn: Boolean
        get() = actualStartTime != null || actualEndTime != null
    
    /**
     * 实际工时（小时）
     */
    val actualWorkHours: Double?
        get() {
            if (actualStartTime == null || actualEndTime == null) return null
            
            val start = actualStartTime.toSecondOfDay()
            var end = actualEndTime.toSecondOfDay()
            if (end < start) {
                // 跨天工作
                end += 24 * 60 * 60
            }
            return (end - start) / 3600.0
        }
    
    /**
     * 是否为今天的排班
     */
    fun isToday(): Boolean = date == LocalDate.now()
    
    /**
     * 是否为过去的排班
     */
    fun isPast(): Boolean = date.isBefore(LocalDate.now())
    
    /**
     * 是否为未来的排班
     */
    fun isFuture(): Boolean = date.isAfter(LocalDate.now())
}