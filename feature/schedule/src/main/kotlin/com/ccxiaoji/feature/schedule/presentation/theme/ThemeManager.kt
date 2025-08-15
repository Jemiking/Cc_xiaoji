package com.ccxiaoji.feature.schedule.presentation.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

// 扩展属性，用于创建 DataStore 实例
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

/**
 * 主题管理器
 * 负责管理应用的主题设置和其他偏好设置
 */
@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        private val WEEK_START_DAY_KEY = intPreferencesKey("week_start_day")
    }
    
    /**
     * 获取当前主题模式
     * true - 深色模式
     * false - 浅色模式
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false
        }
    
    /**
     * 切换主题模式
     */
    suspend fun toggleDarkMode() {
        context.dataStore.edit { preferences ->
            val currentMode = preferences[IS_DARK_MODE_KEY] ?: false
            preferences[IS_DARK_MODE_KEY] = !currentMode
        }
    }
    
    /**
     * 设置主题模式
     */
    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDarkMode
        }
    }
    
    /**
     * 获取一周的开始日期
     * 1 - 周一（默认）
     * 7 - 周日
     */
    val weekStartDay: Flow<DayOfWeek> = context.dataStore.data
        .map { preferences ->
            val startDayValue = preferences[WEEK_START_DAY_KEY] ?: 1
            when (startDayValue) {
                7 -> DayOfWeek.SUNDAY
                else -> DayOfWeek.MONDAY
            }
        }
    
    /**
     * 设置一周的开始日期
     */
    suspend fun setWeekStartDay(dayOfWeek: DayOfWeek) {
        context.dataStore.edit { preferences ->
            val value = when (dayOfWeek) {
                DayOfWeek.SUNDAY -> 7
                else -> 1 // 默认周一
            }
            preferences[WEEK_START_DAY_KEY] = value
        }
    }
}