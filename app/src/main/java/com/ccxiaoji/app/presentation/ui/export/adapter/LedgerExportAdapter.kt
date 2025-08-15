package com.ccxiaoji.app.presentation.ui.export.adapter

import com.ccxiaoji.app.presentation.ui.export.DateRange
import com.ccxiaoji.app.presentation.ui.export.ExportFormat
import com.ccxiaoji.app.presentation.ui.export.ModuleStats
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.domain.export.ExportConfig
import kotlinx.datetime.*
import java.io.File
import javax.inject.Inject

/**
 * 记账模块导出适配器
 * 负责将统一导出中心的请求转换为记账模块的导出调用
 */
class LedgerExportAdapter @Inject constructor(
    private val ledgerApi: LedgerApi
) {
    
    /**
     * 获取记账模块统计信息
     */
    suspend fun getStatistics(): ModuleStats {
        try {
            val stats = ledgerApi.getExportStatistics()
            
            // 计算总记录数
            val totalRecords = stats.transactionCount + 
                stats.accountCount + 
                stats.categoryCount + 
                stats.budgetCount + 
                stats.recurringCount + 
                stats.savingsCount
            
            // 估算文件大小（粗略估算，每条记录约100字节）
            val estimatedSizeKb = (totalRecords * 100) / 1024
            val estimatedSize = when {
                estimatedSizeKb < 1024 -> "${estimatedSizeKb}KB"
                else -> "${estimatedSizeKb / 1024}MB"
            }
            
            return ModuleStats(
                totalRecords = totalRecords,
                lastModified = stats.lastModified,
                estimatedSize = estimatedSize
            )
        } catch (e: Exception) {
            // 如果获取统计信息失败，返回默认值
            return ModuleStats(
                totalRecords = 0,
                lastModified = null,
                estimatedSize = "-"
            )
        }
    }
    
    /**
     * 导出记账数据
     */
    suspend fun exportData(
        dateRange: DateRange,
        format: ExportFormat
    ): File {
        // 计算日期范围
        val (startDate, endDate) = calculateDateRange(dateRange)
        
        // 构建导出配置
        val config = ExportConfig(
            includeTransactions = true,
            includeAccounts = true,
            includeCategories = true,
            includeBudgets = true,
            includeRecurringTransactions = true,
            includeSavingsGoals = true,
            startDate = startDate,
            endDate = endDate,
            format = when (format) {
                ExportFormat.CSV -> com.ccxiaoji.feature.ledger.domain.export.ExportFormat.CSV
                ExportFormat.JSON -> com.ccxiaoji.feature.ledger.domain.export.ExportFormat.JSON
                ExportFormat.EXCEL -> com.ccxiaoji.feature.ledger.domain.export.ExportFormat.EXCEL
            }
        )
        
        // 调用记账模块的导出功能
        return ledgerApi.exportAllData(config)
    }
    
    /**
     * 计算日期范围
     */
    private fun calculateDateRange(dateRange: DateRange): Pair<Long?, Long?> {
        return when (dateRange) {
            DateRange.ALL -> {
                // 全部数据，不限制日期
                Pair(null, null)
            }
            DateRange.THIS_MONTH -> {
                // 本月数据
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                val today = now.toLocalDateTime(timeZone).date
                
                val firstDayOfMonth = LocalDate(today.year, today.month, 1)
                val lastDayOfMonth = firstDayOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
                
                Pair(
                    firstDayOfMonth.atStartOfDayIn(timeZone).toEpochMilliseconds(),
                    lastDayOfMonth.atTime(23, 59, 59).toInstant(timeZone).toEpochMilliseconds()
                )
            }
            DateRange.THIS_YEAR -> {
                // 今年数据
                val now = Clock.System.now()
                val timeZone = TimeZone.currentSystemDefault()
                val today = now.toLocalDateTime(timeZone).date
                
                val firstDayOfYear = LocalDate(today.year, 1, 1)
                val lastDayOfYear = LocalDate(today.year, 12, 31)
                
                Pair(
                    firstDayOfYear.atStartOfDayIn(timeZone).toEpochMilliseconds(),
                    lastDayOfYear.atTime(23, 59, 59).toInstant(timeZone).toEpochMilliseconds()
                )
            }
            DateRange.CUSTOM -> {
                // 自定义范围，暂时返回全部数据
                // TODO: 实现自定义日期选择
                Pair(null, null)
            }
        }
    }
}