package com.ccxiaoji.app.data.backup.export

import com.ccxiaoji.app.data.backup.model.DataRange
import java.io.OutputStream

/**
 * CSV导出器接口
 */
interface CsvExporter {
    /**
     * 导出交易记录到CSV
     * @param outputStream 输出流
     * @param dateRange 数据日期范围
     * @param onProgress 进度回调
     * @return 导出统计信息
     */
    suspend fun exportTransactions(
        outputStream: OutputStream,
        dateRange: DataRange? = null,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): CsvExportStats
}

/**
 * CSV导出统计信息
 */
data class CsvExportStats(
    val totalRows: Int,
    val exportedRows: Int,
    val skippedRows: Int = 0,
    val fileSize: Long = 0
)