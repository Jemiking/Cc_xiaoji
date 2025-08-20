package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 交易记录仓库接口
 * 定义所有交易相关的数据操作
 */
interface TransactionRepository {
    /**
     * 获取所有交易记录
     */
    fun getTransactions(): Flow<List<Transaction>>
    
    /**
     * 根据日期范围获取交易记录
     */
    fun getTransactionsByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>
    
    /**
     * 根据账户获取交易记录
     */
    fun getTransactionsByAccount(accountId: String): Flow<List<Transaction>>
    
    /**
     * 根据分类获取交易记录
     */
    fun getTransactionsByCategory(categoryId: String): Flow<List<Transaction>>
    
    /**
     * 搜索交易记录
     */
    fun searchTransactions(query: String): Flow<List<Transaction>>
    
    /**
     * 根据记账簿获取交易记录
     */
    fun getTransactionsByLedger(ledgerId: String): Flow<List<Transaction>>
    
    /**
     * 根据记账簿和日期范围获取交易记录
     */
    fun getTransactionsByLedgerAndDateRange(
        ledgerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Transaction>>
    
    /**
     * 根据多个记账簿获取交易记录（联合查询）
     */
    fun getTransactionsByLedgers(ledgerIds: List<String>): Flow<List<Transaction>>
    
    /**
     * 获取记账簿的月度收入和支出
     */
    suspend fun getMonthlyIncomesAndExpensesByLedger(
        ledgerId: String,
        year: Int,
        month: Int
    ): BaseResult<Pair<Int, Int>>
    
    /**
     * 添加交易记录
     * @return 创建的交易ID
     */
    suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String,
        ledgerId: String,
        transactionDate: kotlinx.datetime.Instant? = null,
        location: com.ccxiaoji.feature.ledger.domain.model.LocationData? = null
    ): BaseResult<Long>
    
    /**
     * 更新交易记录
     */
    suspend fun updateTransaction(transaction: Transaction): BaseResult<Unit>
    
    /**
     * 删除交易记录
     */
    suspend fun deleteTransaction(transactionId: String): BaseResult<Unit>
    
    /**
     * 获取月度收入和支出
     * @return Pair<收入, 支出>（单位：分）
     */
    suspend fun getMonthlyIncomesAndExpenses(year: Int, month: Int): BaseResult<Pair<Int, Int>>
    
    /**
     * 获取分类统计
     */
    suspend fun getCategoryStatistics(
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>>
    
    /**
     * 获取指定记账簿的分类统计
     */
    suspend fun getCategoryStatisticsByLedger(
        ledgerId: String,
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>>
    
    /**
     * 获取多个记账簿的分类统计
     */
    suspend fun getCategoryStatisticsByLedgers(
        ledgerIds: List<String>,
        categoryType: String?,
        startDate: Long,
        endDate: Long
    ): BaseResult<List<CategoryStatistic>>
    
    /**
     * 获取月度总额（收入-支出）
     */
    suspend fun getMonthlyTotal(year: Int, month: Int): BaseResult<Int>
    
    /**
     * 获取最近交易记录
     */
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>
    
    /**
     * 根据ID获取交易记录
     */
    suspend fun getTransactionById(transactionId: String): Transaction?
    
    /**
     * 根据账户和日期范围获取交易记录
     */
    fun getTransactionsByAccountAndDateRange(
        accountId: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Flow<List<Transaction>>
    
    /**
     * 获取每日收支统计
     */
    suspend fun getDailyTotals(
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>>
    
    /**
     * 获取指定记账簿的每日收支统计
     */
    suspend fun getDailyTotalsByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>>
    
    /**
     * 获取多个记账簿的每日收支统计
     */
    suspend fun getDailyTotalsByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>>
    
    /**
     * 获取金额最大的交易记录
     */
    suspend fun getTopTransactions(
        startDate: LocalDate, 
        endDate: LocalDate, 
        type: String, 
        limit: Int = 10
    ): BaseResult<List<Transaction>>
    
    /**
     * 获取指定记账簿的金额最大交易记录
     */
    suspend fun getTopTransactionsByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate, 
        type: String, 
        limit: Int = 10
    ): BaseResult<List<Transaction>>
    
    /**
     * 获取多个记账簿的金额最大交易记录
     */
    suspend fun getTopTransactionsByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate, 
        type: String, 
        limit: Int = 10
    ): BaseResult<List<Transaction>>
    
    /**
     * 计算储蓄率
     */
    suspend fun calculateSavingsRate(
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Float>
    
    /**
     * 计算指定记账簿的储蓄率
     */
    suspend fun calculateSavingsRateByLedger(
        ledgerId: String,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Float>
    
    /**
     * 计算多个记账簿的储蓄率
     */
    suspend fun calculateSavingsRateByLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate, 
        endDate: LocalDate
    ): BaseResult<Float>
    
    /**
     * 获取分页的交易记录
     * @param offset 偏移量
     * @param limit 每页数量
     * @param accountId 账户ID（可选）
     * @param startDate 开始时间戳（可选）
     * @param endDate 结束时间戳（可选）
     * @return Pair<交易列表, 总数>
     */
    fun getTransactionsPaginated(
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>>
    
    /**
     * 获取指定记账簿的分页交易记录
     */
    fun getTransactionsPaginatedByLedger(
        ledgerId: String,
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>>
    
    /**
     * 获取多个记账簿的分页交易记录
     */
    fun getTransactionsPaginatedByLedgers(
        ledgerIds: List<String>,
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>>
    
}