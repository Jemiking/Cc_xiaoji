package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.domain.repository.FilterRepository
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TransactionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 筛选器仓库实现
 * 管理当前的筛选状态
 */
@Singleton
class FilterRepositoryImpl @Inject constructor() : FilterRepository {
    
    private val _currentFilter = MutableStateFlow(TransactionFilter())
    override val currentFilter: StateFlow<TransactionFilter> = _currentFilter
    
    override suspend fun updateFilter(filter: TransactionFilter) {
        _currentFilter.value = filter
    }
    
    override suspend fun clearFilter() {
        _currentFilter.value = TransactionFilter()
    }
}