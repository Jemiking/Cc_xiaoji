package com.ccxiaoji.feature.ledger.data.importer

import android.util.Log
import com.ccxiaoji.feature.ledger.domain.importer.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * CSV文件解析器
 * 支持v2.0和v2.1格式
 */
class CsvParser @Inject constructor() {
    
    companion object {
        private const val TAG = "CsvParser"
        private const val COMMENT_PREFIX = "#"
        private const val HEADER_LINE = "类型,"
        private const val HEADER_LINE_V20 = "数据类型,"
    }
    
    /**
     * 解析CSV文件
     */
    fun parseFile(file: File): ParsedData {
        val errors = mutableListOf<ImportError>()
        val dataLines = mutableListOf<DataLine>()
        var version: String? = null
        var lineNumber = 0
        
        file.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                lineNumber++
                
                // 跳过空行
                if (line.isBlank()) return@forEachLine
                
                // 解析版本信息
                if (line.contains("格式版本:")) {
                    version = extractVersion(line)
                    return@forEachLine
                }
                
                // 跳过注释行
                if (line.startsWith(COMMENT_PREFIX)) return@forEachLine
                
                // 跳过标题行
                if (line.startsWith(HEADER_LINE) || line.startsWith(HEADER_LINE_V20)) {
                    return@forEachLine
                }
                
                // 解析数据行
                try {
                    val dataLine = parseDataLine(line, lineNumber)
                    if (dataLine != null) {
                        dataLines.add(dataLine)
                    }
                } catch (e: Exception) {
                    errors.add(ImportError.FormatError(lineNumber, "解析失败: ${e.message}"))
                }
            }
        }
        
        // 按数据类型分组
        val groupedData = dataLines.groupBy { it.type }
        
        // 统计数据类型数量
        val dataTypeCounts = groupedData.mapValues { it.value.size }
        
        // 计算日期范围
        val dateRange = calculateDateRange(dataLines)
        
        return ParsedData(
            version = version ?: detectVersion(dataLines),
            totalRows = dataLines.size,
            dataTypeCounts = dataTypeCounts,
            dateRange = dateRange,
            errors = errors,
            headers = groupedData["HEADER"] ?: emptyList(),
            accounts = groupedData["ACCOUNT"] ?: emptyList(),
            categories = groupedData["CATEGORY"] ?: emptyList(),
            transactions = groupedData["TRANSACTION"] ?: emptyList(),
            budgets = groupedData["BUDGET"] ?: emptyList(),
            recurringTransactions = groupedData["RECURRING"] ?: emptyList(),
            savingsGoals = groupedData["SAVINGS"] ?: emptyList(),
            creditBills = groupedData["CREDITBILL"] ?: emptyList()
        )
    }
    
    private fun parseDataLine(line: String, lineNumber: Int): DataLine? {
        val parts = line.split(",").map { it.trim() }
        
        if (parts.isEmpty()) return null
        
        val type = parts[0]
        val data = parts.drop(1)
        
        return DataLine(
            line = lineNumber,
            type = type,
            data = data
        )
    }
    
    private fun extractVersion(line: String): String? {
        val regex = "\\d+\\.\\d+".toRegex()
        return regex.find(line)?.value
    }
    
    private fun detectVersion(dataLines: List<DataLine>): String {
        // 根据数据格式特征检测版本
        // v2.1 有更多的字段说明
        // v2.0 格式较简单
        return if (dataLines.any { it.data.size >= 9 }) "2.1" else "2.0"
    }
    
    private fun calculateDateRange(dataLines: List<DataLine>): DateRange? {
        val transactions = dataLines.filter { it.type == "TRANSACTION" }
        if (transactions.isEmpty()) return null
        
        val dates = transactions.mapNotNull { dataLine ->
            val dateStr = dataLine.data.getOrNull(0) ?: return@mapNotNull null
            parseDateTime(dateStr) ?: parseDate(dateStr)
        }
        
        if (dates.isEmpty()) return null
        
        return DateRange(
            start = dates.minOrNull() ?: 0L,
            end = dates.maxOrNull() ?: System.currentTimeMillis()
        )
    }
    
    private fun parseDate(dateStr: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    private fun parseDateTime(dateTimeStr: String): Long? {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateTimeStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * 解析后的数据
 */
data class ParsedData(
    val version: String,
    val totalRows: Int,
    val dataTypeCounts: Map<String, Int>,
    val dateRange: DateRange?,
    val errors: List<ImportError>,
    val headers: List<DataLine>,
    val accounts: List<DataLine>,
    val categories: List<DataLine>,
    val transactions: List<DataLine>,
    val budgets: List<DataLine>,
    val recurringTransactions: List<DataLine>,
    val savingsGoals: List<DataLine>,
    val creditBills: List<DataLine>
)

/**
 * 数据行
 */
data class DataLine(
    val line: Int,
    val type: String,
    val data: List<String>
)