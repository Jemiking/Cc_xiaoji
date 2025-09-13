package com.ccxiaoji.feature.ledger.domain.repository

/**
 * 自动记账开发者设置仓库（仅写入，供监听/去重读取）
 */
interface AutoLedgerDeveloperSettingsRepository {
    suspend fun setEmitWithoutKeywords(enabled: Boolean)
    suspend fun setEmitGroupSummary(enabled: Boolean)
    suspend fun setLogUnmatched(enabled: Boolean)
    suspend fun setDedupEnabled(enabled: Boolean)
    suspend fun setDedupWindowSec(seconds: Int)
}

