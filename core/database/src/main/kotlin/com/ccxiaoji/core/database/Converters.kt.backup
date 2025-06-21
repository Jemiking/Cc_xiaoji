package com.ccxiaoji.core.database

import androidx.room.TypeConverter
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.common.model.SyncStatus
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return SyncStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromRecurringFrequency(frequency: RecurringFrequency): String {
        return frequency.name
    }
    
    @TypeConverter
    fun toRecurringFrequency(frequency: String): RecurringFrequency {
        return RecurringFrequency.valueOf(frequency)
    }
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
    
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
    
    // Schedule module date converters (Long <-> LocalDate/LocalDateTime)
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it / 1000, 0, java.time.ZoneOffset.UTC).toLocalDate()
        }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.atStartOfDay()?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
    }
    
    @TypeConverter
    fun fromLocalDateTimeLong(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofEpochSecond(it / 1000, 0, java.time.ZoneOffset.UTC)
        }
    }
    
    @TypeConverter
    fun localDateTimeToTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.toInstant(java.time.ZoneOffset.UTC)?.toEpochMilli()
    }
}