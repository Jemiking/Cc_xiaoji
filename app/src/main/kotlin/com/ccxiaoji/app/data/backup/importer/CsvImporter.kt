package com.ccxiaoji.app.data.backup.importer

import com.ccxiaoji.app.data.backup.model.ImportMode
import java.io.InputStream

/**
 * CSV导入器接口
 */
interface CsvImporter {
    /**
     * 从CSV导入交易记录
     * @param inputStream 输入流
     * @param mode 导入模式
     * @param onProgress 进度回调
     * @return 导入统计信息
     */
    suspend fun importTransactions(
        inputStream: InputStream,
        mode: ImportMode = ImportMode.APPEND,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): CsvImportStats
    
    /**
     * 验证CSV格式
     * @param inputStream 输入流
     * @return 验证结果
     */
    suspend fun validateCsvFormat(inputStream: InputStream): CsvValidationResult
}

/**
 * CSV导入统计信息
 */
data class CsvImportStats(
    val totalRows: Int,
    val importedRows: Int,
    val skippedRows: Int = 0,
    val errorRows: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * CSV验证结果
 */
data class CsvValidationResult(
    val isValid: Boolean,
    val format: CsvFormat? = null,
    val errors: List<String> = emptyList()
)

/**
 * CSV格式类型
 */
enum class CsvFormat {
    CC_XIAOJI,  // CC小记格式
    QIANJI,     // 钱迹格式
    UNKNOWN     // 未知格式
}