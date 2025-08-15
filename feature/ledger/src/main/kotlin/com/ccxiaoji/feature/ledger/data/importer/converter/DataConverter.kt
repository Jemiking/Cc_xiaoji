package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据转换器基类
 */
abstract class DataConverter<T> {
    
    protected val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    protected val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * 转换数据行为实体
     */
    abstract fun convert(dataLine: DataLine, userId: String): ConvertResult<T>
    
    /**
     * 安全解析字符串
     */
    protected fun safeGetString(data: List<String>, index: Int, default: String = ""): String {
        return data.getOrNull(index)?.trim() ?: default
    }
    
    /**
     * 安全解析Double
     */
    protected fun safeGetDouble(data: List<String>, index: Int, default: Double = 0.0): Double {
        return try {
            data.getOrNull(index)?.toDouble() ?: default
        } catch (e: Exception) {
            default
        }
    }
    
    /**
     * 安全解析Int
     */
    protected fun safeGetInt(data: List<String>, index: Int, default: Int = 0): Int {
        return try {
            data.getOrNull(index)?.toInt() ?: default
        } catch (e: Exception) {
            default
        }
    }
    
    /**
     * 安全解析Boolean
     */
    protected fun safeGetBoolean(data: List<String>, index: Int, default: Boolean = false): Boolean {
        val value = safeGetString(data, index)
        return value == "是" || value == "true" || value == "1"
    }
    
    /**
     * 解析日期
     */
    protected fun parseDate(dateStr: String): Long? {
        return try {
            dateFormat.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 解析日期时间
     */
    protected fun parseDateTime(dateTimeStr: String): Long? {
        return try {
            dateTimeFormat.parse(dateTimeStr)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 金额转换为分
     */
    protected fun amountToCents(amount: Double): Long {
        return (amount * 100).toLong()
    }
}

/**
 * 转换结果
 */
sealed class ConvertResult<T> {
    data class Success<T>(val data: T) : ConvertResult<T>()
    data class Error<T>(val error: ImportError) : ConvertResult<T>()
}