package com.ccxiaoji.feature.schedule.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Room数据库日期类型转换器
 * 用于在数据库存储和对象使用之间转换日期类型
 */
class DateConverter {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC).toLocalDate()
        }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.atStartOfDay()?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }
    
    @TypeConverter
    fun fromLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC)
        }
    }
    
    @TypeConverter
    fun localDateTimeToTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }
}