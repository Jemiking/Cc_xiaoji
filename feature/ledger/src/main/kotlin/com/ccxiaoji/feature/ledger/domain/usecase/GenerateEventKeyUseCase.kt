package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.shared.notification.domain.model.RawNotificationEvent
import java.security.MessageDigest
import javax.inject.Inject

/**
 * 生成事件指纹用例
 * 
 * 为通知事件生成唯一的指纹，用于去重和幂等性保证
 */
class GenerateEventKeyUseCase @Inject constructor() {
    
    /**
     * 为原始通知事件生成事件键
     * 
     * 基于包名、金额窗口、文本hash等生成唯一指纹
     */
    fun generateForRawEvent(event: RawNotificationEvent): String =
        generateForRawEvent(event, windowSec = 5 * 60)

    /**
     * 为原始事件生成指纹（可配置时间窗口）
     */
    fun generateForRawEvent(event: RawNotificationEvent, windowSec: Int): String {
        val text = "${event.title.orEmpty()} ${event.text.orEmpty()}"
        val textHash = text.hashCode().toString()
        val bucketMs = (windowSec.coerceAtLeast(1)) * 1000L
        val timeWindow = (event.postTime / bucketMs) * bucketMs

        val components = listOf(
            event.packageName,
            textHash,
            timeWindow.toString()
        )

        return generateHash(components.joinToString("|"))
    }
    
    /**
     * 为解析后的支付通知生成事件键
     * 
     * 包含更多业务信息，如金额、商户等
     */
    fun generateForPaymentNotification(notification: PaymentNotification): String =
        generateForPaymentNotification(notification, windowSec = 5 * 60)

    /**
     * 为解析后的通知生成指纹（可配置时间窗口）
     */
    fun generateForPaymentNotification(notification: PaymentNotification, windowSec: Int): String {
        val bucketMs = (windowSec.coerceAtLeast(1)) * 1000L
        val timeWindow = (notification.postedTime / bucketMs) * bucketMs
        val merchantHash = notification.normalizedMerchant?.hashCode()?.toString() ?: "null"

        val components = listOf(
            notification.sourceApp,
            notification.amountCents.toString(),
            timeWindow.toString(),
            merchantHash,
            notification.direction.name
        )

        return generateHash(components.joinToString("|"))
    }
    
    /**
     * 生成文本内容的hash
     */
    fun generateTextHash(text: String?): String {
        if (text.isNullOrBlank()) return "empty"
        return text.hashCode().toString()
    }
    
    /**
     * 生成商户名的hash
     */
    fun generateMerchantHash(merchant: String?): String? {
        if (merchant.isNullOrBlank()) return null
        return merchant.hashCode().toString()
    }
    
    /**
     * 检查两个事件是否在同一时间窗口内
     */
    fun isInSameTimeWindow(time1: Long, time2: Long, windowSizeMs: Long = 5 * 60 * 1000): Boolean {
        val window1 = time1 / windowSizeMs
        val window2 = time2 / windowSizeMs
        return window1 == window2
    }
    
    /**
     * 使用SHA-256生成hash
     */
    private fun generateHash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32) // 取前32位
    }
}
