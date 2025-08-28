package com.ccxiaoji.shared.notification.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log
import com.ccxiaoji.shared.notification.api.NotificationAccessController
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAccessControllerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: NotificationEventRepository
) : NotificationAccessController {

    companion object { private const val TAG = "AutoLedger_Notification" }

    // 简单节流：15秒内不重复发起 rebind
    @Volatile private var lastRebindAt: Long = 0

    override fun requestRebind(): Boolean {
        val cn = ComponentName(appContext, PaymentNotificationListener::class.java)
        val granted = isAccessGranted(cn)
        if (!granted) {
            Log.w(TAG, "❗ 权限未授予，无法重连。请前往系统设置开启通知使用权")
            return false
        }
        return try {
            val now = System.currentTimeMillis()
            if (now - lastRebindAt < 15_000) {
                Log.i(TAG, "⏳ 已在15秒内请求过重连，跳过本次")
                return true
            }
            lastRebindAt = now
            Log.i(TAG, "🔁 请求重连通知监听服务（已授权）")
            // 轻推组件状态，促使系统重新绑定（不杀进程）
            val pm = appContext.packageManager
            pm.setComponentEnabledSetting(
                cn,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                cn,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            NotificationListenerService.requestRebind(cn)
            true
        } catch (e: Exception) {
            Log.e(TAG, "重连请求失败: ${e.message}", e)
            false
        }
    }

    override fun openNotificationAccessSettings(): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
            Log.i(TAG, "🧭 已打开系统通知使用权设置页")
            true
        } catch (e: Exception) {
            Log.e(TAG, "打开通知使用权设置失败: ${e.message}", e)
            false
        }
    }

    override fun openChannelSettings(channelId: String): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
            }
            appContext.startActivity(intent)
            Log.i(TAG, "🧭 已打开通知渠道设置页: $channelId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "打开通知渠道设置失败: ${e.message}", e)
            false
        }
    }

    override fun diagnostics() = repository.diagnostics()

    private fun isAccessGranted(cn: ComponentName): Boolean {
        return try {
            val flat = Settings.Secure.getString(appContext.contentResolver, "enabled_notification_listeners")
            val granted = flat?.split(":")?.any { it.equals(cn.flattenToString(), ignoreCase = true) || it.contains(appContext.packageName) } == true
            Log.v(TAG, "🔍 通知使用权授权检测: $granted")
            granted
        } catch (e: Exception) {
            Log.w(TAG, "读取通知使用权授权状态失败: ${e.message}")
            false
        }
    }
}
