package com.ccxiaoji.feature.schedule.presentation.ui.export

import com.ccxiaoji.feature.schedule.presentation.ui.statistics.TimeRange
import java.io.File
import java.time.LocalDate

/**
 * 导出界面UI状态
 */
data class ExportUiState(
    val timeRange: TimeRange = TimeRange.THIS_MONTH,
    val customStartDate: LocalDate = LocalDate.now().minusMonths(1),
    val customEndDate: LocalDate = LocalDate.now(),
    val exportFormat: ExportFormat = ExportFormat.CSV,
    val includeStatistics: Boolean = true,
    val includeActualTime: Boolean = false,
    val isLoading: Boolean = false,
    val exportedFile: File? = null,
    val errorMessage: String? = null,
    val exportHistory: List<ExportInfo> = emptyList()
)