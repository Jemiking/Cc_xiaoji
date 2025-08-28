package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.common.base.safeSuspendCall
import com.ccxiaoji.feature.ledger.domain.model.AutoLedgerDebugRecord
import com.ccxiaoji.feature.ledger.domain.repository.AutoLedgerDebugRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import com.google.gson.Gson
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 自动记账调试记录Repository实现
 * 
 * 目前使用内存存储，后续可考虑添加数据库持久化
 */
@Singleton 
class AutoLedgerDebugRepositoryImpl @Inject constructor() : AutoLedgerDebugRepository {
    
    // 使用线程安全的Map存储调试记录
    private val debugRecords = ConcurrentHashMap<String, AutoLedgerDebugRecord>()
    
    // JSON序列化器
    private val gson = Gson()
    
    override suspend fun saveDebugRecord(record: AutoLedgerDebugRecord): BaseResult<Unit> = 
        safeSuspendCall {
            debugRecords[record.id] = record
        }
    
    override suspend fun getRecentDebugRecords(limit: Int): BaseResult<List<AutoLedgerDebugRecord>> = 
        safeSuspendCall {
            debugRecords.values
                .sortedByDescending { it.timestamp }
                .take(limit)
        }
    
    override fun getDebugRecordsFlow(limit: Int): Flow<List<AutoLedgerDebugRecord>> = flow {
        // 简单实现：定期发射最新记录
        // 实际使用中可以考虑使用StateFlow或其他响应式机制
        while (true) {
            val records = debugRecords.values
                .sortedByDescending { it.timestamp }
                .take(limit)
            emit(records)
            kotlinx.coroutines.delay(1000) // 每秒更新一次
        }
    }
    
    override suspend fun getDebugRecordsByStatus(
        statuses: List<AutoLedgerDebugRecord.ProcessingStatus>,
        limit: Int
    ): BaseResult<List<AutoLedgerDebugRecord>> = safeSuspendCall {
        debugRecords.values
            .filter { it.status in statuses }
            .sortedByDescending { it.timestamp }
            .take(limit)
    }
    
    override suspend fun getDebugRecordsByTimeRange(
        startTime: Instant,
        endTime: Instant,
        limit: Int
    ): BaseResult<List<AutoLedgerDebugRecord>> = safeSuspendCall {
        debugRecords.values
            .filter { it.timestamp in startTime..endTime }
            .sortedByDescending { it.timestamp }
            .take(limit)
    }
    
    override suspend fun clearOldDebugRecords(olderThan: Instant): BaseResult<Int> = 
        safeSuspendCall {
            val oldRecords = debugRecords.values.filter { it.timestamp < olderThan }
            oldRecords.forEach { record ->
                debugRecords.remove(record.id)
            }
            oldRecords.size
        }
    
    override suspend fun getDebugStatistics(): BaseResult<AutoLedgerDebugRepository.DebugStatistics> = 
        safeSuspendCall {
            val records = debugRecords.values
            val now = Clock.System.now()
            val last24Hours = now.minus(kotlin.time.Duration.parse("PT24H"))
            
            val last24HoursRecords = records.count { it.timestamp > last24Hours }
            val successCount = records.count { 
                it.status == AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_AUTO ||
                it.status == AutoLedgerDebugRecord.ProcessingStatus.SUCCESS_SEMI
            }
            val failureCount = records.count { 
                it.status == AutoLedgerDebugRecord.ProcessingStatus.FAILED_PARSE ||
                it.status == AutoLedgerDebugRecord.ProcessingStatus.FAILED_PROCESS ||
                it.status == AutoLedgerDebugRecord.ProcessingStatus.FAILED_UNKNOWN
            }
            val duplicateCount = records.count { it.isDuplicate }
            
            val averageProcessingTime = if (records.isNotEmpty()) {
                records.map { it.processingTimeMs }.average()
            } else 0.0
            
            val averageConfidence = if (records.isNotEmpty()) {
                records.map { it.parseConfidence }.average()
            } else 0.0
            
            AutoLedgerDebugRepository.DebugStatistics(
                totalRecords = records.size,
                last24HoursRecords = last24HoursRecords,
                successCount = successCount,
                failureCount = failureCount,
                duplicateCount = duplicateCount,
                averageProcessingTime = averageProcessingTime,
                averageConfidence = averageConfidence
            )
        }
    
    override suspend fun exportDebugRecords(
        masked: Boolean,
        limit: Int
    ): BaseResult<String> = safeSuspendCall {
        val records = debugRecords.values
            .sortedByDescending { it.timestamp }
            .take(limit)
            .let { recordsList ->
                if (masked) {
                    recordsList.map { it.masked() }
                } else {
                    recordsList
                }
            }
        
        val exportData = mapOf(
            "exportTime" to Clock.System.now().toString(),
            "recordCount" to records.size,
            "isMasked" to masked,
            "records" to records
        )
        
        gson.toJson(exportData)
    }
}