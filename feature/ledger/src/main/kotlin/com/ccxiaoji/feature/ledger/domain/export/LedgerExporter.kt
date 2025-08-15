package com.ccxiaoji.feature.ledger.domain.export

import java.io.File

/**
 * 记账模块导出器接口
 */
interface LedgerExporter {
    
    /**
     * 导出交易记录
     * @param startDate 开始日期（时间戳）
     * @param endDate 结束日期（时间戳）
     * @param accountIds 账户ID列表，null表示导出所有账户
     * @param categoryIds 分类ID列表，null表示导出所有分类
     * @return 导出的文件
     */
    suspend fun exportTransactions(
        startDate: Long? = null,
        endDate: Long? = null,
        accountIds: List<String>? = null,
        categoryIds: List<String>? = null
    ): File
    
    /**
     * 导出账户信息
     * @return 导出的文件
     */
    suspend fun exportAccounts(): File
    
    /**
     * 导出分类信息
     * @return 导出的文件
     */
    suspend fun exportCategories(): File
    
    /**
     * 导出预算信息
     * @param year 年份，null表示导出所有
     * @param month 月份，null表示导出所有
     * @return 导出的文件
     */
    suspend fun exportBudgets(
        year: Int? = null,
        month: Int? = null
    ): File
    
    /**
     * 导出所有数据
     * @param config 导出配置
     * @return 导出的文件（可能是ZIP包）
     */
    suspend fun exportAll(config: ExportConfig): File
}

/**
 * 导出配置
 */
data class ExportConfig(
    val includeTransactions: Boolean = true,
    val includeAccounts: Boolean = true,
    val includeCategories: Boolean = true,
    val includeBudgets: Boolean = true,
    val includeRecurringTransactions: Boolean = false,
    val includeSavingsGoals: Boolean = false,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val format: ExportFormat = ExportFormat.CSV
)

/**
 * 导出格式
 */
enum class ExportFormat {
    CSV,
    JSON,
    EXCEL
}