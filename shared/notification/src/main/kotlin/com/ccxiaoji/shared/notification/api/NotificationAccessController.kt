package com.ccxiaoji.shared.notification.api

import kotlinx.coroutines.flow.Flow

/**
 * 通知使用权与监听服务控制器
 * - 提供一键重连通知监听服务
 * - 打开系统通知使用权设置页
 * - 暴露监听健康统计（透传 NotificationEventRepository 的诊断信息）
 */
interface NotificationAccessController {
    /** 请求重连监听服务（带节流，可能异步） */
    fun requestRebind(): Boolean

    /** 打开系统“通知使用权”设置页 */
    fun openNotificationAccessSettings(): Boolean

    /** 打开某个通知渠道的系统设置页 */
    fun openChannelSettings(channelId: String): Boolean

    /** 监听层诊断信息（含连接健康） */
    fun diagnostics(): Flow<com.ccxiaoji.shared.notification.api.NotificationEventRepository.Diagnostics>
}
