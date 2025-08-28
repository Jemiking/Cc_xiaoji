package com.ccxiaoji.shared.notification.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 支付通知监听服务
 * 
 * 继承自NotificationListenerService，监听系统通知并提取支付相关信息。
 * 使用Hilt进行依赖注入，确保与应用的其他组件良好集成。
 */
@AndroidEntryPoint
class PaymentNotificationListener : NotificationListenerService() {
    
    companion object {
        private const val TAG = "AutoLedger_Notification"
    }
    
    @Inject
    lateinit var notificationEventRepository: NotificationEventRepository

    // 可配置：是否在未命中关键词时也透传事件（默认开启）
    @Inject
    lateinit var dataStore: DataStore<Preferences>
    
    // 服务内协程作用域，使用SupervisorJob避免异常传播
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val EMIT_WITHOUT_KEYWORDS_KEY = booleanPreferencesKey("auto_ledger_emit_without_keywords")
    private val EMIT_GROUP_SUMMARY_KEY = booleanPreferencesKey("auto_ledger_emit_group_summary")
    // 是否记录未匹配(不支持包名)的噪声日志（默认false，减少刷屏）
    private val LOG_UNMATCHED_NOTIFICATIONS_KEY = booleanPreferencesKey("auto_ledger_log_unmatched")
    @Volatile private var emitWithoutKeywords: Boolean = true
    @Volatile private var emitGroupSummary: Boolean = false
    @Volatile private var logUnmatchedNotifications: Boolean = false
    
    /**
     * 支持的支付应用包名（极简MVP：仅支付宝）
     */
    private val supportedPackages = setOf(
        "com.eg.android.AlipayGphone" // 支付宝
    )
    
    /**
     * 支付相关关键词
     * 用于快速过滤非支付相关通知
     */
    private val paymentKeywords = setOf(
        "付款", "支付", "收款", "转账", "退款", "到账", "余额", 
        "成功", "失败", "红包", "零钱", "银行卡", "信用卡"
    )

    // 自动重连尝试计数
    private var rebindAttempts: Int = 0
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "🟢 通知监听服务已连接")
        notificationEventRepository.updateConnectionStatus(true)
        Log.d(TAG, "已更新连接状态为: true")
        // 重置重连尝试
        rebindAttempts = 0

        // 订阅 DataStore 配置，动态更新行为
        serviceScope.launch {
            try {
                dataStore.data.collect { prefs ->
                    val newEmitWithout = prefs[EMIT_WITHOUT_KEYWORDS_KEY] ?: true
                    val oldEmitWithout = emitWithoutKeywords
                    emitWithoutKeywords = newEmitWithout

                    val newEmitGroupSummary = prefs[EMIT_GROUP_SUMMARY_KEY] ?: false
                    val oldEmitGroupSummary = emitGroupSummary
                    emitGroupSummary = newEmitGroupSummary

                    if (newEmitWithout != oldEmitWithout) {
                        Log.i(TAG, "⚙️ 配置更新: emitWithoutKeywords=$newEmitWithout")
                    }
                    if (newEmitGroupSummary != oldEmitGroupSummary) {
                        Log.i(TAG, "⚙️ 配置更新: emitGroupSummary=$newEmitGroupSummary")
                    }

                    val newLogUnmatched = prefs[LOG_UNMATCHED_NOTIFICATIONS_KEY] ?: false
                    val oldLogUnmatched = logUnmatchedNotifications
                    logUnmatchedNotifications = newLogUnmatched
                    if (newLogUnmatched != oldLogUnmatched) {
                        Log.i(TAG, "⚙️ 配置更新: logUnmatchedNotifications=$newLogUnmatched")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "读取DataStore配置失败，使用默认值(true)", e)
                emitWithoutKeywords = true
                emitGroupSummary = false
                logUnmatchedNotifications = false
            }
        }
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "🔴 通知监听服务已断开连接")
        notificationEventRepository.updateConnectionStatus(false)
        Log.d(TAG, "已更新连接状态为: false")
        // 节流重连：最多3次，间隔15秒（仅在系统权限仍授予的情况下）
        val granted = try {
            val flat = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            val self = android.content.ComponentName(this, PaymentNotificationListener::class.java).flattenToString()
            flat?.split(":")?.any { it.equals(self, true) || it.contains(packageName) } == true
        } catch (_: Exception) { false }
        if (granted) {
            scheduleRebindIfNeeded()
        } else {
            Log.w(TAG, "❗ 检测到使用权被关闭，自动重连已跳过。请到系统设置重新授予")
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 快速过滤：只处理支持的应用包（避免无关APP刷屏）
        if (sbn.packageName !in supportedPackages) {
            if (logUnmatchedNotifications) {
                Log.v(TAG, "⚪ 跳过不支持的应用包: ${sbn.packageName}")
            }
            notificationEventRepository.recordSkippedUnsupportedPackage()
            return
        }
        // 仅对支持的包打印收到通知，降低噪声
        Log.d(TAG, "📱 收到通知: package=${sbn.packageName}, key=${sbn.key}")

        Log.i(TAG, "✅ 支持的应用包: ${sbn.packageName}")
        
        val notification = sbn.notification
        
        // 群组摘要：默认跳过，可配置透传
        if (isGroupSummaryNotification(notification)) {
            if (!emitGroupSummary) {
                Log.v(TAG, "⚪ 跳过群组摘要通知（可在设置中开启透传）")
                notificationEventRepository.recordSkippedGroupSummary()
                return
            } else {
                Log.i(TAG, "🟡 群组摘要通知按配置透传")
            }
        }
        
        // 提取通知内容
        val title = notification.extras?.getString(Notification.EXTRA_TITLE)
        val text = extractNotificationText(notification)
        
        Log.d(TAG, "📄 通知内容 - 标题: '$title', 文本: '$text'")
        
        // 关键词检测（仅用于日志与诊断，不再作为硬过滤条件）
        val hasPaymentKeywords = containsPaymentKeywords(title, text)
        Log.d(TAG, "🔍 支付关键词匹配: $hasPaymentKeywords")

        if (!hasPaymentKeywords && !emitWithoutKeywords) {
            Log.v(TAG, "⚪ 未命中关键词且配置不透传，跳过该通知")
            notificationEventRepository.recordSkippedNoKeywordsByConfig()
            return
        }

        if (hasPaymentKeywords) {
            Log.i(TAG, "🎯 关键词命中，将透传事件以供业务层处理")
        } else {
            Log.i(TAG, "🎯 关键词未命中，但按配置透传事件，以供业务层进一步判定")
        }
        
        // 创建原始通知事件
        val event = RawNotificationEvent(
            packageName = sbn.packageName,
            title = title,
            text = text,
            extras = notification.extras,
            postTime = sbn.postTime,
            notificationKey = sbn.key,
            groupKey = sbn.groupKey,
            isGroupSummary = isGroupSummaryNotification(notification)
        )
        
        Log.d(TAG, "📦 创建事件对象: key=${event.notificationKey}, postTime=${event.postTime}")
        
        // 在后台协程中发送事件，避免阻塞系统回调
        serviceScope.launch {
            // 记录诊断
            notificationEventRepository.recordEmitted(hasPaymentKeywords)
            Log.d(TAG, "🚀 开始发送事件到Repository...")
            notificationEventRepository.emit(event)
            Log.i(TAG, "✅ 事件发送完成")
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 通知移除时的处理（如需要的话）
        super.onNotificationRemoved(sbn)
    }
    
    /**
     * 提取通知文本内容
     * 
     * 尝试多种方式获取通知文本：
     * 1. EXTRA_TEXT - 主要文本
     * 2. EXTRA_BIG_TEXT - 展开文本
     * 3. EXTRA_SUB_TEXT - 子文本
     */
    private fun extractNotificationText(notification: Notification): String? {
        val extras = notification.extras ?: return null
        
        return extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
    }
    
    /**
     * 检查是否为群组摘要通知
     */
    private fun isGroupSummaryNotification(notification: Notification): Boolean {
        return (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
    }
    
    /**
     * 检查通知内容是否包含支付相关关键词
     */
    private fun containsPaymentKeywords(title: String?, text: String?): Boolean {
        val content = "${title.orEmpty()} ${text.orEmpty()}".lowercase()
        val matchedKeywords = paymentKeywords.filter { keyword -> content.contains(keyword) }
        
        if (matchedKeywords.isNotEmpty()) {
            Log.d(TAG, "🔍 匹配的支付关键词: $matchedKeywords")
            return true
        } else {
            Log.v(TAG, "🔍 未找到支付关键词，检查内容: '$content'")
            return false
        }
    }
    // —— 内部：重连节流 ——
    private fun scheduleRebindIfNeeded() {
        if (rebindAttempts >= 3) {
            Log.w(TAG, "🔁 重连次数已达上限，放弃自动重连")
            return
        }
        rebindAttempts += 1
        val delayMs = 15_000L
        serviceScope.launch {
            try {
                kotlinx.coroutines.delay(delayMs)
                Log.i(TAG, "🔁 第${rebindAttempts}次尝试重连通知监听服务")
                val cn = android.content.ComponentName(this@PaymentNotificationListener, PaymentNotificationListener::class.java)
                NotificationListenerService.requestRebind(cn)
            } catch (e: Exception) {
                Log.e(TAG, "重连调度失败: ${e.message}")
            }
        }
    }
}
