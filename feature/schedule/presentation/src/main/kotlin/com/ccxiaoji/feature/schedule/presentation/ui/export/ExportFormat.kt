package com.ccxiaoji.feature.schedule.presentation.ui.export

/**
 * 导出格式枚举
 */
enum class ExportFormat(
    val displayName: String,
    val description: String,
    val extension: String
) {
    CSV("CSV表格", "适合在Excel中查看和编辑", "csv"),
    JSON("JSON数据", "适合程序读取和处理", "json"),
    REPORT("统计报表", "人工阅读的文本格式", "txt")
}