package com.ccxiaoji.feature.plan.domain.model

import java.time.LocalTime

/**
 * 提醒设置
 * 
 * @property enabled 是否启用提醒
 * @property reminderTime 提醒时间
 * @property reminderDaysBefore 提前几天提醒
 * @property reminderType 提醒类型
 */
data class ReminderSettings(
    val enabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(9, 0),
    val reminderDaysBefore: Int = 1,
    val reminderType: ReminderType = ReminderType.DEADLINE
)

/**
 * 提醒类型
 */
enum class ReminderType {
    /** 截止日期提醒 */
    DEADLINE,
    
    /** 里程碑提醒 */
    MILESTONE,
    
    /** 进度提醒 */
    PROGRESS,
    
    /** 每日提醒 */
    DAILY
}