package com.ccxiaoji.core.common.extensions

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * 日期相关扩展函数
 */

/**
 * 将Date转换为格式化的字符串
 */
fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.CHINA).format(this)
}

/**
 * 将LocalDate转换为格式化的字符串
 */
fun LocalDate.format(pattern: String = "yyyy-MM-dd"): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * 将LocalDateTime转换为格式化的字符串
 */
fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}

/**
 * 将Date转换为LocalDate
 */
fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
}

/**
 * 将Date转换为LocalDateTime
 */
fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

/**
 * 将LocalDate转换为Date
 */
fun LocalDate.toDate(): Date {
    return Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

/**
 * 将LocalDateTime转换为Date
 */
fun LocalDateTime.toDate(): Date {
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}