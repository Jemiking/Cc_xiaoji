package com.ccxiaoji.feature.schedule.presentation.ui.shift

/**
 * 班次管理UI状态
 */
data class ShiftUiState(
    val isLoading: Boolean = false,
    val showShiftDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)