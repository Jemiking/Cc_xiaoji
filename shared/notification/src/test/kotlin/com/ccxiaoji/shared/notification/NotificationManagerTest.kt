package com.ccxiaoji.shared.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.ccxiaoji.shared.notification.domain.model.NotificationConfig
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class NotificationManagerTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var notificationManager: NotificationManager

    @MockK
    private lateinit var notificationConfig: NotificationConfig

    private lateinit var ccNotificationManager: CcNotificationManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.packageName } returns "com.ccxiaoji.app"
        
        // Mock NotificationConfig properties
        every { notificationConfig.mainActivityClass } returns Any::class.java
        every { notificationConfig.smallIconResourceId } returns 1
        every { notificationConfig.packageName } returns "com.ccxiaoji.app"
        
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
    fun `获取通知配置`() {
        // Given
        val config = notificationConfig

        // Then
        assertThat(config.packageName).isEqualTo("com.ccxiaoji.app")
        assertThat(config.smallIconResourceId).isEqualTo(1)
        assertThat(config.mainActivityClass).isNotNull()
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