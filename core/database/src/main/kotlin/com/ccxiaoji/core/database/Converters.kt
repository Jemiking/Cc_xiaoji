package com.ccxiaoji.core.database

import androidx.room.TypeConverter
import com.ccxiaoji.common.model.CategoryType
import com.ccxiaoji.common.model.RecurringFrequency
import com.ccxiaoji.common.model.SyncStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.datetime.Instant

class Converters {
    private val gson = Gson()
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
    
    // CategoryType converters
    @TypeConverter
    fun fromCategoryType(type: CategoryType?): String? {
        return type?.name
    }
    
    @TypeConverter
    fun toCategoryType(typeString: String?): CategoryType? {
        return typeString?.let { CategoryType.valueOf(it) }
    }
    
    // Instant converters for kotlinx.datetime
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }
    
    @TypeConverter
    fun toInstant(timestamp: Long?): Instant? {
        return timestamp?.let { Instant.fromEpochMilliseconds(it) }
    }
    
    // String List converters
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }
    
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(it, listType)
        }
    }
}