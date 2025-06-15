package com.ccxiaoji.feature.schedule.presentation.ui.statistics

import java.time.LocalDate

/**
 * 统计界面UI状态
 */
data class ScheduleStatisticsUiState(
    val timeRange: TimeRange = TimeRange.THIS_MONTH,
    val customStartDate: LocalDate = LocalDate.now().minusMonths(1),
    val customEndDate: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)