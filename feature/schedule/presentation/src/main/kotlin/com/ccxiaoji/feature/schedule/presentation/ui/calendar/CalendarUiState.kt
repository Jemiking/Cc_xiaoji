package com.ccxiaoji.feature.schedule.presentation.ui.calendar

import androidx.compose.runtime.Stable

/**
 * 日历UI状态
 */
@Stable
data class CalendarUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)