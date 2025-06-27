package com.ccxiaoji.shared.notification.domain.model

/**
 * 通知配置
 */
data class NotificationConfig(
    val mainActivityClass: Class<*>,
    val smallIconResourceId: Int,
    val packageName: String
)