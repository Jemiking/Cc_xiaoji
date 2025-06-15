package com.ccxiaoji.core.common.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate as JavaLocalDate
import java.time.LocalDateTime as JavaLocalDateTime
import java.time.Instant as JavaInstant

/**
 * DateConverter 单元测试
 * 确保所有日期类型转换的正确性
 */
class DateConverterTest {
    
    @Test
    fun `test LocalDate conversion between java and kotlin`() {
        // 测试 Java to Kotlin
        val javaDate = JavaLocalDate.of(2025, 1, 14)
        val kotlinDate = DateConverter.toKotlinDate(javaDate)
        
        assertEquals(2025, kotlinDate.year)
        assertEquals(1, kotlinDate.monthNumber)
        assertEquals(14, kotlinDate.dayOfMonth)
        
        // 测试 Kotlin to Java
        val convertedBack = DateConverter.toJavaDate(kotlinDate)
        assertEquals(javaDate, convertedBack)
    }
    
    @Test
    fun `test LocalDateTime conversion between java and kotlin`() {
        // 测试 Java to Kotlin
        val javaDateTime = JavaLocalDateTime.of(2025, 1, 14, 15, 30, 45, 123456789)
        val kotlinDateTime = DateConverter.toKotlinDateTime(javaDateTime)
        
        assertEquals(2025, kotlinDateTime.year)
        assertEquals(1, kotlinDateTime.monthNumber)
        assertEquals(14, kotlinDateTime.dayOfMonth)
        assertEquals(15, kotlinDateTime.hour)
        assertEquals(30, kotlinDateTime.minute)
        assertEquals(45, kotlinDateTime.second)
        assertEquals(123456789, kotlinDateTime.nanosecond)
        
        // 测试 Kotlin to Java
        val convertedBack = DateConverter.toJavaDateTime(kotlinDateTime)
        assertEquals(javaDateTime, convertedBack)
    }
    
    @Test
    fun `test Instant conversion between java and kotlin`() {
        // 测试 Java to Kotlin
        val javaInstant = JavaInstant.ofEpochMilli(1736848800000L) // 2025-01-14 10:00:00 UTC
        val kotlinInstant = DateConverter.toKotlinInstant(javaInstant)
        
        assertEquals(1736848800000L, kotlinInstant.toEpochMilliseconds())
        
        // 测试 Kotlin to Java
        val convertedBack = DateConverter.toJavaInstant(kotlinInstant)
        assertEquals(javaInstant, convertedBack)
    }
    
    @Test
    fun `test date to timestamp conversion`() {
        // 测试 Kotlin LocalDate to timestamp
        val kotlinDate = LocalDate(2025, 1, 14)
        val kotlinTimestamp = DateConverter.kotlinDateToTimestamp(kotlinDate)
        assertTrue(kotlinTimestamp > 0)
        
        // 测试 Java LocalDate to timestamp
        val javaDate = JavaLocalDate.of(2025, 1, 14)
        val javaTimestamp = DateConverter.javaDateToTimestamp(javaDate)
        assertTrue(javaTimestamp > 0)
        
        // 两种方式应该产生相同的时间戳（同一天的开始时间）
        assertEquals(kotlinTimestamp, javaTimestamp)
    }
    
    @Test
    fun `test calculate hours between time strings`() {
        // 正常工作时间
        assertEquals(8.0, DateConverter.calculateHours("09:00", "17:00"), 0.01)
        assertEquals(8.5, DateConverter.calculateHours("09:00", "17:30"), 0.01)
        assertEquals(9.0, DateConverter.calculateHours("08:30", "17:30"), 0.01)
        
        // 跨午夜的情况
        assertEquals(8.0, DateConverter.calculateHours("22:00", "06:00"), 0.01)
        assertEquals(12.0, DateConverter.calculateHours("20:00", "08:00"), 0.01)
        
        // 24小时
        assertEquals(24.0, DateConverter.calculateHours("00:00", "00:00"), 0.01)
        
        // 带分钟的计算
        assertEquals(2.5, DateConverter.calculateHours("10:00", "12:30"), 0.01)
        assertEquals(2.25, DateConverter.calculateHours("10:15", "12:30"), 0.01)
        
        // 更多边界情况
        assertEquals(0.5, DateConverter.calculateHours("10:00", "10:30"), 0.01)
        assertEquals(23.5, DateConverter.calculateHours("01:00", "00:30"), 0.01)
        assertEquals(1.0, DateConverter.calculateHours("23:30", "00:30"), 0.01)
        
        // 验证分钟为负数的处理
        assertEquals(0.5, DateConverter.calculateHours("10:30", "11:00"), 0.01)
        assertEquals(1.5, DateConverter.calculateHours("10:45", "12:15"), 0.01)
    }
    
    @Test
    fun `test edge cases for date conversion`() {
        // 测试月末日期
        val javaMonthEnd = JavaLocalDate.of(2025, 2, 28)
        val kotlinMonthEnd = DateConverter.toKotlinDate(javaMonthEnd)
        assertEquals(28, kotlinMonthEnd.dayOfMonth)
        
        // 测试闰年
        val javaLeapYear = JavaLocalDate.of(2024, 2, 29)
        val kotlinLeapYear = DateConverter.toKotlinDate(javaLeapYear)
        assertEquals(29, kotlinLeapYear.dayOfMonth)
        
        // 测试年初
        val javaYearStart = JavaLocalDate.of(2025, 1, 1)
        val kotlinYearStart = DateConverter.toKotlinDate(javaYearStart)
        assertEquals(1, kotlinYearStart.dayOfMonth)
        assertEquals(1, kotlinYearStart.monthNumber)
        
        // 测试年末
        val javaYearEnd = JavaLocalDate.of(2024, 12, 31)
        val kotlinYearEnd = DateConverter.toKotlinDate(javaYearEnd)
        assertEquals(31, kotlinYearEnd.dayOfMonth)
        assertEquals(12, kotlinYearEnd.monthNumber)
    }
}