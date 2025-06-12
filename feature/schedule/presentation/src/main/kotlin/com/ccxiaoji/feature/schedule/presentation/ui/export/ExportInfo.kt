package com.ccxiaoji.feature.schedule.presentation.ui.export

import java.io.File
import java.time.LocalDateTime

/**
 * 导出信息
 */
data class ExportInfo(
    val file: File,
    val fileName: String,
    val format: ExportFormat,
    val exportTime: LocalDateTime
)