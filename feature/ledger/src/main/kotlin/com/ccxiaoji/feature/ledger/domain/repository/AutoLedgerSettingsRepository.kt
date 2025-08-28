package com.ccxiaoji.feature.ledger.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 自动记账设置仓库
 *
 * 管理自动记账的全局开关等首要设置。
 */
interface AutoLedgerSettingsRepository {
    /**
     * 全局开关：是否启用自动记账
     */
    fun globalEnabled(): Flow<Boolean>

    /**
     * 设置全局开关
     */
    suspend fun setGlobalEnabled(enabled: Boolean)
}

