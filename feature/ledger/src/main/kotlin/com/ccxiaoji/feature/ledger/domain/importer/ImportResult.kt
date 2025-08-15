package com.ccxiaoji.feature.ledger.domain.importer

/**
 * 导入结果
 */
data class ImportResult(
    val success: Boolean,
    val totalRows: Int,
    val successCount: Int,
    val failedCount: Int,
    val skippedCount: Int,
    val errors: List<ImportError>,
    val summary: ImportSummary,
    val duration: Long = 0  // 导入耗时（毫秒）
)

/**
 * 导入摘要
 */
data class ImportSummary(
    val accountsImported: Int = 0,
    val categoriesImported: Int = 0,
    val transactionsImported: Int = 0,
    val budgetsImported: Int = 0,
    val recurringImported: Int = 0,
    val savingsImported: Int = 0,
    val creditBillsImported: Int = 0
)

/**
 * 导入错误
 */
sealed class ImportError {
    abstract val line: Int
    abstract val message: String
    
    data class FormatError(
        override val line: Int,
        override val message: String
    ) : ImportError()
    
    data class ValidationError(
        override val line: Int,
        val field: String,
        override val message: String
    ) : ImportError()
    
    data class DependencyError(
        override val line: Int,
        val missingReference: String,
        override val message: String
    ) : ImportError()
    
    data class DatabaseError(
        override val line: Int,
        override val message: String,
        val cause: Throwable? = null
    ) : ImportError()
}