package com.ccxiaoji.shared.notification.api

import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import kotlinx.coroutines.flow.Flow

/**
 * 通知事件仓库接口
 * 
 * 提供系统通知事件的数据流，用于跨模块通信。
 * shared/notification 模块负责监听系统通知，
 * feature/ledger 模块负责处理支付相关的业务逻辑。
 */
interface NotificationEventRepository {
    
    /**
     * 系统通知事件流
     * 
     * 提供持续的通知事件流，消费者可以订阅此流来接收通知事件。
     * 使用SharedFlow实现，支持多个订阅者同时消费事件。
     */
    val events: Flow<RawNotificationEvent>
    
    /**
     * 发送通知事件到流中
     * 
     * 此方法由NotificationListenerService调用，
     * 将系统通知转换为RawNotificationEvent并发布到事件流。
     * 
     * @param event 原始通知事件
     */
    suspend fun emit(event: RawNotificationEvent)
    
    /**
     * 获取通知监听服务的连接状态
     */
    fun isListenerConnected(): Boolean
    
    /**
     * 更新监听服务连接状态
     */
    fun updateConnectionStatus(isConnected: Boolean)

    /**
     * 监听层诊断统计（透传与跳过计数）。
     */
    data class Diagnostics(
        val totalEmitted: Int,
        val emittedByKeywords: Int,
        val emittedWithoutKeywords: Int,
        val skippedGroupSummary: Int,
        val skippedUnsupportedPackage: Int,
        val skippedNoKeywordsByConfig: Int,
        // 监听健康
        val isConnected: Boolean,
        val connectCount: Int,
        val disconnectCount: Int,
        val totalConnectedMs: Long,
        val lastStatusChangeAt: Long
    )

    /**
     * 获取诊断统计流。
     */
    fun diagnostics(): kotlinx.coroutines.flow.Flow<Diagnostics>

    /**
     * 记录一次透传事件。
     * @param byKeywords 是否因关键词命中而透传
     */
    fun recordEmitted(byKeywords: Boolean)

    /** 记录由于群组摘要而跳过。 */
    fun recordSkippedGroupSummary()

    /** 记录由于不支持的包而跳过。 */
    fun recordSkippedUnsupportedPackage()

    /** 记录由于配置不允许无关键词透传而跳过。 */
    fun recordSkippedNoKeywordsByConfig()
}
