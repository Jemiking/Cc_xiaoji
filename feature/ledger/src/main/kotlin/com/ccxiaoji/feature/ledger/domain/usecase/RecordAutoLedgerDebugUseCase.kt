package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.AutoLedgerDebugRecord
import com.ccxiaoji.feature.ledger.domain.model.PaymentNotification
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerDebugRepository
import kotlinx.datetime.Clock
import java.util.*
import javax.inject.Inject

/**
 * 记录自动记账调试信息UseCase
 * 
 * 负责创建和保存调试记录，记录完整的处理流程
 */
class RecordAutoLedgerDebugUseCase @Inject constructor(
    private val debugRepository: AutoLedgerDebugRepository
) {
    
    /**
     * 记录成功的自动记账
     */
    suspend fun recordSuccess(
        notification: PaymentNotification,
        recommendation: com.ccxiaoji.feature.ledger.domain.usecase.AccountCategoryRecommendation,
        transactionId: String,
        processingTimeMs: Long,
        isAutomatic: Boolean
    ): BaseResult<Unit> {
        val record = createBaseRecord(notification, processingTimeMs).copy(
            status = if (isAutomatic) 
                AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_AUTO 
            else 
                AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_SEMI,
            recommendedAccountId = recommendation.accountId,
            recommendedCategoryId = recommendation.categoryId,
            recommendedLedgerId = recommendation.ledgerId,
            recommendationConfidence = recommendation.confidence,
            transactionId = transactionId
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 记录由于重复而跳过的处理
     */
    suspend fun recordSkippedDuplicate(
        notification: PaymentNotification,
        duplicateReason: String,
        processingTimeMs: Long
    ): BaseResult<Unit> {
        val record = createBaseRecord(notification, processingTimeMs).copy(
            status = AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_DUPLICATE,
            isDuplicate = true,
            duplicateReason = duplicateReason
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 记录由于置信度低而跳过的处理
     */
    suspend fun recordSkippedLowConfidence(
        notification: PaymentNotification,
        processingTimeMs: Long
    ): BaseResult<Unit> {
        val record = createBaseRecord(notification, processingTimeMs).copy(
            status = AutoLedgerDebugRecord.ProcessingStatus.SKIPPED_LOW_CONFIDENCE
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 记录解析失败
     */
    suspend fun recordParseFailure(
        sourceType: com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType,
        notificationTitle: String,
        notificationText: String,
        errorMessage: String,
        processingTimeMs: Long
    ): BaseResult<Unit> {
        val record = AutoLedgerDebugRecord(
            id = generateRecordId(),
            timestamp = Clock.System.now(),
            status = AutoLedgerDebugRecord.ProcessingStatus.FAILED_PARSE,
            sourceType = sourceType,
            notificationTitle = notificationTitle,
            notificationText = notificationText,
            parsedMerchant = null,
            parsedAmount = null,
            parsedDirection = null,
            parseConfidence = 0.0,
            fingerprint = "",
            isDuplicate = false,
            duplicateReason = null,
            recommendedAccountId = null,
            recommendedCategoryId = null,
            recommendedLedgerId = null,
            recommendationConfidence = 0.0,
            processingTimeMs = processingTimeMs,
            errorMessage = errorMessage,
            transactionId = null
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 记录处理失败
     */
    suspend fun recordProcessFailure(
        notification: PaymentNotification,
        errorMessage: String,
        processingTimeMs: Long
    ): BaseResult<Unit> {
        val record = createBaseRecord(notification, processingTimeMs).copy(
            status = AutoLedgerDebugRecord.ProcessingStatus.FAILED_PROCESS,
            errorMessage = errorMessage
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 记录未知错误
     */
    suspend fun recordUnknownError(
        sourceType: com.ccxiaoji.feature.ledger.domain.model.PaymentSourceType,
        notificationTitle: String,
        notificationText: String,
        errorMessage: String,
        processingTimeMs: Long
    ): BaseResult<Unit> {
        val record = AutoLedgerDebugRecord(
            id = generateRecordId(),
            timestamp = Clock.System.now(),
            status = AutoLedgerDebugRecord.ProcessingStatus.FAILED_UNKNOWN,
            sourceType = sourceType,
            notificationTitle = notificationTitle,
            notificationText = notificationText,
            parsedMerchant = null,
            parsedAmount = null,
            parsedDirection = null,
            parseConfidence = 0.0,
            fingerprint = "",
            isDuplicate = false,
            duplicateReason = null,
            recommendedAccountId = null,
            recommendedCategoryId = null,
            recommendedLedgerId = null,
            recommendationConfidence = 0.0,
            processingTimeMs = processingTimeMs,
            errorMessage = errorMessage,
            transactionId = null
        )
        
        return debugRepository.saveDebugRecord(record)
    }
    
    /**
     * 创建基础调试记录
     */
    private fun createBaseRecord(
        notification: PaymentNotification,
        processingTimeMs: Long
    ): AutoLedgerDebugRecord {
        return AutoLedgerDebugRecord(
            id = generateRecordId(),
            timestamp = Clock.System.now(),
            status = AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_AUTO, // 会被覆盖
            sourceType = notification.sourceType,
            notificationTitle = notification.originalTitle,
            notificationText = notification.originalText,
            parsedMerchant = notification.normalizedMerchant,
            parsedAmount = notification.amountCents,
            parsedDirection = notification.direction,
            parseConfidence = notification.confidence,
            fingerprint = notification.fingerprint,
            isDuplicate = false,
            duplicateReason = null,
            recommendedAccountId = null,
            recommendedCategoryId = null,
            recommendedLedgerId = null,
            recommendationConfidence = 0.0,
            processingTimeMs = processingTimeMs,
            errorMessage = null,
            transactionId = null
        )
    }
    
    /**
     * 生成唯一的记录ID
     */
    private fun generateRecordId(): String {
        return "debug_${Clock.System.now().epochSeconds}_${UUID.randomUUID().toString().substring(0, 8)}"
    }
}