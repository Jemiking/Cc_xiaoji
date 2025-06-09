package com.ccxiaoji.core.common.utils

import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * 日期时间相关的扩展函数
 */

/**
 * 将LocalDateTime格式化为字符串
 * @param pattern 格式化模式，默认为"yyyy-MM-dd HH:mm:ss"
 * @return 格式化后的字符串
 */
fun LocalDateTime.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.toJavaLocalDateTime().format(formatter)
}

/**
 * 将LocalDate格式化为字符串
 * @param pattern 格式化模式，默认为"yyyy-MM-dd"
 * @return 格式化后的字符串
 */
fun LocalDate.format(pattern: String = "yyyy-MM-dd"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return this.toJavaLocalDate().format(formatter)
}

/**
 * 格式化为中文日期（如：2024年1月1日）
 */
fun LocalDate.formatChinese(): String {
    return "${year}年${monthNumber}月${dayOfMonth}日"
}

/**
 * 格式化为中文月份（如：1月）
 */
fun LocalDate.formatChineseMonth(): String {
    return "${monthNumber}月"
}

/**
 * 格式化为简短日期（如：1/1）
 */
fun LocalDate.formatShort(): String {
    return "$monthNumber/$dayOfMonth"
}

/**
 * 格式化为相对时间（今天、昨天、前天、x天前）
 */
fun LocalDate.formatRelative(): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val daysDiff = (today.toEpochDays() - this.toEpochDays()).toInt()
    
    return when (daysDiff) {
        0 -> "今天"
        1 -> "昨天"
        2 -> "前天"
        in 3..7 -> "${daysDiff}天前"
        in 8..30 -> "${daysDiff / 7}周前"
        in 31..365 -> "${daysDiff / 30}个月前"
        else -> if (daysDiff > 365) "${daysDiff / 365}年前" else format("yyyy-MM-dd")
    }
}

/**
 * 格式化为相对时间（刚刚、x分钟前、x小时前、昨天、x天前）
 */
fun LocalDateTime.formatRelative(): String {
    val now = Clock.System.now()
    val instant = this.toInstant(TimeZone.currentSystemDefault())
    val duration = now - instant
    
    return when {
        duration.inWholeMinutes < 1L -> "刚刚"
        duration.inWholeMinutes < 60L -> "${duration.inWholeMinutes}分钟前"
        duration.inWholeHours < 24L -> "${duration.inWholeHours}小时前"
        duration.inWholeDays == 1L -> "昨天"
        duration.inWholeDays < 7L -> "${duration.inWholeDays}天前"
        duration.inWholeDays < 30L -> "${duration.inWholeDays / 7}周前"
        duration.inWholeDays < 365L -> "${duration.inWholeDays / 30}个月前"
        else -> "${duration.inWholeDays / 365}年前"
    }
}

/**
 * 获取当月第一天
 */
fun LocalDate.firstDayOfMonth(): LocalDate {
    return LocalDate(year, month, 1)
}

/**
 * 获取当月最后一天
 */
fun LocalDate.lastDayOfMonth(): LocalDate {
    val daysInMonth = month.length(DateUtils.isLeapYear(year))
    return LocalDate(year, month, daysInMonth)
}

/**
 * 获取当年第一天
 */
fun LocalDate.firstDayOfYear(): LocalDate {
    return LocalDate(year, Month.JANUARY, 1)
}

/**
 * 获取当年最后一天
 */
fun LocalDate.lastDayOfYear(): LocalDate {
    return LocalDate(year, Month.DECEMBER, 31)
}

/**
 * 判断是否是今天
 */
fun LocalDate.isToday(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return this == today
}

/**
 * 判断是否是昨天
 */
fun LocalDate.isYesterday(): Boolean {
    val yesterday = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(1, DateTimeUnit.DAY)
    return this == yesterday
}

/**
 * 判断是否是本月
 */
fun LocalDate.isCurrentMonth(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return this.year == today.year && this.month == today.month
}

/**
 * 判断是否是本年
 */
fun LocalDate.isCurrentYear(): Boolean {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return this.year == today.year
}

/**
 * 获取两个日期之间的天数差
 */
fun LocalDate.daysUntil(other: LocalDate): Int {
    return (other.toEpochDays() - this.toEpochDays()).toInt()
}

/**
 * 获取两个日期之间的月数差
 */
fun LocalDate.monthsUntil(other: LocalDate): Int {
    return (other.year - this.year) * 12 + (other.monthNumber - this.monthNumber)
}