package com.ccxiaoji.common.data.import

/**
 * 数据导入结果
 */
data class ImportResult(
    val success: Boolean,
    val totalItems: Int,
    val importedItems: Int,
    val skippedItems: Int,
    val errors: List<ImportError>,
    val moduleResults: Map<DataModule, ModuleImportResult>,
    val importTime: Long = System.currentTimeMillis()
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val successRate: Float get() = if (totalItems > 0) importedItems.toFloat() / totalItems else 0f
}

/**
 * 导入错误信息
 */
data class ImportError(
    val message: String,
    val code: String = ""
)

/**
 * 模块导入结果
 */
data class ModuleImportResult(
    val module: DataModule,
    val totalItems: Int,
    val importedItems: Int,
    val skippedItems: Int,
    val errors: List<String>
)

/**
 * 数据模块枚举
 */
enum class DataModule(val displayName: String) {
    ACCOUNTS("账户"),
    CATEGORIES("分类"),
    TRANSACTIONS("交易记录"),
    BUDGETS("预算"),
    SAVINGS_GOALS("储蓄目标"),
    TASKS("待办任务"),
    HABITS("习惯"),
    HABIT_RECORDS("习惯记录"),
    COUNTDOWNS("倒计时"),
    USERS("用户信息")
}

/**
 * 导入配置
 */
data class ImportConfig(
    val selectedModules: Set<DataModule> = DataModule.values().toSet(),
    val skipExisting: Boolean = true,
    val createBackup: Boolean = true
)

/**
 * 导入文件验证结果
 */
data class ImportValidation(
    val isValid: Boolean,
    val fileSize: Long,
    val dataModules: List<DataModule>,
    val errors: List<String>
)