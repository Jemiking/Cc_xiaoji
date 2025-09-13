package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerWithStats
import kotlinx.coroutines.flow.Flow

/**
 * 账本仓库接口
 */
interface LedgerRepository {
    
    /**
     * 获取用户的所有激活账本
     */
    fun getUserLedgers(userId: String): Flow<List<Ledger>>
    
    /**
     * 获取用户的账本及统计数据
     */
    fun getUserLedgersWithStats(userId: String): Flow<List<LedgerWithStats>>
    
    /**
     * 获取用户的默认账本
     */
    suspend fun getDefaultLedger(userId: String): BaseResult<Ledger>
    
    /**
     * 根据 ID 获取账本
     */
    suspend fun getLedgerById(ledgerId: String): BaseResult<Ledger>
    
    /**
     * 创建账本
     */
    suspend fun createLedger(
        userId: String,
        name: String,
        description: String? = null,
        color: String = "#3A7AFE",
        icon: String = "book",
        isDefault: Boolean = false
    ): BaseResult<Ledger>
    
    /**
     * 更新账本
     */
    suspend fun updateLedger(ledger: Ledger): BaseResult<Unit>
    
    /**
     * 删除账本（软删除）
     */
    suspend fun deleteLedger(ledgerId: String): BaseResult<Unit>
    
    /**
     * 设置默认账本
     */
    suspend fun setDefaultLedger(userId: String, ledgerId: String): BaseResult<Unit>
    
    /**
     * 更新账本显示顺序
     */
    suspend fun updateLedgerOrder(ledgerId: String, newOrder: Int): BaseResult<Unit>
    
    /**
        * 确保用户有默认账本（如果没有则创建）
     */
    suspend fun ensureDefaultLedger(userId: String): BaseResult<Ledger>
    
    /**
     * 检查用户是否有默认账本
     */
    suspend fun hasDefaultLedger(userId: String): BaseResult<Boolean>
    
    /**
     * 批量更新账本顺序
     */
    suspend fun updateLedgersOrder(ledgerOrders: List<Pair<String, Int>>): BaseResult<Unit>
}
