package com.ccxiaoji.shared.notification.data

import android.util.Log
import com.ccxiaoji.shared.notification.api.NotificationEventRepository
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通知事件仓库实现
 * 
 * 使用SharedFlow实现事件发布和订阅机制，
 * 支持多个消费者同时订阅通知事件流。
 */
@Singleton
class NotificationEventRepositoryImpl @Inject constructor() : NotificationEventRepository {
    
    companion object {
        private const val TAG = "AutoLedger_EventFlow"
    }
    
    // 使用SupervisorJob确保异常不会影响整个scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // 通知监听服务连接状态
    private val isConnected = AtomicBoolean(false)

    // 诊断统计
    private val totalEmitted = AtomicInteger(0)
    private val emittedByKeywords = AtomicInteger(0)
    private val emittedWithoutKeywords = AtomicInteger(0)
    private val skippedGroupSummary = AtomicInteger(0)
    private val skippedUnsupportedPackage = AtomicInteger(0)
    private val skippedNoKeywordsByConfig = AtomicInteger(0)

    // 连接健康
    private val connectCount = AtomicInteger(0)
    private val disconnectCount = AtomicInteger(0)
    private var lastStatusChangeAt: Long = System.currentTimeMillis()
    private var lastConnectedAt: Long = 0L
    private var totalConnectedMs: Long = 0L

    private val _diagnostics = MutableStateFlow(
        NotificationEventRepository.Diagnostics(
            totalEmitted = 0,
            emittedByKeywords = 0,
            emittedWithoutKeywords = 0,
            skippedGroupSummary = 0,
            skippedUnsupportedPackage = 0,
            skippedNoKeywordsByConfig = 0,
            isConnected = false,
            connectCount = 0,
            disconnectCount = 0,
            totalConnectedMs = 0,
            lastStatusChangeAt = lastStatusChangeAt
        )
    )
    
    /**
     * 通知事件流
     * 
     * 使用MutableSharedFlow配置：
     * - replay = 0: 不重放历史事件
     * - extraBufferCapacity = 64: 额外缓冲区，避免突发通知阻塞
     * - onBufferOverflow = DROP_OLDEST: 缓冲区满时丢弃最旧事件
     */
    private val _events = MutableSharedFlow<RawNotificationEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    
    override val events: Flow<RawNotificationEvent> = _events.asSharedFlow()
    
    init {
        Log.i(TAG, "🔄 NotificationEventRepository 初始化完成")
        Log.d(TAG, "📊 SharedFlow配置: replay=0, buffer=64, overflow=DROP_OLDEST")
    }
    
    /**
     * 发送通知事件
     * 
     * 此方法设计为非阻塞，即使在高频通知场景下也不会阻塞
     * NotificationListenerService的回调线程。
     */
    override suspend fun emit(event: RawNotificationEvent) {
        Log.d(TAG, "📤 接收到事件发送请求: package=${event.packageName}, key=${event.notificationKey}")
        
        // tryEmit是非阻塞的，失败时返回false但不会抛异常
        val success = _events.tryEmit(event)
        
        if (success) {
            Log.i(TAG, "✅ 事件发送成功到SharedFlow")
            Log.d(TAG, "📋 事件详情: title='${event.title}', text='${event.text}', time=${event.postTime}")
        } else {
            Log.e(TAG, "❌ 事件发送失败! 可能是缓冲区满 (当前容量: 64)")
            Log.w(TAG, "⚠️ 丢失事件: ${event.notificationKey}")
        }
        
        // 记录当前事件流状态
        Log.d(TAG, "🔗 SharedFlow订阅者数量: ${_events.subscriptionCount.value}")
    }
    
    override fun isListenerConnected(): Boolean {
        val connected = isConnected.get()
        Log.v(TAG, "🔍 查询连接状态: $connected")
        return connected
    }
    
    override fun updateConnectionStatus(isConnected: Boolean) {
        val oldStatus = this.isConnected.get()
        this.isConnected.set(isConnected)
        Log.i(TAG, "🔄 更新连接状态: $oldStatus -> $isConnected")
        val now = System.currentTimeMillis()
        if (!oldStatus && isConnected) {
            connectCount.incrementAndGet()
            lastConnectedAt = now
        } else if (oldStatus && !isConnected) {
            disconnectCount.incrementAndGet()
            if (lastConnectedAt > 0) totalConnectedMs += (now - lastConnectedAt)
            lastConnectedAt = 0L
        }
        lastStatusChangeAt = now

        if (isConnected) {
            Log.i(TAG, "🟢 通知监听服务已连接到事件流")
        } else {
            Log.w(TAG, "🔴 通知监听服务已断开事件流")
        }
        publishDiagnostics()
    }

    override fun diagnostics(): Flow<NotificationEventRepository.Diagnostics> = _diagnostics.asStateFlow()

    override fun recordEmitted(byKeywords: Boolean) {
        totalEmitted.incrementAndGet()
        if (byKeywords) emittedByKeywords.incrementAndGet() else emittedWithoutKeywords.incrementAndGet()
        publishDiagnostics()
    }

    override fun recordSkippedGroupSummary() {
        skippedGroupSummary.incrementAndGet()
        publishDiagnostics()
    }

    override fun recordSkippedUnsupportedPackage() {
        skippedUnsupportedPackage.incrementAndGet()
        publishDiagnostics()
    }

    override fun recordSkippedNoKeywordsByConfig() {
        skippedNoKeywordsByConfig.incrementAndGet()
        publishDiagnostics()
    }

    private fun publishDiagnostics() {
        _diagnostics.value = NotificationEventRepository.Diagnostics(
            totalEmitted = totalEmitted.get(),
            emittedByKeywords = emittedByKeywords.get(),
            emittedWithoutKeywords = emittedWithoutKeywords.get(),
            skippedGroupSummary = skippedGroupSummary.get(),
            skippedUnsupportedPackage = skippedUnsupportedPackage.get(),
            skippedNoKeywordsByConfig = skippedNoKeywordsByConfig.get(),
            isConnected = isConnected.get(),
            connectCount = connectCount.get(),
            disconnectCount = disconnectCount.get(),
            totalConnectedMs = totalConnectedMs + (if (isConnected.get() && lastConnectedAt > 0) (System.currentTimeMillis() - lastConnectedAt) else 0),
            lastStatusChangeAt = lastStatusChangeAt
        )
    }
}
