package com.ccxiaoji.feature.schedule.presentation.ui.settings

import java.time.DayOfWeek

/**
 * 设置界面UI状态
 */
data class SettingsUiState(
    // 通知设置
    val notificationEnabled: Boolean = true,
    val notificationTime: String = "08:00",
    
    // 通用设置
    val weekStartDay: String = "星期一",
    val weekStartDayValue: DayOfWeek = DayOfWeek.MONDAY,
    
    // 数据管理
    val autoBackupEnabled: Boolean = false,
    val lastBackupTime: String? = null,
    
    // 外观
    val darkMode: DarkModeOption = DarkModeOption.SYSTEM,
    val isDarkMode: Boolean = false,
    
    // 应用信息
    val appVersion: String = "1.0",
    
    // 状态
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)