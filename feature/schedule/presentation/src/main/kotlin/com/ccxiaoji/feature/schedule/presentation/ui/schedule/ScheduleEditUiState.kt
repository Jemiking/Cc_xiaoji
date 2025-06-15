package com.ccxiaoji.feature.schedule.presentation.ui.schedule

/**
 * 排班编辑UI状态
 */
data class ScheduleEditUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)