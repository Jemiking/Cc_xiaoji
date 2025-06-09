package com.ccxiaoji.feature.ledger.api

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Ledger模块对外暴露的API接口
 * 分步迁移：先提供统计功能相关API
 */
interface LedgerApi {
    // ========== 统计功能 ==========
    
    /**
     * 获取今日收支统计
     */
    suspend fun getTodayStatistics(): DailyStatistics
    
    /**
     * 获取本月支出总额
     */
    suspend fun getMonthlyExpense(year: Int, month: Int): Double
    
    /**
     * 获取指定日期范围的收支统计
     */
    suspend fun getStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): PeriodStatistics
    
    /**
     * 获取账户总余额
     */
    suspend fun getTotalBalance(): Double
    
    /**
     * 获取最近交易记录（用于首页展示）
     */
    fun getRecentTransactions(limit: Int): Flow<List<TransactionItem>>
    
    // ========== 导航功能 ==========
    
    /**
     * 导航到记账主页
     */
    fun navigateToLedger()
    
    /**
     * 导航到快速记账
     */
    fun navigateToQuickAdd()
    
    /**
     * 导航到统计页面
     */
    fun navigateToStatistics()
    
    /**
     * 导航到账户管理
     */
    fun navigateToAccounts()
    
    /**
     * 导航到分类管理
     */
    fun navigateToCategories()
    
    // ========== 分类管理功能 ==========
    
    /**
     * 获取所有分类
     */
    suspend fun getAllCategories(): List<CategoryItem>
    
    /**
     * 根据类型获取分类
     */
    suspend fun getCategoriesByType(type: String): List<CategoryItem>
    
    /**
     * 添加分类
     */
    suspend fun addCategory(name: String, type: String, icon: String, color: String, parentId: String? = null)
    
    /**
     * 更新分类
     */
    suspend fun updateCategory(categoryId: String, name: String, icon: String, color: String)
    
    /**
     * 删除分类
     */
    suspend fun deleteCategory(categoryId: String)
    
    /**
     * 获取分类使用次数
     */
    suspend fun getCategoryUsageCount(categoryId: String): Int
    
    // ========== 交易记录功能 ==========
    
    /**
     * 获取指定月份的交易记录
     */
    suspend fun getTransactionsByMonth(year: Int, month: Int): List<TransactionItem>
    
    /**
     * 获取最近交易记录列表
     */
    suspend fun getRecentTransactionsList(limit: Int = 10): List<TransactionItem>
    
    /**
     * 添加交易记录
     */
    suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String? = null
    ): String // 返回交易ID
    
    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(
        transactionId: String,
        amountCents: Int,
        categoryId: String,
        note: String?
    )
    
    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(transactionId: String)
    
    /**
     * 批量删除交易记录
     */
    suspend fun deleteTransactions(transactionIds: List<String>)
    
    /**
     * 搜索交易记录
     */
    suspend fun searchTransactions(query: String): List<TransactionItem>
    
    /**
     * 根据账户获取交易记录
     */
    suspend fun getTransactionsByAccount(accountId: String): List<TransactionItem>
    
    /**
     * 获取交易详情
     */
    suspend fun getTransactionDetail(transactionId: String): TransactionDetail?
    
    /**
     * 获取日期范围内的交易统计
     */
    suspend fun getTransactionStatsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionStats
    
    /**
     * 导航到交易详情
     */
    fun navigateToTransactionDetail(transactionId: String)
}

/**
 * 每日统计数据
 */
data class DailyStatistics(
    val income: Double,
    val expense: Double,
    val balance: Double
)

/**
 * 期间统计数据
 */
data class PeriodStatistics(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactionCount: Int
)

/**
 * 交易记录条目（简化版，用于列表展示）
 */
data class TransactionItem(
    val id: String,
    val amount: Double,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String,
    val accountName: String,
    val note: String?,
    val date: LocalDate
)

/**
 * 分类信息（用于展示）
 */
data class CategoryItem(
    val id: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String? = null,
    val isSystem: Boolean = false,
    val usageCount: Int = 0
)

/**
 * 交易详情（完整信息）
 */
data class TransactionDetail(
    val id: String,
    val amountCents: Int,
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val categoryType: String,
    val accountId: String,
    val accountName: String,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val amountYuan: Double
        get() = amountCents / 100.0
}

/**
 * 交易统计信息
 */
data class TransactionStats(
    val totalIncome: Int,
    val totalExpense: Int,
    val transactionCount: Int,
    val categoryStats: List<CategoryStat>
) {
    val totalIncomeYuan: Double
        get() = totalIncome / 100.0
    
    val totalExpenseYuan: Double
        get() = totalExpense / 100.0
    
    val balance: Int
        get() = totalIncome - totalExpense
    
    val balanceYuan: Double
        get() = balance / 100.0
}

/**
 * 分类统计
 */
data class CategoryStat(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val totalAmount: Int,
    val transactionCount: Int,
    val percentage: Float
) {
    val totalAmountYuan: Double
        get() = totalAmount / 100.0
}