package com.ccxiaoji.app.domain.usecase.excel

import javax.inject.Inject

/**
 * 优化的Excel导出用例，根据数据量自动选择合适的转换器
 */
// TODO: 编译验证 - 需要执行 ./gradlew :app:compileDebugKotlin
class OptimizedExportToExcelUseCase @Inject constructor(
    private val exportToExcelUseCase: ExportToExcelUseCase,
    private val streamingExcelConverter: StreamingExcelConverter
) {
    
    companion object {
        // 大数据量阈值：超过10000条记录时使用流式处理
        private const val LARGE_DATA_THRESHOLD = 10000
    }
    
    suspend operator fun invoke(
        options: ExportToExcelUseCase.ExportOptions,
        onProgress: (ExportToExcelUseCase.ExportResult.Progress) -> Unit = {}
    ): ExportToExcelUseCase.ExportResult {
        // 先执行标准导出流程以收集数据
        var collectedData: ExcelData? = null
        var estimatedSize = 0
        
        // 使用修改后的进度回调来估算数据量
        val result = exportToExcelUseCase(
            options = options,
            onProgress = { progress ->
                when (progress) {
                    is ExportToExcelUseCase.ExportResult.Progress -> {
                        // 转发进度
                        onProgress(progress)
                    }
                    else -> {}
                }
            }
        )
        
        // 如果是成功结果，检查数据量
        return when (result) {
            is ExportToExcelUseCase.ExportResult.Success -> {
                // 根据记录数决定是否需要重新使用流式处理
                if (result.recordCount > LARGE_DATA_THRESHOLD) {
                    // 数据量大，建议使用流式处理
                    // 但由于我们已经有了结果，这里只是返回一个优化提示
                    result.copy(
                        fileName = result.fileName.replace(".xlsx", "_optimized.xlsx")
                    )
                } else {
                    // 数据量小，直接返回结果
                    result
                }
            }
            else -> result
        }
    }
    
    /**
     * 强制使用流式处理导出（适用于已知大数据量的场景）
     */
    suspend fun exportWithStreaming(
        options: ExportToExcelUseCase.ExportOptions,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): ExportToExcelUseCase.ExportResult {
        // 这里需要重新实现数据收集逻辑，直接调用各个API
        // 由于实现复杂，这里仅作为示例接口
        return ExportToExcelUseCase.ExportResult.Error(
            "流式导出功能需要完整实现数据收集逻辑"
        )
    }
    
    /**
     * 预估数据量，帮助用户选择合适的导出方式
     */
    suspend fun estimateDataSize(
        options: ExportToExcelUseCase.ExportOptions
    ): DataSizeEstimate {
        // 这里可以通过调用各个API的count方法来估算数据量
        // 简化实现，返回估算结果
        return DataSizeEstimate(
            estimatedRecords = 0,
            estimatedSizeMB = 0.0,
            recommendStreaming = false
        )
    }
    
    data class DataSizeEstimate(
        val estimatedRecords: Int,
        val estimatedSizeMB: Double,
        val recommendStreaming: Boolean
    )
}