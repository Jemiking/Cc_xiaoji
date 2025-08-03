package com.ccxiaoji.app.data.backup.export

import com.ccxiaoji.app.data.backup.model.DataRange
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CSV导出器实现
 */
@Singleton
class CsvExporterImpl @Inject constructor() : CsvExporter {
    
    override suspend fun exportTransactions(
        outputStream: OutputStream,
        dateRange: DataRange?,
        onProgress: (Float, String) -> Unit
    ): CsvExportStats {
        // TODO: 实现CSV导出逻辑
        return CsvExportStats(
            totalRows = 0,
            exportedRows = 0
        )
    }
}