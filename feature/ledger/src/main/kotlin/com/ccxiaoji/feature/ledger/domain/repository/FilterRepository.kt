package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionFilter
import kotlinx.coroutines.flow.StateFlow

/**
 * 筛选器仓库接口
 * 管理当前的筛选状态
 */
interface FilterRepository {
    /**
     * 当前筛选器状态
     */
    val currentFilter: StateFlow<TransactionFilter>
    
    /**
     * 更新筛选器
     */
    suspend fun updateFilter(filter: TransactionFilter)
    
    /**
     * 清除筛选器
     */
    suspend fun clearFilter()
}