package com.ccxiaoji.app.presentation.ui.profile

/**
 * 数据导入结果
 */
data class ImportResult(
    val success: Boolean,
    val message: String? = null,
    val errorMessage: String? = null,
    val importedCount: Int = 0,
    val failedCount: Int = 0,
    val transactionCount: Int = 0,
    val todoCount: Int = 0,
    val habitCount: Int = 0,
    val otherCount: Int = 0,
    val details: Map<String, Int> = emptyMap()
)