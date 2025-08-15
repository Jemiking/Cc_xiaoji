package com.ccxiaoji.feature.ledger.domain.importer

/**
 * 导入配置
 */
data class ImportConfig(
    val conflictStrategy: ConflictStrategy = ConflictStrategy.SKIP,
    val includeTransactions: Boolean = true,
    val includeAccounts: Boolean = true,
    val includeCategories: Boolean = true,
    val includeBudgets: Boolean = true,
    val includeRecurringTransactions: Boolean = true,
    val includeSavingsGoals: Boolean = true,
    val includeCreditCardBills: Boolean = true,
    val allowPartialImport: Boolean = true,  // 允许部分导入（遇错继续）
    val batchSize: Int = 100,  // 批处理大小
    val skipInvalidRows: Boolean = true,  // 跳过无效行
    val autoFixErrors: Boolean = false,  // 自动修正错误
    val enableRetry: Boolean = true  // 启用重试机制
)

/**
 * 冲突处理策略
 */
enum class ConflictStrategy {
    SKIP,      // 跳过重复数据
    RENAME,    // 自动重命名
    MERGE,     // 合并数据
    OVERWRITE  // 覆盖现有数据
}