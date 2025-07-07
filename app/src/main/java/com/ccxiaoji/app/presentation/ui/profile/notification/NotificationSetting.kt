package com.ccxiaoji.app.presentation.ui.profile.notification

/**
 * 通知设置数据类
 */
data class NotificationSetting(
    val title: String,
    val description: String? = null,
    val value: String? = null,
    val enabled: Boolean,
    val onToggle: ((Boolean) -> Unit)? = null,
    val onClick: (() -> Unit)? = null
)