package com.ccxiaoji.feature.schedule.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ccxiaoji.feature.schedule.domain.usecase.GetScheduleByDateUseCase
import com.ccxiaoji.feature.schedule.domain.usecase.GetShiftsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class ScheduleNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val getScheduleByDateUseCase: GetScheduleByDateUseCase,
    private val getShiftsUseCase: GetShiftsUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "schedule_reminder_channel"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "schedule_daily_reminder"
    }

    override suspend fun doWork(): Result {
        return try {
            // 获取今天的日期
            val today = LocalDate.now()
            
            // 获取今天的排班信息
            val schedule = getScheduleByDateUseCase(today).first()
            
            if (schedule != null) {
                // 直接使用 schedule 中的 shift 对象
                val shift = schedule.shift
                
                if (shift != null) {
                    // 发送通知
                    sendNotification(
                        title = "今日排班提醒",
                        content = "今天的班次：${shift.name}（${shift.startTime} - ${shift.endTime}）"
                    )
                }
            } else {
                // 今天没有排班
                sendNotification(
                    title = "今日排班提醒",
                    content = "今天没有排班安排"
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun sendNotification(title: String, content: String) {
        createNotificationChannel()
        
        // TODO: 需要从应用模块获取MainActivity的引用
        // 暂时使用包名和类名的字符串形式
        val intent = Intent().apply {
            setClassName(applicationContext.packageName, "com.ccxiaoji.app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(applicationContext)) {
            try {
                notify(NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // 处理通知权限被拒绝的情况
                e.printStackTrace()
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "排班提醒"
            val descriptionText = "每日排班提醒通知"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}