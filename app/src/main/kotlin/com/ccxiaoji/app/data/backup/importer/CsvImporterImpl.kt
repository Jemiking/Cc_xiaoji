package com.ccxiaoji.app.data.backup.importer

import com.ccxiaoji.app.data.backup.model.ImportMode
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CSV导入器实现
 */
@Singleton
class CsvImporterImpl @Inject constructor() : CsvImporter {
    
    override suspend fun importTransactions(
        inputStream: InputStream,
        mode: ImportMode,
        onProgress: (Float, String) -> Unit
    ): CsvImportStats {
        // TODO: 实现CSV导入逻辑
        return CsvImportStats(
            totalRows = 0,
            importedRows = 0
        )
    }
    
    override suspend fun validateCsvFormat(inputStream: InputStream): CsvValidationResult {
        // TODO: 实现CSV格式验证逻辑
        return CsvValidationResult(
            isValid = false,
            errors = listOf("验证功能尚未实现")
        )
    }
}