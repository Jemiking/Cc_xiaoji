package com.ccxiaoji.app.data.backup.model

import kotlinx.datetime.Instant

/**
 * 备份操作结果
 */
sealed class BackupResult {
    data class Success(
        val filePath: String,
        val fileSize: Long,
        val backupTime: Instant,
        val statistics: BackupStatistics
    ) : BackupResult()
    
    data class Error(
        val error: BackupError
    ) : BackupResult()
}

/**
 * 导入操作结果
 */
sealed class ImportResult {
    data class Success(
        val importedCount: Map<String, Int>,
        val skippedCount: Map<String, Int>,
        val importTime: Instant
    ) : ImportResult()
    
    data class Error(
        val error: BackupError
    ) : ImportResult()
}

/**
 * 导入模式
 */
enum class ImportMode {
    APPEND,    // 追加模式：保留现有数据，只添加新数据
    OVERWRITE  // 覆盖模式：清空现有数据，完全替换
}

/**
 * 导入配置
 */
data class ImportConfig(
    val mode: ImportMode = ImportMode.APPEND,
    val selectedModules: Set<String> = setOf(
        "transactions",
        "ledger_master",
        "todo",
        "habit",
        "schedule",
        "plan",
        "others"
    )
)