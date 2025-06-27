package com.ccxiaoji.feature.ledger.data.cache

import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 记账模块缓存管理器
 * 用于缓存频繁访问的数据，提升性能
 */
@Singleton
class LedgerCacheManager @Inject constructor() {
    
    // 账户缓存
    private val _accountsCache = MutableStateFlow<Map<String, Account>>(emptyMap())
    val accountsCache: StateFlow<Map<String, Account>> = _accountsCache.asStateFlow()
    
    // 分类缓存
    private val _categoriesCache = MutableStateFlow<Map<String, Category>>(emptyMap())
    val categoriesCache: StateFlow<Map<String, Category>> = _categoriesCache.asStateFlow()
    
    // 最近交易缓存（仅缓存最近100条）
    private val _recentTransactionsCache = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactionsCache: StateFlow<List<Transaction>> = _recentTransactionsCache.asStateFlow()
    
    // 月度统计缓存
    private val monthlyStatisticsCache = ConcurrentHashMap<String, MonthlyStatistics>()
    
    // 分类统计缓存
    private val categoryStatisticsCache = ConcurrentHashMap<String, CategoryStatisticsCache>()
    
    /**
     * 更新账户缓存
     */
    fun updateAccountsCache(accounts: List<Account>) {
        _accountsCache.value = accounts.associateBy { it.id }
    }
    
    /**
     * 获取缓存的账户
     */
    fun getCachedAccount(accountId: String): Account? {
        return _accountsCache.value[accountId]
    }
    
    /**
     * 更新分类缓存
     */
    fun updateCategoriesCache(categories: List<Category>) {
        _categoriesCache.value = categories.associateBy { it.id }
    }
    
    /**
     * 获取缓存的分类
     */
    fun getCachedCategory(categoryId: String): Category? {
        return _categoriesCache.value[categoryId]
    }
    
    /**
     * 更新最近交易缓存
     */
    fun updateRecentTransactionsCache(transactions: List<Transaction>) {
        _recentTransactionsCache.value = transactions.take(100)
    }
    
    /**
     * 添加新交易到缓存
     */
    fun addTransactionToCache(transaction: Transaction) {
        val current = _recentTransactionsCache.value.toMutableList()
        current.add(0, transaction)
        _recentTransactionsCache.value = current.take(100)
        
        // 清除相关的月度统计缓存
        val monthKey = "${transaction.createdAt.toEpochMilliseconds() / 1000 / 86400 / 30}"
        monthlyStatisticsCache.remove(monthKey)
    }
    
    /**
     * 从缓存中删除交易
     */
    fun removeTransactionFromCache(transactionId: String) {
        _recentTransactionsCache.value = _recentTransactionsCache.value.filter { it.id != transactionId }
    }
    
    /**
     * 缓存月度统计数据
     */
    fun cacheMonthlyStatistics(year: Int, month: Int, statistics: MonthlyStatistics) {
        val key = "$year-$month"
        monthlyStatisticsCache[key] = statistics
    }
    
    /**
     * 获取缓存的月度统计数据
     */
    fun getCachedMonthlyStatistics(year: Int, month: Int): MonthlyStatistics? {
        val key = "$year-$month"
        return monthlyStatisticsCache[key]
    }
    
    /**
     * 缓存分类统计数据
     */
    fun cacheCategoryStatistics(
        categoryType: String,
        startDate: Long,
        endDate: Long,
        statistics: List<com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic>
    ) {
        val key = "$categoryType-$startDate-$endDate"
        categoryStatisticsCache[key] = CategoryStatisticsCache(
            timestamp = System.currentTimeMillis(),
            data = statistics
        )
    }
    
    /**
     * 获取缓存的分类统计数据
     */
    fun getCachedCategoryStatistics(
        categoryType: String,
        startDate: Long,
        endDate: Long
    ): List<com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic>? {
        val key = "$categoryType-$startDate-$endDate"
        val cached = categoryStatisticsCache[key] ?: return null
        
        // 缓存有效期为5分钟
        if (System.currentTimeMillis() - cached.timestamp > 5 * 60 * 1000) {
            categoryStatisticsCache.remove(key)
            return null
        }
        
        return cached.data
    }
    
    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        _accountsCache.value = emptyMap()
        _categoriesCache.value = emptyMap()
        _recentTransactionsCache.value = emptyList()
        monthlyStatisticsCache.clear()
        categoryStatisticsCache.clear()
    }
    
    /**
     * 清除交易相关缓存
     */
    fun clearTransactionCache() {
        _recentTransactionsCache.value = emptyList()
        monthlyStatisticsCache.clear()
        categoryStatisticsCache.clear()
    }
    
    data class MonthlyStatistics(
        val income: Int,
        val expense: Int,
        val balance: Int
    )
    
    data class CategoryStatisticsCache(
        val timestamp: Long,
        val data: List<com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic>
    )
}