package com.ccxiaoji.feature.schedule.data.scheduler

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import com.ccxiaoji.feature.schedule.data.worker.ScheduleNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 排班通知调度器
 * 负责管理每日排班提醒的调度
 */
@Singleton
class ScheduleNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREFERENCES_NAME = "schedule_notification_preferences"
        private const val DEFAULT_NOTIFICATION_TIME = "08:00"
        
        // DataStore keys
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_NOTIFICATION_TIME = stringPreferencesKey("notification_time")
    }
    
    // 创建DataStore实例
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = PREFERENCES_NAME
    )
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * 设置每日提醒
     * @param enabled 是否启用
     * @param time 提醒时间，格式 "HH:mm"
     */
    suspend fun scheduleDailyReminder(enabled: Boolean, time: String = DEFAULT_NOTIFICATION_TIME) {
        // 保存设置到DataStore
        context.dataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
            preferences[KEY_NOTIFICATION_TIME] = time
        }
        
        if (enabled) {
            scheduleWork(time)
        } else {
            cancelWork()
        }
    }
    
    /**
     * 获取通知设置状态
     */
    fun getNotificationSettings(): Flow<Pair<Boolean, String>> {
        return context.dataStore.data.map { preferences ->
            val enabled = preferences[KEY_NOTIFICATION_ENABLED] ?: false
            val time = preferences[KEY_NOTIFICATION_TIME] ?: DEFAULT_NOTIFICATION_TIME
            enabled to time
        }
    }
    
    /**
     * 获取当前通知设置（同步方法）
     */
    suspend fun getCurrentNotificationSettings(): Pair<Boolean, String> {
        return getNotificationSettings().first()
    }
    
    private fun scheduleWork(timeString: String) {
        // 解析时间字符串
        val (hours, minutes) = timeString.split(":").map { it.toInt() }
        
        // 计算初始延迟
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        var targetDateTime = LocalDateTime.of(today, LocalTime.of(hours, minutes))
        
        // 如果今天的目标时间已过，则设置为明天
        if (targetDateTime <= now) {
            targetDateTime = targetDateTime.plusDays(1)
        }
        
        // 计算延迟时间（分钟）
        val initialDelay = ChronoUnit.MINUTES.between(now, targetDateTime)
        
        // 创建工作请求
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<ScheduleNotificationWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        
        // 使用唯一工作名称，确保只有一个活动的定期任务
        workManager.enqueueUniquePeriodicWork(
            ScheduleNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
    }
    
    private fun cancelWork() {
        workManager.cancelUniqueWork(ScheduleNotificationWorker.WORK_NAME)
    }
    
    /**
     * 立即发送测试通知
     */
    fun sendTestNotification() {
        val testWorkRequest = OneTimeWorkRequestBuilder<ScheduleNotificationWorker>()
            .build()
        
        workManager.enqueue(testWorkRequest)
    }
}