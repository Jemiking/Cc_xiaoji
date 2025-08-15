package com.ccxiaoji.feature.ledger.data.importer

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import com.ccxiaoji.feature.ledger.domain.importer.ImportConfig
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * 导入错误处理器
 * 提供错误恢复和重试机制
 */
class ImportErrorHandler @Inject constructor() {
    
    companion object {
        private const val TAG = "ImportErrorHandler"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
    }
    
    /**
     * 处理导入错误
     */
    suspend fun handleError(
        error: ImportError,
        config: ImportConfig,
        retryAttempt: Int = 0
    ): ErrorHandlingResult {
        Log.e(TAG, "处理导入错误: ${error.message}")
        
        return when (error) {
            is ImportError.FormatError -> handleFormatError(error, config)
            is ImportError.ValidationError -> handleValidationError(error, config)
            is ImportError.DependencyError -> handleDependencyError(error, config)
            is ImportError.DatabaseError -> handleDatabaseError(error, config, retryAttempt)
        }
    }
    
    /**
     * 处理格式错误
     */
    private fun handleFormatError(
        error: ImportError.FormatError,
        config: ImportConfig
    ): ErrorHandlingResult {
        // 格式错误通常无法恢复
        return if (config.skipInvalidRows) {
            ErrorHandlingResult.Skip(
                reason = "跳过格式错误的行: ${error.message}"
            )
        } else {
            ErrorHandlingResult.Fail(
                error = error,
                canRetry = false
            )
        }
    }
    
    /**
     * 处理验证错误
     */
    private fun handleValidationError(
        error: ImportError.ValidationError,
        config: ImportConfig
    ): ErrorHandlingResult {
        // 验证错误可以选择跳过或修正
        return when {
            config.skipInvalidRows -> {
                ErrorHandlingResult.Skip(
                    reason = "跳过验证失败的数据: ${error.field} - ${error.message}"
                )
            }
            config.autoFixErrors -> {
                // 尝试自动修正
                ErrorHandlingResult.AutoFix(
                    suggestion = getSuggestionForValidationError(error)
                )
            }
            else -> {
                ErrorHandlingResult.Fail(
                    error = error,
                    canRetry = false
                )
            }
        }
    }
    
    /**
     * 处理依赖错误
     */
    private fun handleDependencyError(
        error: ImportError.DependencyError,
        config: ImportConfig
    ): ErrorHandlingResult {
        // 依赖错误可能需要延后处理
        return if (config.allowPartialImport) {
            ErrorHandlingResult.Defer(
                reason = "延后处理，等待依赖项导入: ${error.missingReference}"
            )
        } else {
            ErrorHandlingResult.Fail(
                error = error,
                canRetry = false
            )
        }
    }
    
    /**
     * 处理数据库错误
     */
    private suspend fun handleDatabaseError(
        error: ImportError.DatabaseError,
        config: ImportConfig,
        retryAttempt: Int
    ): ErrorHandlingResult {
        // 数据库错误可能是暂时的，可以重试
        return if (retryAttempt < MAX_RETRY_ATTEMPTS && config.enableRetry) {
            delay(RETRY_DELAY_MS * (retryAttempt + 1))
            ErrorHandlingResult.Retry(
                attempt = retryAttempt + 1,
                delayMs = RETRY_DELAY_MS * (retryAttempt + 1)
            )
        } else {
            ErrorHandlingResult.Fail(
                error = error,
                canRetry = false
            )
        }
    }
    
    /**
     * 获取验证错误的修正建议
     */
    private fun getSuggestionForValidationError(
        error: ImportError.ValidationError
    ): String {
        return when {
            error.message.contains("不能为空") -> {
                "使用默认值填充空字段"
            }
            error.message.contains("超过") && error.message.contains("字符") -> {
                "截断过长的文本"
            }
            error.message.contains("格式错误") -> {
                "尝试修正格式"
            }
            error.message.contains("必须大于0") -> {
                "使用最小有效值"
            }
            else -> {
                "无法自动修正，建议跳过"
            }
        }
    }
    
    /**
     * 批量处理错误
     */
    suspend fun handleBatchErrors(
        errors: List<ImportError>,
        config: ImportConfig
    ): BatchErrorHandlingResult {
        val results = mutableMapOf<ImportError, ErrorHandlingResult>()
        val retriableErrors = mutableListOf<ImportError>()
        val skippedErrors = mutableListOf<ImportError>()
        val failedErrors = mutableListOf<ImportError>()
        
        errors.forEach { error ->
            val result = handleError(error, config)
            results[error] = result
            
            when (result) {
                is ErrorHandlingResult.Retry -> retriableErrors.add(error)
                is ErrorHandlingResult.Skip -> skippedErrors.add(error)
                is ErrorHandlingResult.Fail -> failedErrors.add(error)
                is ErrorHandlingResult.Defer -> retriableErrors.add(error)
                is ErrorHandlingResult.AutoFix -> {
                    // 自动修正的错误可能需要重试
                    retriableErrors.add(error)
                }
            }
        }
        
        return BatchErrorHandlingResult(
            totalErrors = errors.size,
            retriableCount = retriableErrors.size,
            skippedCount = skippedErrors.size,
            failedCount = failedErrors.size,
            results = results,
            retriableErrors = retriableErrors,
            skippedErrors = skippedErrors,
            failedErrors = failedErrors
        )
    }
    
    /**
     * 生成错误报告
     */
    fun generateErrorReport(errors: List<ImportError>): ErrorReport {
        val groupedByType = errors.groupBy { it::class.simpleName }
        val errorFrequency = mutableMapOf<String, Int>()
        
        errors.forEach { error ->
            val key = when (error) {
                is ImportError.ValidationError -> "验证错误: ${error.field}"
                is ImportError.DependencyError -> "依赖错误: ${error.missingReference}"
                else -> error::class.simpleName ?: "未知错误"
            }
            errorFrequency[key] = errorFrequency.getOrDefault(key, 0) + 1
        }
        
        val topErrors = errorFrequency.entries
            .sortedByDescending { it.value }
            .take(10)
            .map { ErrorSummary(it.key, it.value) }
        
        return ErrorReport(
            totalErrors = errors.size,
            errorsByType = groupedByType.mapValues { it.value.size },
            topErrors = topErrors,
            suggestions = generateSuggestions(errors)
        )
    }
    
    /**
     * 生成改进建议
     */
    private fun generateSuggestions(errors: List<ImportError>): List<String> {
        val suggestions = mutableListOf<String>()
        
        val errorTypes = errors.groupBy { it::class }
        
        if (errorTypes.containsKey(ImportError.FormatError::class)) {
            suggestions.add("检查CSV文件格式是否符合v2.1标准")
        }
        
        if (errorTypes.containsKey(ImportError.ValidationError::class)) {
            val validationErrors = errors.filterIsInstance<ImportError.ValidationError>()
            val fieldErrors = validationErrors.groupBy { it.field }
            
            fieldErrors.forEach { (field, errors) ->
                if (errors.size > 5) {
                    suggestions.add("字段「$field」存在大量验证错误，建议检查数据质量")
                }
            }
        }
        
        if (errorTypes.containsKey(ImportError.DependencyError::class)) {
            suggestions.add("确保按正确顺序导入数据：账户 → 分类 → 交易")
        }
        
        if (errorTypes.containsKey(ImportError.DatabaseError::class)) {
            suggestions.add("检查数据库连接和存储空间")
        }
        
        return suggestions
    }
}

/**
 * 错误处理结果
 */
sealed class ErrorHandlingResult {
    data class Retry(val attempt: Int, val delayMs: Long) : ErrorHandlingResult()
    data class Skip(val reason: String) : ErrorHandlingResult()
    data class Fail(val error: ImportError, val canRetry: Boolean) : ErrorHandlingResult()
    data class Defer(val reason: String) : ErrorHandlingResult()
    data class AutoFix(val suggestion: String) : ErrorHandlingResult()
}

/**
 * 批量错误处理结果
 */
data class BatchErrorHandlingResult(
    val totalErrors: Int,
    val retriableCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
    val results: Map<ImportError, ErrorHandlingResult>,
    val retriableErrors: List<ImportError>,
    val skippedErrors: List<ImportError>,
    val failedErrors: List<ImportError>
)

/**
 * 错误报告
 */
data class ErrorReport(
    val totalErrors: Int,
    val errorsByType: Map<String?, Int>,
    val topErrors: List<ErrorSummary>,
    val suggestions: List<String>
)

/**
 * 错误摘要
 */
data class ErrorSummary(
    val description: String,
    val count: Int
)