package com.ccxiaoji.common.data.import

/**
 * 账户数据导入模型
 */
data class AccountData(
    val id: Long,
    val name: String,
    val type: String,
    val balance: Double,
    val isActive: Boolean = true,
    val userId: Long,
    val syncStatus: String = "LOCAL"
)

/**
 * 模块导入结果
 */
data class ModuleImportResult(
    val module: DataModule,
    val success: Boolean,
    val importedCount: Int = 0,
    val skippedCount: Int = 0,
    val failedCount: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * 数据模块枚举
 */
enum class DataModule {
    ACCOUNTS,
    TRANSACTIONS,
    CATEGORIES,
    BUDGETS,
    SAVINGS_GOALS,
    TODOS,
    HABITS,
    SCHEDULES,
    PLANS,
    COUNTDOWNS
}