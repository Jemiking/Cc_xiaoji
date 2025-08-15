package com.ccxiaoji.feature.ledger.data.importer

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.importer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.ProducerScope
import java.io.BufferedReader
import java.io.File
import javax.inject.Inject

/**
 * 流式CSV导入器
 * 支持大文件的流式处理，避免内存溢出
 */
class StreamingCsvImporter @Inject constructor(
    private val csvParser: CsvParser,
    private val importOrchestrator: ImportOrchestrator
) {
    
    companion object {
        private const val TAG = "StreamingCsvImporter"
        private const val DEFAULT_BATCH_SIZE = 100
        private const val MAX_MEMORY_USAGE = 50 * 1024 * 1024 // 50MB
    }
    
    /**
     * 流式导入大文件
     */
    suspend fun importLargeFile(
        file: File,
        config: ImportConfig = ImportConfig(),
        onProgress: (ImportProgress) -> Unit = {}
    ): Flow<ImportProgress> = channelFlow<ImportProgress> {
        val startTime = System.currentTimeMillis()
        var totalProcessed = 0
        var successCount = 0
        var failedCount = 0
        var skippedCount = 0
        
        withContext(Dispatchers.IO) {
            file.bufferedReader().use { reader ->
                val batch = mutableListOf<String>()
                var currentBatchSize = 0
                val batchSize = config.batchSize
                
                // 跳过头部注释
                val header = skipHeaderAndComments(reader)
                
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // 跳过空行和注释
                    if (line!!.isBlank() || line!!.startsWith("#")) continue
                    
                    batch.add(line!!)
                    currentBatchSize++
                    
                    // 达到批次大小或内存限制时处理
                    if (currentBatchSize >= batchSize || shouldFlushBatch(batch)) {
                        val batchResult = processBatch(batch, config)
                    
                    totalProcessed += batchResult.processed
                    successCount += batchResult.success
                    failedCount += batchResult.failed
                    skippedCount += batchResult.skipped
                    
                    val progress = ImportProgress(
                        totalRows = totalProcessed,
                        processedRows = totalProcessed,
                        successCount = successCount,
                        failedCount = failedCount,
                        skippedCount = skippedCount,
                        currentBatch = totalProcessed / batchSize,
                        message = "处理中... (批次 ${totalProcessed / batchSize})"
                    )
                        
                        send(progress)
                        onProgress(progress)
                        
                        batch.clear()
                        currentBatchSize = 0
                    }
                }
                
                // 处理剩余数据
                if (batch.isNotEmpty()) {
                    val batchResult = processBatch(batch, config)
                    
                    totalProcessed += batchResult.processed
                    successCount += batchResult.success
                    failedCount += batchResult.failed
                    skippedCount += batchResult.skipped
                    
                    val progress = ImportProgress(
                        totalRows = totalProcessed,
                        processedRows = totalProcessed,
                        successCount = successCount,
                        failedCount = failedCount,
                        skippedCount = skippedCount,
                        currentBatch = (totalProcessed / batchSize) + 1,
                        message = "导入完成"
                    )
                    
                    send(progress)
                    onProgress(progress)
                }
            }
        }
        
        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "流式导入完成: 总计 $totalProcessed 条, 耗时 ${duration}ms")
    }
    
    /**
     * 跳过文件头部和注释
     */
    private fun skipHeaderAndComments(reader: BufferedReader): List<String> {
        val header = mutableListOf<String>()
        var line: String?
        
        while (reader.readLine().also { line = it } != null) {
            if (line!!.startsWith("#") || line!!.contains("格式版本")) {
                header.add(line!!)
            } else if (line!!.contains("类型,") || line!!.contains("数据类型,")) {
                header.add(line!!)
                break
            }
        }
        
        return header
    }
    
    /**
     * 判断是否应该刷新批次（基于内存使用）
     */
    private fun shouldFlushBatch(batch: List<String>): Boolean {
        val estimatedSize = batch.sumOf { it.length * 2 } // 估算UTF-16编码大小
        return estimatedSize > MAX_MEMORY_USAGE
    }
    
    /**
     * 处理一批数据
     */
    private suspend fun processBatch(
        lines: List<String>,
        config: ImportConfig
    ): BatchResult = withContext(Dispatchers.IO) {
        try {
            // 解析批次数据
            val parsedLines = lines.mapNotNull { line ->
                try {
                    parseDataLine(line)
                } catch (e: Exception) {
                    Log.e(TAG, "解析行失败: $line", e)
                    null
                }
            }
            
            // 按类型分组
            val grouped = parsedLines.groupBy { it.type }
            
            // 处理各类型数据
            val result = importOrchestrator.processBatchData(
                grouped,
                config
            )
            
            BatchResult(
                processed = lines.size,
                success = result.successCount,
                failed = result.failedCount,
                skipped = result.skippedCount
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "批次处理失败", e)
            BatchResult(
                processed = lines.size,
                success = 0,
                failed = lines.size,
                skipped = 0
            )
        }
    }
    
    private fun parseDataLine(line: String): DataLine? {
        val parts = line.split(",").map { it.trim() }
        if (parts.isEmpty()) return null
        
        return DataLine(
            line = 0, // 行号在流式处理中不重要
            type = parts[0],
            data = parts.drop(1)
        )
    }
}

/**
 * 导入进度
 */
data class ImportProgress(
    val totalRows: Int,
    val processedRows: Int,
    val successCount: Int,
    val failedCount: Int,
    val skippedCount: Int,
    val currentBatch: Int,
    val message: String
)

/**
 * 批次处理结果
 */
data class BatchResult(
    val processed: Int,
    val success: Int,
    val failed: Int,
    val skipped: Int
)