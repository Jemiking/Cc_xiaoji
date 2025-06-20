package com.ccxiaoji.shared.notification.data.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ccxiaoji.shared.notification.domain.model.NotificationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: NotificationConfig
) {
    companion object {
        // 通知渠道ID
        const val CHANNEL_TASK_REMINDER = "task_reminder"
        const val CHANNEL_HABIT_REMINDER = "habit_reminder"
        const val CHANNEL_BUDGET_ALERT = "budget_alert"
        const val CHANNEL_CREDIT_CARD_REMINDER = "credit_card_reminder"
        const val CHANNEL_SCHEDULE_REMINDER = "schedule_reminder"
        const val CHANNEL_GENERAL = "general"
        
        // 通知ID范围
        const val NOTIFICATION_ID_TASK_BASE = 1000
        const val NOTIFICATION_ID_HABIT_BASE = 2000
        const val NOTIFICATION_ID_BUDGET_BASE = 3000
        const val NOTIFICATION_ID_CREDIT_CARD_BASE = 5000
        const val NOTIFICATION_ID_SCHEDULE_BASE = 6000
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
            
            // 信用卡还款提醒渠道
            val creditCardChannel = NotificationChannel(
                CHANNEL_CREDIT_CARD_REMINDER,
                "信用卡还款提醒",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "信用卡还款到期提醒"
                enableLights(true)
                enableVibration(true)
            }
            
            // 排班提醒渠道
            val scheduleChannel = NotificationChannel(
                CHANNEL_SCHEDULE_REMINDER,
                "排班提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日排班提醒通知"
                enableLights(true)
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
                listOf(taskChannel, habitChannel, budgetChannel, creditCardChannel, scheduleChannel, generalChannel)
            )
        }
    }
    
    // 发送任务提醒通知
    fun sendTaskReminder(taskId: String, taskTitle: String, dueTime: String) {
        val intent = Intent(context, config.mainActivityClass).apply {
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
            .setSmallIcon(config.smallIconResourceId)
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
        val intent = Intent(context, config.mainActivityClass).apply {
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
            .setSmallIcon(config.smallIconResourceId)
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
        val intent = Intent(context, config.mainActivityClass).apply {
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
            .setSmallIcon(config.smallIconResourceId)
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
    
    // 发送信用卡还款提醒
    fun sendCreditCardReminder(
        cardId: String,
        cardName: String,
        debtAmount: String,
        daysUntilDue: Int,
        paymentDueDay: Int
    ) {
        val intent = Intent(context, config.mainActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "account")
            putExtra("accountId", cardId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            cardId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, message) = when (daysUntilDue) {
            0 -> "信用卡还款日" to "「$cardName」今天是还款日，需还款 $debtAmount"
            1 -> "信用卡还款提醒" to "「$cardName」明天（${paymentDueDay}日）是还款日，需还款 $debtAmount"
            3 -> "信用卡还款提醒" to "「$cardName」还有3天（${paymentDueDay}日）到还款日，需还款 $debtAmount"
            else -> "信用卡还款提醒" to "「$cardName」还有${daysUntilDue}天到还款日，需还款 $debtAmount"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_CREDIT_CARD_REMINDER)
            .setSmallIcon(config.smallIconResourceId)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_CREDIT_CARD_BASE + cardId.hashCode(), notification)
        }
    }
    
    // 发送排班提醒通知
    fun sendScheduleReminder(hasSchedule: Boolean, shiftName: String? = null, shiftTime: String? = null) {
        val intent = Intent(context, config.mainActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigation", "schedule")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SCHEDULE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val (title, content) = if (hasSchedule && shiftName != null && shiftTime != null) {
            "今日排班提醒" to "今天的班次：$shiftName（$shiftTime）"
        } else {
            "今日排班提醒" to "今天没有排班安排"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_SCHEDULE_REMINDER)
            .setSmallIcon(config.smallIconResourceId)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_SCHEDULE_BASE, notification)
        }
    }
    
    // 发送通用通知
    fun sendGeneralNotification(title: String, message: String, notificationId: Int = NOTIFICATION_ID_GENERAL_BASE) {
        val intent = Intent(context, config.mainActivityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_GENERAL)
            .setSmallIcon(config.smallIconResourceId)
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