package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.model.LedgerWithStats
import kotlinx.coroutines.flow.Flow

/**
 * 记账簿仓库接口
 */
interface LedgerRepository {
    
    /**
     * 获取用户的所有激活记账簿
     */
    fun getUserLedgers(userId: String): Flow<List<Ledger>>
    
    /**
     * 获取用户的记账簿及统计数据
     */
    fun getUserLedgersWithStats(userId: String): Flow<List<LedgerWithStats>>
    
    /**
     * 获取用户的默认记账簿
     */
    suspend fun getDefaultLedger(userId: String): BaseResult<Ledger>
    
    /**
     * 根据ID获取记账簿
     */
    suspend fun getLedgerById(ledgerId: String): BaseResult<Ledger>
    
    /**
     * 创建记账簿
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
     * 更新记账簿
     */
    suspend fun updateLedger(ledger: Ledger): BaseResult<Unit>
    
    /**
     * 删除记账簿（软删除）
     */
    suspend fun deleteLedger(ledgerId: String): BaseResult<Unit>
    
    /**
     * 设置默认记账簿
     */
    suspend fun setDefaultLedger(userId: String, ledgerId: String): BaseResult<Unit>
    
    /**
     * 更新记账簿显示顺序
     */
    suspend fun updateLedgerOrder(ledgerId: String, newOrder: Int): BaseResult<Unit>
    
    /**
     * 确保用户有默认记账簿（如果没有则创建）
     */
    suspend fun ensureDefaultLedger(userId: String): BaseResult<Ledger>
    
    /**
     * 检查用户是否有默认记账簿
     */
    suspend fun hasDefaultLedger(userId: String): BaseResult<Boolean>
    
    /**
     * 批量更新记账簿顺序
     */
    suspend fun updateLedgersOrder(ledgerOrders: List<Pair<String, Int>>): BaseResult<Unit>
}