package com.ccxiaoji.common.util

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 时间戳适配器
 * 用于处理不同格式的时间戳，包括Protobuf格式
 */
@Singleton
class TimestampAdapter @Inject constructor() {
    
    /**
     * 解析时间戳
     * 支持多种格式：
     * - 标准毫秒时间戳 (Long)
     * - Protobuf格式 (Map with value.seconds and value.nanos)
     * - ISO日期字符串
     */
    fun parseTimestamp(value: Any?): Long {
        return when (value) {
            // 标准毫秒时间戳
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            
            // Protobuf格式（当前导出格式）
            is Map<*, *> -> {
                parseProtobufTimestamp(value)
            }
            
            // ISO日期字符串
            is String -> {
                parseStringTimestamp(value)
            }
            
            null -> System.currentTimeMillis()
            
            else -> {
                // 尝试转换为字符串再解析
                try {
                    parseStringTimestamp(value.toString())
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }
    
    /**
     * 解析Protobuf格式的时间戳
     */
    private fun parseProtobufTimestamp(map: Map<*, *>): Long {
        return try {
            val innerValue = map["value"] as? Map<*, *>
            if (innerValue != null) {
                val seconds = (innerValue["seconds"] as? Number)?.toLong() ?: 0
                val nanos = (innerValue["nanos"] as? Number)?.toLong() ?: 0
                seconds * 1000 + nanos / 1_000_000
            } else {
                // 可能是直接的seconds/nanos格式
                val seconds = (map["seconds"] as? Number)?.toLong() ?: 0
                val nanos = (map["nanos"] as? Number)?.toLong() ?: 0
                seconds * 1000 + nanos / 1_000_000
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    /**
     * 解析字符串格式的时间戳
     */
    private fun parseStringTimestamp(value: String): Long {
        // 如果是纯数字，尝试作为时间戳解析
        if (value.all { it.isDigit() }) {
            return try {
                value.toLong()
            } catch (e: NumberFormatException) {
                System.currentTimeMillis()
            }
        }
        
        // 尝试多种日期格式
        val dateFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // ISO格式带毫秒
            "yyyy-MM-dd'T'HH:mm:ss'Z'",       // ISO格式
            "yyyy-MM-dd'T'HH:mm:ss",          // ISO格式无时区
            "yyyy-MM-dd HH:mm:ss",            // 标准格式
            "yyyy-MM-dd",                     // 仅日期
            "yyyy/MM/dd HH:mm:ss",            // 斜杠分隔
            "yyyy/MM/dd"                      // 斜杠分隔仅日期
        )
        
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(value)
                if (date != null) {
                    return date.time
                }
            } catch (e: Exception) {
                // 继续尝试下一个格式
            }
        }
        
        // 如果所有格式都失败，返回当前时间
        return System.currentTimeMillis()
    }
    
    /**
     * 格式化时间戳为指定格式
     */
    fun formatTimestamp(timestamp: Long, format: TimestampFormat): Any {
        return when (format) {
            TimestampFormat.MILLISECONDS -> timestamp
            
            TimestampFormat.PROTOBUF -> mapOf(
                "value" to mapOf(
                    "seconds" to timestamp / 1000,
                    "nanos" to (timestamp % 1000) * 1_000_000
                )
            )
            
            TimestampFormat.ISO_STRING -> {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.format(Date(timestamp))
            }
            
            TimestampFormat.SIMPLE_STRING -> {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(timestamp))
            }
        }
    }
    
    /**
     * 将时间戳转换为本地时间字符串
     */
    fun toLocalDateTimeString(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
    
    /**
     * 将时间戳转换为本地日期字符串
     */
    fun toLocalDateString(timestamp: Long, pattern: String = "yyyy-MM-dd"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
    
    /**
     * 将时间戳转换为本地时间字符串
     */
    fun toLocalTimeString(timestamp: Long, pattern: String = "HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}

/**
 * 时间戳格式枚举
 */
enum class TimestampFormat {
    MILLISECONDS,      // 标准毫秒时间戳
    PROTOBUF,         // Protobuf格式
    ISO_STRING,       // ISO 8601格式字符串
    SIMPLE_STRING     // 简单格式字符串
}