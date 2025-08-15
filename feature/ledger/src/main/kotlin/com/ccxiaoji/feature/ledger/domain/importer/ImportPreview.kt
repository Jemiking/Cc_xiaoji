package com.ccxiaoji.feature.ledger.domain.importer

/**
 * 导入预览信息
 */
data class ImportPreview(
    val fileName: String,
    val fileSize: Long,
    val format: String,
    val version: String?,
    val totalRows: Int,
    val dataTypes: Map<String, Int>,  // 数据类型和数量
    val dateRange: DateRange?,
    val hasErrors: Boolean,
    val errors: List<ImportError> = emptyList()
)

data class DateRange(
    val start: Long,
    val end: Long
)