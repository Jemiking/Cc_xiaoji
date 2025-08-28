package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.AutoLedgerDebugRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * 自动记账调试记录Repository
 */
interface AutoLedgerDebugRepository {
    
    /**
     * 保存调试记录
     */
    suspend fun saveDebugRecord(record: AutoLedgerDebugRecord): BaseResult<Unit>
    
    /**
     * 获取最近N条调试记录
     * @param limit 记录数量限制，默认100条
     */
    suspend fun getRecentDebugRecords(limit: Int = 100): BaseResult<List<AutoLedgerDebugRecord>>
    
    /**
     * 获取调试记录流（实时更新）
     * @param limit 记录数量限制，默认50条
     */
    fun getDebugRecordsFlow(limit: Int = 50): Flow<List<AutoLedgerDebugRecord>>
    
    /**
     * 根据状态筛选调试记录
     */
    suspend fun getDebugRecordsByStatus(
        statuses: List<AutoLedgerDebugRecord.ProcessingStatus>,
        limit: Int = 100
    ): BaseResult<List<AutoLedgerDebugRecord>>
    
    /**
     * 根据时间范围获取调试记录
     */
    suspend fun getDebugRecordsByTimeRange(
        startTime: Instant,
        endTime: Instant,
        limit: Int = 100
    ): BaseResult<List<AutoLedgerDebugRecord>>
    
    /**
     * 清除过期的调试记录
     * @param olderThan 清除早于此时间的记录
     */
    suspend fun clearOldDebugRecords(olderThan: Instant): BaseResult<Int>
    
    /**
     * 获取调试统计信息
     */
    suspend fun getDebugStatistics(): BaseResult<DebugStatistics>
    
    /**
     * 导出调试记录为JSON
     * @param masked 是否脱敏处理
     * @param limit 导出数量限制
     */
    suspend fun exportDebugRecords(
        masked: Boolean = true,
        limit: Int = 1000
    ): BaseResult<String>
    
    /**
     * 调试统计信息
     */
    data class DebugStatistics(
        val totalRecords: Int,
        val last24HoursRecords: Int,
        val successCount: Int,
        val failureCount: Int,
        val duplicateCount: Int,
        val averageProcessingTime: Double,
        val averageConfidence: Double
    )
}