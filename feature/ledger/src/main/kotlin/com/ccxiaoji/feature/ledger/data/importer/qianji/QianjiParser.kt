package com.ccxiaoji.feature.ledger.data.importer.qianji

import java.io.BufferedReader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 钱迹数据解析器
 * 负责解析钱迹APP导出的CSV文件
 */
class QianjiParser {
    
    /**
     * 钱迹交易记录数据结构
     */
    data class QianjiRecord(
        val id: String,                    // ID
        val datetime: String,               // 时间
        val category: String,               // 分类
        val subCategory: String?,           // 二级分类
        val type: String,                   // 类型（支出/收入/退款）
        val amount: String,                 // 金额
        val currency: String,               // 币种
        val account1: String,               // 账户1
        val account2: String?,              // 账户2（转账时使用）
        val remark: String?,                // 备注
        val isReimbursed: String?,          // 已报销
        val fee: String?,                   // 手续费
        val coupon: String?,                // 优惠券
        val reporter: String?,              // 记账者
        val billMark: String?,              // 账单标记
        val tags: String?,                  // 标签
        val billImage: String?,             // 账单图片
        val relatedId: String?              // 关联账单
    )
    
    /**
     * 解析CSV文件
     * @param file CSV文件
     * @return 解析后的记录列表
     */
    fun parseFile(file: File): List<QianjiRecord> {
        android.util.Log.e("QIANJI_DEBUG", "parseFile: Starting parse for file: ${file.absolutePath}")
        val records = mutableListOf<QianjiRecord>()
        
        file.bufferedReader().use { reader ->
            // 读取并解析CSV
            android.util.Log.e("QIANJI_DEBUG", "Reading file lines...")
            val lines = reader.readLines()
            android.util.Log.e("QIANJI_DEBUG", "Total lines in file: ${lines.size}")
            
            if (lines.isEmpty()) {
                android.util.Log.w("QianjiParser", "File is empty, returning empty list")
                return emptyList()
            }
            
            // 第一行是表头
            android.util.Log.e("QIANJI_DEBUG", "Parsing headers from first line: ${lines[0]}")
            val headers = parseCSVLine(lines[0])
            android.util.Log.e("QIANJI_DEBUG", "Parsed headers (${headers.size}): ${headers.joinToString(", ")}")
            val headerMap = headers.withIndex().associate { it.value to it.index }
            android.util.Log.d("QianjiParser", "Header map created: ${headerMap}")
            
            // 解析数据行
            android.util.Log.e("QIANJI_DEBUG", "Starting to parse ${lines.size - 1} data rows")
            for (i in 1 until lines.size) {
                if (i <= 3 || i == lines.size - 1) { // 只记录前3行和最后一行
                    android.util.Log.e("QIANJI_DEBUG", "Parsing row $i: ${lines[i].take(100)}...")
                }
                val values = parseCSVLine(lines[i])
                if (values.size >= headers.size) {
                    try {
                        val record = QianjiRecord(
                            id = getValue(values, headerMap, "ID") ?: "",
                            datetime = getValue(values, headerMap, "时间") ?: "",
                            category = getValue(values, headerMap, "分类") ?: "",
                            subCategory = getValue(values, headerMap, "二级分类"),
                            type = getValue(values, headerMap, "类型") ?: "",
                            amount = getValue(values, headerMap, "金额") ?: "0",
                            currency = getValue(values, headerMap, "币种") ?: "CNY",
                            account1 = getValue(values, headerMap, "账户1") ?: "",
                            account2 = getValue(values, headerMap, "账户2"),
                            remark = getValue(values, headerMap, "备注"),
                            isReimbursed = getValue(values, headerMap, "已报销"),
                            fee = getValue(values, headerMap, "手续费"),
                            coupon = getValue(values, headerMap, "优惠券"),
                            reporter = getValue(values, headerMap, "记账者"),
                            billMark = getValue(values, headerMap, "账单标记"),
                            tags = getValue(values, headerMap, "标签"),
                            billImage = getValue(values, headerMap, "账单图片"),
                            relatedId = getValue(values, headerMap, "关联账单")
                        )
                        records.add(record)
                    } catch (e: Exception) {
                        // 跳过解析失败的行
                        android.util.Log.e("QianjiParser", "解析第${i + 1}行失败: ${e.message}", e)
                    }
                } else {
                    android.util.Log.w("QianjiParser", "Row $i has ${values.size} values but expected ${headers.size}")
                }
            }
        }
        
        android.util.Log.e("QIANJI_DEBUG", "Parsing complete, total records: ${records.size}")
        if (records.isNotEmpty()) {
            android.util.Log.e("QIANJI_DEBUG", "First record: ${records[0]}")
        }
        return records
    }
    
    /**
     * 解析CSV行
     * 正确处理引号、逗号和转义
     */
    fun parseCSVLine(line: String): List<String> {
        android.util.Log.d("QianjiParser", "parseCSVLine input: ${line.take(100)}...")
        val result = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        
        // 处理UTF-8 BOM
        val cleanLine = if (line.startsWith("\uFEFF")) {
            line.substring(1)
        } else {
            line
        }
        
        while (i < cleanLine.length) {
            val char = cleanLine[i]
            
            when {
                char == '"' -> {
                    if (inQuotes) {
                        // 检查是否是转义的引号（""）
                        if (i + 1 < cleanLine.length && cleanLine[i + 1] == '"') {
                            currentField.append('"')
                            i++ // 跳过下一个引号
                        } else {
                            // 结束引号
                            inQuotes = false
                        }
                    } else if (currentField.isEmpty() || (i > 0 && cleanLine[i - 1] == ',')) {
                        // 开始引号（字段开头或逗号后）
                        inQuotes = true
                    } else {
                        // 字段中间的引号，当作普通字符
                        currentField.append(char)
                    }
                }
                char == ',' && !inQuotes -> {
                    // 字段分隔符
                    result.add(currentField.toString())
                    currentField.clear()
                }
                else -> {
                    // 普通字符
                    currentField.append(char)
                }
            }
            i++
        }
        
        // 添加最后一个字段
        result.add(currentField.toString())
        
        android.util.Log.d("QianjiParser", "parseCSVLine result (${result.size}): ${result.take(5).joinToString(", ")}...")
        return result
    }
    
    /**
     * 从值列表中获取指定列的值
     */
    private fun getValue(values: List<String>, headerMap: Map<String, Int>, column: String): String? {
        val index = headerMap[column] ?: return null
        return if (index < values.size) {
            val value = values[index]
            if (value.isEmpty() || value == "null") null else value
        } else {
            null
        }
    }
    
    /**
     * 解析日期时间
     * 钱迹格式：2024-10-19 22:19:04
     */
    fun parseDateTime(datetime: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.parse(datetime, formatter)
    }
    
    /**
     * 验证是否为钱迹格式的CSV
     */
    fun isQianjiFormat(headers: List<String>): Boolean {
        val requiredHeaders = listOf("ID", "时间", "分类", "类型", "金额", "账户1")
        android.util.Log.d("QianjiParser", "isQianjiFormat: Checking headers")
        android.util.Log.d("QianjiParser", "Required headers: ${requiredHeaders.joinToString(", ")}")
        android.util.Log.d("QianjiParser", "Actual headers: ${headers.joinToString(", ")}")
        
        val isValid = headers.containsAll(requiredHeaders)
        
        if (!isValid) {
            val missing = requiredHeaders.filter { !headers.contains(it) }
            android.util.Log.w("QianjiParser", "Missing required headers: ${missing.joinToString(", ")}")
        } else {
            android.util.Log.d("QianjiParser", "All required headers found")
        }
        
        return isValid
    }
}