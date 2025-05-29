package com.ccxiaoji.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ccxiaoji.app.R
import com.ccxiaoji.app.presentation.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // 通知渠道ID
        const val CHANNEL_TASK_REMINDER = "task_reminder"
        const val CHANNEL_HABIT_REMINDER = "habit_reminder"
        const val CHANNEL_BUDGET_ALERT = "budget_alert"
        const val CHANNEL_GENERAL = "general"
        
        // 通知ID范围
        const val NOTIFICATION_ID_TASK_BASE = 1000
        const val NOTIFICATION_ID_HABIT_BASE = 2000
        const val NOTIFICATION_ID_BUDGET_BASE = 3000
        const val NOTIFICATION_ID_GENERAL_BASE = 4000
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // 任务提醒渠道
            val taskChannel = NotificationChannel(
                CHANNEL_TASK_REMINDER,
                "任务提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "任务到期提醒"
                enableLights(true)
                enableVibration(true)
            }
            
            // 习惯提醒渠道
            val habitChannel = NotificationChannel(
                CHANNEL_HABIT_REMINDER,
                "习惯打卡提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "习惯打卡提醒"
                enableLights(true)
            }
            
            // 预算提醒渠道
            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGET_ALERT,
                "预算提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "预算超支提醒"
                enableLights(true)
                enableVibration(true)
            }
            
            // 通用渠道
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                "通用通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "应用通用通知"
            }
            
            notificationManager.createNotificationChannels(
                listOf(taskChannel, habitChannel, budgetChannel, generalChannel)
            )
        }
    }
    
    // 发送任务提醒通知
    fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "todo")
            putExtra("taskId", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_TASK_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("任务提醒")
            .setContentText("「$taskTitle」将于 $dueTime 到期")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_TASK_BASE + taskId.hashCode(), notification)
        }
    }
    
    // 发送习惯打卡提醒
    fun sendHabitReminder(habitId: String, habitTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "habit")
            putExtra("habitId", habitId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_HABIT_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("习惯打卡提醒")
            .setContentText("别忘了打卡「$habitTitle」")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_HABIT_BASE + habitId.hashCode(), notification)
        }
    }
    
    // 发送预算超支提醒
    fun sendBudgetAlert(categoryName: String, percentage: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "ledger")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            categoryName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val message = when {
            percentage >= 100 -> "「$categoryName」预算已超支 ${percentage - 100}%"
            percentage >= 90 -> "「$categoryName」预算已使用 $percentage%，即将超支"
            else -> "「$categoryName」预算已使用 $percentage%"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ALERT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("预算提醒")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BUDGET_BASE + categoryName.hashCode(), notification)
        }
    }
    
    // 发送通用通知
    fun sendGeneralNotification(title: String, message: String, notificationId: Int = NOTIFICATION_ID_GENERAL_BASE) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
    }
    
    // 取消通知
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    // 取消所有通知
    fun cancelAllNotifications() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}