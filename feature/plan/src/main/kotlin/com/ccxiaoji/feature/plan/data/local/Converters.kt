package com.ccxiaoji.feature.plan.data.local

import androidx.room.TypeConverter
import com.ccxiaoji.feature.plan.domain.model.ReminderSettings
import com.ccxiaoji.feature.plan.domain.model.ReminderType
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalTime

/**
 * Room数据库类型转换器
 */
class Converters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * 时间戳转Instant
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it) }
    }
    
    /**
     * Instant转时间戳
     */
    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }
    
    /**
     * 字符串列表转JSON
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    /**
     * JSON转字符串列表
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 提醒设置转JSON
     */
    @TypeConverter
    fun fromReminderSettings(settings: ReminderSettings?): String? {
        return settings?.let {
            json.encodeToString(ReminderSettingsJson(
                enabled = it.enabled,
                reminderTimeHour = it.reminderTime.hour,
                reminderTimeMinute = it.reminderTime.minute,
                reminderDaysBefore = it.reminderDaysBefore,
                reminderType = it.reminderType.name
            ))
        }
    }
    
    /**
     * JSON转提醒设置
     */
    @TypeConverter
    fun toReminderSettings(value: String?): ReminderSettings? {
        return value?.let {
            try {
                val settingsJson = json.decodeFromString<ReminderSettingsJson>(it)
                ReminderSettings(
                    enabled = settingsJson.enabled,
                    reminderTime = LocalTime.of(settingsJson.reminderTimeHour, settingsJson.reminderTimeMinute),
                    reminderDaysBefore = settingsJson.reminderDaysBefore,
                    reminderType = ReminderType.valueOf(settingsJson.reminderType)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * 提醒设置JSON结构
 */
@kotlinx.serialization.Serializable
private data class ReminderSettingsJson(
    val enabled: Boolean,
    val reminderTimeHour: Int,
    val reminderTimeMinute: Int,
    val reminderDaysBefore: Int,
    val reminderType: String
)