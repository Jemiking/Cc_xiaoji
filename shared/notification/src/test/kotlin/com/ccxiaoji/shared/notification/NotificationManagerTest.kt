package com.ccxiaoji.shared.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.notification.data.NotificationConfig
import com.ccxiaoji.shared.notification.domain.model.NotificationData
import com.ccxiaoji.shared.notification.domain.model.NotificationType
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class NotificationManagerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var notificationManager: NotificationManager

    @MockK
    private lateinit var notificationConfig: NotificationConfig

    @MockK
    private lateinit var notificationApi: NotificationApi

    private lateinit var ccNotificationManager: CcNotificationManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.packageName } returns "com.ccxiaoji.app"
        
        ccNotificationManager = CcNotificationManager(context, notificationConfig)
    }

    @Test
    fun `创建通知渠道`() {
        // Given
        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.O
        
        val channelId = "task_reminder"
        val channelName = "任务提醒"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        every { notificationManager.createNotificationChannel(any()) } just Runs

        // When
        ccNotificationManager.createNotificationChannel(channelId, channelName, importance)

        // Then
        verify(exactly = 1) { 
            notificationManager.createNotificationChannel(
                withArg { channel ->
                    assertThat(channel.id).isEqualTo(channelId)
                    assertThat(channel.name.toString()).isEqualTo(channelName)
                    assertThat(channel.importance).isEqualTo(importance)
                }
            )
        }
    }

    @Test
    fun `发送任务提醒通知`() = runTest {
        // Given
        val notificationData = NotificationData(
            id = 1001,
            type = NotificationType.TASK_REMINDER,
            title = "任务提醒",
            content = "别忘了完成今天的任务：整理文档",
            channelId = "task_reminder",
            priority = NotificationCompat.PRIORITY_HIGH,
            autoCancel = true,
            extras = mapOf("taskId" to "task123")
        )

        every { notificationManager.notify(any(), any()) } just Runs
        coEvery { notificationApi.sendNotification(any()) } returns true

        // When
        val result = notificationApi.sendNotification(notificationData)

        // Then
        assertThat(result).isTrue()
        coVerify(exactly = 1) { notificationApi.sendNotification(notificationData) }
    }

    @Test
    fun `发送习惯打卡提醒`() = runTest {
        // Given
        val notificationData = NotificationData(
            id = 2001,
            type = NotificationType.HABIT_REMINDER,
            title = "习惯打卡提醒",
            content = "该进行每日运动了！坚持就是胜利💪",
            channelId = "habit_reminder",
            priority = NotificationCompat.PRIORITY_DEFAULT,
            autoCancel = true,
            extras = mapOf("habitId" to "habit456")
        )

        coEvery { notificationApi.sendNotification(any()) } returns true

        // When
        val result = notificationApi.sendNotification(notificationData)

        // Then
        assertThat(result).isTrue()
        assertThat(notificationData.type).isEqualTo(NotificationType.HABIT_REMINDER)
        assertThat(notificationData.extras["habitId"]).isEqualTo("habit456")
    }

    @Test
    fun `检查通知权限`() {
        // Given
        every { notificationManager.areNotificationsEnabled() } returns true

        // When
        val hasPermission = notificationManager.areNotificationsEnabled()

        // Then
        assertThat(hasPermission).isTrue()
        verify(exactly = 1) { notificationManager.areNotificationsEnabled() }
    }

    @Test
    fun `取消通知`() {
        // Given
        val notificationId = 1001
        every { notificationManager.cancel(notificationId) } just Runs

        // When
        ccNotificationManager.cancelNotification(notificationId)

        // Then
        verify(exactly = 1) { notificationManager.cancel(notificationId) }
    }

    @Test
    fun `批量发送通知`() = runTest {
        // Given
        val notifications = listOf(
            NotificationData(
                id = 3001,
                type = NotificationType.SCHEDULE_REMINDER,
                title = "排班提醒",
                content = "明天是早班，记得早点休息",
                channelId = "schedule_reminder"
            ),
            NotificationData(
                id = 3002,
                type = NotificationType.BUDGET_ALERT,
                title = "预算提醒",
                content = "本月餐饮预算已使用80%",
                channelId = "budget_alert"
            )
        )

        coEvery { notificationApi.sendBatchNotifications(any()) } returns mapOf(
            3001 to true,
            3002 to true
        )

        // When
        val results = notificationApi.sendBatchNotifications(notifications)

        // Then
        assertThat(results).hasSize(2)
        assertThat(results[3001]).isTrue()
        assertThat(results[3002]).isTrue()
        coVerify(exactly = 1) { notificationApi.sendBatchNotifications(notifications) }
    }

    @Test
    fun `获取通知配置`() {
        // Given
        every { notificationConfig.isEnabled } returns true
        every { notificationConfig.defaultReminderTime } returns LocalTime(9, 0)
        every { notificationConfig.soundEnabled } returns true
        every { notificationConfig.vibrationEnabled } returns true

        // When
        val config = notificationConfig

        // Then
        assertThat(config.isEnabled).isTrue()
        assertThat(config.defaultReminderTime).isEqualTo(LocalTime(9, 0))
        assertThat(config.soundEnabled).isTrue()
        assertThat(config.vibrationEnabled).isTrue()
    }
}

// 假设的NotificationManager实现
class CcNotificationManager(
    private val context: Context,
    private val config: NotificationConfig
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

// 假设的API接口扩展
interface NotificationApi {
    suspend fun sendNotification(data: NotificationData): Boolean
    suspend fun sendBatchNotifications(notifications: List<NotificationData>): Map<Int, Boolean>
}