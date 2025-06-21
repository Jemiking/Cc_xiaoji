package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 处理交易搜索功能的ViewModel
 * 负责搜索查询、结果过滤等功能
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    
    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()
    
    // 搜索查询流，带防抖
    private val searchQueryFlow = MutableStateFlow("")
    
    init {
        // 设置搜索防抖
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300) // 300ms防抖
                .collect { query ->
                    performSearch(query)
                }
        }
    }
    
    /**
     * 更新搜索查询
     */
    fun updateSearchQuery(query: String) {
        _searchState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }
    
    /**
     * 切换搜索模式
     */
    fun toggleSearchMode() {
        _searchState.update { state ->
            if (state.isSearchMode) {
                // 退出搜索模式
                state.copy(
                    isSearchMode = false,
                    searchQuery = "",
                    searchResults = emptyList()
                )
            } else {
                // 进入搜索模式
                state.copy(isSearchMode = true)
            }
        }
    }
    
    /**
     * 清除搜索
     */
    fun clearSearch() {
        _searchState.update { 
            it.copy(
                searchQuery = "",
                searchResults = emptyList()
            )
        }
        searchQueryFlow.value = ""
    }
    
    /**
     * 设置可搜索的交易列表
     */
    fun setSearchableTransactions(transactions: List<Transaction>) {
        _searchState.update { it.copy(allTransactions = transactions) }
        // 如果当前有搜索查询，重新执行搜索
        if (_searchState.value.searchQuery.isNotBlank()) {
            performSearch(_searchState.value.searchQuery)
        }
    }
    
    /**
     * 执行搜索
     */
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            _searchState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        val allTransactions = _searchState.value.allTransactions
        val filtered = allTransactions.filter { transaction ->
            // 搜索备注
            transaction.note?.contains(query, ignoreCase = true) == true ||
            // 搜索分类名称
            transaction.categoryDetails?.name?.contains(query, ignoreCase = true) == true ||
            // 搜索金额（元）
            (transaction.amountCents / 100.0).toString().contains(query)
        }
        
        _searchState.update { 
            it.copy(
                searchResults = filtered,
                hasSearched = true
            )
        }
    }
    
    /**
     * 退出搜索模式
     */
    fun exitSearchMode() {
        _searchState.update { 
            SearchState() // 重置为初始状态
        }
        searchQueryFlow.value = ""
    }
}

/**
 * 搜索状态
 */
data class SearchState(
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val hasSearched: Boolean = false
) {
    val hasResults: Boolean get() = searchResults.isNotEmpty()
    val showEmptyState: Boolean get() = hasSearched && searchResults.isEmpty()
}