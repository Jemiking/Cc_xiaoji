package com.ccxiaoji.feature.schedule.notification

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Singleton
class ScheduleNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PREF_NAME = "schedule_notification_prefs"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_TIME = "notification_time"
        private const val DEFAULT_NOTIFICATION_TIME = "08:00"
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * 设置每日提醒
     * @param enabled 是否启用
     * @param time 提醒时间，格式 "HH:mm"
     */
    fun scheduleDailyReminder(enabled: Boolean, time: String = DEFAULT_NOTIFICATION_TIME) {
        // 保存设置
        sharedPreferences.edit().apply {
            putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
            putString(KEY_NOTIFICATION_TIME, time)
            apply()
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
    fun getNotificationSettings(): Pair<Boolean, String> {
        val enabled = sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, false)
        val time = sharedPreferences.getString(KEY_NOTIFICATION_TIME, DEFAULT_NOTIFICATION_TIME) ?: DEFAULT_NOTIFICATION_TIME
        return enabled to time
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