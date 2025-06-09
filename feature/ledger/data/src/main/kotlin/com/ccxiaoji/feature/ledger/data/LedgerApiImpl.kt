package com.ccxiaoji.feature.ledger.data

import com.ccxiaoji.feature.ledger.api.CategoryItem
import com.ccxiaoji.feature.ledger.api.DailyStatistics
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.api.PeriodStatistics
import com.ccxiaoji.feature.ledger.api.TransactionDetail
import com.ccxiaoji.feature.ledger.api.TransactionItem
import com.ccxiaoji.feature.ledger.api.TransactionStats
import com.ccxiaoji.feature.ledger.data.repository.CategoryRepository
import com.ccxiaoji.feature.ledger.data.repository.StatisticsRepository
import com.ccxiaoji.feature.ledger.data.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LedgerApi的实现类
 * 委托给各个Repository处理具体业务逻辑
 */
@Singleton
class LedgerApiImpl @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val ledgerNavigator: LedgerNavigator
) : LedgerApi {
    
    // ========== 统计功能 ==========
    
    override suspend fun getTodayStatistics(): DailyStatistics {
        return statisticsRepository.getTodayStatistics()
    }
    
    override suspend fun getMonthlyExpense(year: Int, month: Int): Double {
        return statisticsRepository.getMonthlyExpense(year, month)
    }
    
    override suspend fun getStatisticsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): PeriodStatistics {
        return statisticsRepository.getStatisticsByDateRange(startDate, endDate)
    }
    
    override suspend fun getTotalBalance(): Double {
        return statisticsRepository.getTotalBalance()
    }
    
    override fun getRecentTransactions(limit: Int): Flow<List<TransactionItem>> {
        return statisticsRepository.getRecentTransactions(limit)
    }
    
    // ========== 导航功能 ==========
    
    override fun navigateToLedger() {
        ledgerNavigator.navigateToLedger()
    }
    
    override fun navigateToQuickAdd() {
        ledgerNavigator.navigateToQuickAdd()
    }
    
    override fun navigateToStatistics() {
        ledgerNavigator.navigateToStatistics()
    }
    
    override fun navigateToAccounts() {
        ledgerNavigator.navigateToAccounts()
    }
    
    override fun navigateToCategories() {
        ledgerNavigator.navigateToCategories()
    }
    
    // ========== 分类管理功能 ==========
    
    override suspend fun getAllCategories(): List<CategoryItem> {
        return categoryRepository.getAllCategories()
    }
    
    override suspend fun getCategoriesByType(type: String): List<CategoryItem> {
        return categoryRepository.getCategoriesByType(type)
    }
    
    override suspend fun addCategory(
        name: String,
        type: String,
        icon: String,
        color: String,
        parentId: String?
    ) {
        categoryRepository.createCategory(name, type, icon, color, parentId)
    }
    
    override suspend fun updateCategory(
        categoryId: String,
        name: String,
        icon: String,
        color: String
    ) {
        categoryRepository.updateCategory(categoryId, name, icon, color)
    }
    
    override suspend fun deleteCategory(categoryId: String) {
        categoryRepository.deleteCategory(categoryId)
    }
    
    override suspend fun getCategoryUsageCount(categoryId: String): Int {
        return categoryRepository.getCategoryUsageCount(categoryId)
    }
    
    // ========== 交易记录功能 ==========
    
    override suspend fun getTransactionsByMonth(year: Int, month: Int): List<TransactionItem> {
        return transactionRepository.getTransactionsByMonth(getCurrentUserId(), year, month)
    }
    
    override suspend fun getRecentTransactionsList(limit: Int): List<TransactionItem> {
        return transactionRepository.getRecentTransactions(getCurrentUserId(), limit)
    }
    
    override suspend fun addTransaction(
        amountCents: Int,
        categoryId: String,
        note: String?,
        accountId: String?
    ): String {
        return transactionRepository.addTransaction(
            userId = getCurrentUserId(),
            amountCents = amountCents,
            categoryId = categoryId,
            note = note,
            accountId = accountId
        )
    }
    
    override suspend fun updateTransaction(
        transactionId: String,
        amountCents: Int,
        categoryId: String,
        note: String?
    ) {
        transactionRepository.updateTransaction(
            userId = getCurrentUserId(),
            transactionId = transactionId,
            amountCents = amountCents,
            categoryId = categoryId,
            note = note
        )
    }
    
    override suspend fun deleteTransaction(transactionId: String) {
        transactionRepository.deleteTransaction(getCurrentUserId(), transactionId)
    }
    
    override suspend fun deleteTransactions(transactionIds: List<String>) {
        transactionRepository.deleteTransactions(getCurrentUserId(), transactionIds)
    }
    
    override suspend fun searchTransactions(query: String): List<TransactionItem> {
        return transactionRepository.searchTransactions(getCurrentUserId(), query)
    }
    
    override suspend fun getTransactionsByAccount(accountId: String): List<TransactionItem> {
        return transactionRepository.getTransactionsByAccount(getCurrentUserId(), accountId)
    }
    
    override suspend fun getTransactionDetail(transactionId: String): TransactionDetail? {
        return transactionRepository.getTransactionDetail(transactionId)
    }
    
    override suspend fun getTransactionStatsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): TransactionStats {
        return transactionRepository.getTransactionStatsByDateRange(
            userId = getCurrentUserId(),
            startDate = startDate,
            endDate = endDate
        )
    }
    
    override fun navigateToTransactionDetail(transactionId: String) {
        ledgerNavigator.navigateToTransactionDetail(transactionId)
    }
    
    private fun getCurrentUserId(): String {
        // 在实际应用中，这应该从用户状态管理获取
        return "current_user_id"
    }
}