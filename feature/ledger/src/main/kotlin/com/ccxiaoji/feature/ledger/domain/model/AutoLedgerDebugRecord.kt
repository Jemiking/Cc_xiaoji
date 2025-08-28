package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.Instant

/**
 * 自动记账调试记录
 * 
 * 用于记录每次通知处理的详细信息，便于调试和问题定位
 */
data class AutoLedgerDebugRecord(
    val id: String,
    val timestamp: Instant,
    val status: ProcessingStatus,
    val sourceType: PaymentSourceType,
    val notificationTitle: String, // 可脱敏
    val notificationText: String, // 可脱敏
    val parsedMerchant: String?, // 可脱敏
    val parsedAmount: Long?, // 金额（分）
    val parsedDirection: PaymentDirection?,
    val parseConfidence: Double,
    val fingerprint: String, // 用于去重的指纹
    val isDuplicate: Boolean,
    val duplicateReason: String?, // 去重原因
    val recommendedAccountId: String?,
    val recommendedCategoryId: String?,
    val recommendedLedgerId: String?,
    val recommendationConfidence: Double,
    val processingTimeMs: Long, // 处理耗时
    val errorMessage: String?, // 错误信息
    val transactionId: String?, // 成功创建的交易ID
    val isSensitiveDataMasked: Boolean = false // 是否已脱敏
) {
    enum class ProcessingStatus {
        SUCCESS_AUTO, // 自动记账成功
        SUCCESS_SEMI, // 半自动记账成功
        SKIPPED_DUPLICATE, // 跳过（重复）
        SKIPPED_LOW_CONFIDENCE, // 跳过（置信度低）
        FAILED_PARSE, // 解析失败
        FAILED_PROCESS, // 处理失败
        FAILED_UNKNOWN // 未知错误
    }
    
    /**
     * 获取脱敏后的记录
     */
    fun masked(): AutoLedgerDebugRecord {
        if (isSensitiveDataMasked) return this
        
        return copy(
            notificationTitle = maskText(notificationTitle),
            notificationText = maskText(notificationText),
            parsedMerchant = parsedMerchant?.let { maskMerchant(it) },
            isSensitiveDataMasked = true
        )
    }
    
    private fun maskText(text: String): String {
        // 保留前2个和后2个字符，中间用*代替
        return when {
            text.length <= 4 -> "*".repeat(text.length)
            text.length <= 6 -> text.substring(0, 2) + "*".repeat(text.length - 4) + text.substring(text.length - 2)
            else -> text.substring(0, 3) + "*".repeat(minOf(8, text.length - 6)) + text.substring(text.length - 3)
        }
    }
    
    private fun maskMerchant(merchant: String): String {
        // 商户名脱敏：保留首尾，中间用*代替
        return when {
            merchant.length <= 2 -> "*".repeat(merchant.length)
            merchant.length <= 4 -> merchant.substring(0, 1) + "*".repeat(merchant.length - 2) + merchant.substring(merchant.length - 1)
            else -> merchant.substring(0, 2) + "*".repeat(minOf(6, merchant.length - 4)) + merchant.substring(merchant.length - 2)
        }
    }
    
    /**
     * 获取状态显示文本
     */
    fun getStatusText(): String = when (status) {
        ProcessingStatus.SUCCESS_AUTO -> "自动记账成功"
        ProcessingStatus.SUCCESS_SEMI -> "半自动记账"
        ProcessingStatus.SKIPPED_DUPLICATE -> "跳过-重复"
        ProcessingStatus.SKIPPED_LOW_CONFIDENCE -> "跳过-置信度低"
        ProcessingStatus.FAILED_PARSE -> "解析失败"
        ProcessingStatus.FAILED_PROCESS -> "处理失败"
        ProcessingStatus.FAILED_UNKNOWN -> "未知错误"
    }
    
    /**
     * 获取状态颜色
     */
    fun getStatusColor(): String = when (status) {
        ProcessingStatus.SUCCESS_AUTO -> "#4CAF50" // 绿色
        ProcessingStatus.SUCCESS_SEMI -> "#FF9800" // 橙色
        ProcessingStatus.SKIPPED_DUPLICATE -> "#2196F3" // 蓝色
        ProcessingStatus.SKIPPED_LOW_CONFIDENCE -> "#9C27B0" // 紫色
        ProcessingStatus.FAILED_PARSE -> "#F44336" // 红色
        ProcessingStatus.FAILED_PROCESS -> "#F44336" // 红色
        ProcessingStatus.FAILED_UNKNOWN -> "#757575" // 灰色
    }
}