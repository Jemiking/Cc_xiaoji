package com.ccxiaoji.shared.notification.domain.model

import android.os.Bundle

/**
 * 原始通知事件模型
 * 
 * 这个模型封装了从系统通知中提取的原始信息，
 * 不包含业务语义，保持模块间的解耦
 */
data class RawNotificationEvent(
    /**
     * 发送通知的应用包名
     */
    val packageName: String,
    
    /**
     * 通知标题
     */
    val title: String?,
    
    /**
     * 通知文本内容
     */
    val text: String?,
    
    /**
     * 通知的额外信息Bundle
     */
    val extras: Bundle?,
    
    /**
     * 通知发布时间（毫秒时间戳）
     */
    val postTime: Long,
    
    /**
     * 通知唯一标识（用于去重）
     */
    val notificationKey: String? = null,
    
    /**
     * 通知组信息（用于处理群组通知）
     */
    val groupKey: String? = null,
    
    /**
     * 是否为群组摘要通知
     */
    val isGroupSummary: Boolean = false
)