package com.ccxiaoji.core.common.util

import kotlinx.datetime.*
import java.time.LocalDate as JavaLocalDate
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.ZoneId
import java.time.Instant as JavaInstant

/**
 * 统一的日期类型转换器
 * 负责处理 java.time 和 kotlinx.datetime 之间的转换
 * 
 * 使用规则：
 * - 数据库层：使用 kotlinx.datetime
 * - API接口层：使用 java.time
 * - 转换时机：在模块边界进行转换
 */
object DateConverter {
    
    // ========== LocalDate 转换 ==========
    
    /**
     * 将 java.time.LocalDate 转换为 kotlinx.datetime.LocalDate
     */
    fun toKotlinDate(date: JavaLocalDate): LocalDate {
        return LocalDate(date.year, date.monthValue, date.dayOfMonth)
    }
    
    /**
     * 将 kotlinx.datetime.LocalDate 转换为 java.time.LocalDate
     */
    fun toJavaDate(date: LocalDate): JavaLocalDate {
        return JavaLocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    }
    
    // ========== LocalDateTime 转换 ==========
    
    /**
     * 将 java.time.LocalDateTime 转换为 kotlinx.datetime.LocalDateTime
     */
    fun toKotlinDateTime(dateTime: JavaLocalDateTime): LocalDateTime {
        return LocalDateTime(
            dateTime.year,
            dateTime.monthValue,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
            dateTime.nano
        )
    }
    
    /**
     * 将 kotlinx.datetime.LocalDateTime 转换为 java.time.LocalDateTime
     */
    fun toJavaDateTime(dateTime: LocalDateTime): JavaLocalDateTime {
        return JavaLocalDateTime.of(
            dateTime.year,
            dateTime.monthNumber,
            dateTime.dayOfMonth,
            dateTime.hour,
            dateTime.minute,
            dateTime.second,
            dateTime.nanosecond
        )
    }
    
    // ========== Instant 转换 ==========
    
    /**
     * 将 java.time.Instant 转换为 kotlinx.datetime.Instant
     */
    fun toKotlinInstant(instant: JavaInstant): Instant {
        return Instant.fromEpochMilliseconds(instant.toEpochMilli())
    }
    
    /**
     * 将 kotlinx.datetime.Instant 转换为 java.time.Instant
     */
    fun toJavaInstant(instant: Instant): JavaInstant {
        return JavaInstant.ofEpochMilli(instant.toEpochMilliseconds())
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 将 kotlinx.datetime.LocalDate 转换为时间戳（毫秒）
     * 使用系统默认时区的当天开始时间
     */
    fun kotlinDateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
    
    /**
     * 将 java.time.LocalDate 转换为时间戳（毫秒）
     * 使用系统默认时区的当天开始时间
     */
    fun javaDateToTimestamp(date: JavaLocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    
    /**
     * 计算两个时间字符串之间的小时数
     * @param startTime 格式如 "09:00"
     * @param endTime 格式如 "18:00"
     * @return 小时数，如 9.0
     * 
     * 特殊情况：
     * - 相同时间（如 "00:00" 到 "00:00"）表示24小时
     */
    fun calculateHours(startTime: String, endTime: String): Double {
        val start = parseTime(startTime)
        val end = parseTime(endTime)
        
        var hours = end.first - start.first
        var minutes = end.second - start.second
        
        // 处理分钟为负数的情况
        if (minutes < 0) {
            hours -= 1
            minutes += 60
        }
        
        // 处理跨天情况
        if (hours < 0) {
            hours += 24
        }
        
        val totalHours = hours + minutes / 60.0
        
        // 处理相同时间表示24小时的情况
        if (totalHours == 0.0 && startTime == endTime) {
            return 24.0
        }
        
        return totalHours
    }
    
    /**
     * 解析时间字符串
     * @param time 格式如 "09:00" 或 "9:00"
     * @return Pair<小时, 分钟>
     */
    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        return if (parts.size == 2) {
            val hour = parts[0].toIntOrNull() ?: 0
            val minute = parts[1].toIntOrNull() ?: 0
            Pair(hour, minute)
        } else {
            Pair(0, 0)
        }
    }
}