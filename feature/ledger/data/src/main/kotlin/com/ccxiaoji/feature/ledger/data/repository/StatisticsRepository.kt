package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.core.database.dao.TransactionDao
import com.ccxiaoji.core.database.dao.CategoryDao
import com.ccxiaoji.core.database.dao.AccountDao
import com.ccxiaoji.feature.ledger.api.DailyStatistics
import com.ccxiaoji.feature.ledger.api.PeriodStatistics
import com.ccxiaoji.feature.ledger.api.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统计相关的Repository
 * 从TransactionRepository中迁移统计功能
 */
@Singleton
class StatisticsRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao
) {
    /**
     * 获取今日收支统计
     */
    suspend fun getTodayStatistics(): DailyStatistics {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val startMillis = today.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        return DailyStatistics(
            income = income / 100.0,
            expense = expense / 100.0,
            balance = (income - expense) / 100.0
        )
    }
    
    /**
     * 获取本月支出总额
     */
    suspend fun getMonthlyExpense(year: Int, month: Int): Double {
        val startDate = LocalDate(year, month, 1)
        val endDate = startDate.plus(1, DateTimeUnit.MONTH)
        
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val expense = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        return expense / 100.0
    }
    
    /**
     * 获取指定日期范围的收支统计
     */
    suspend fun getStatisticsByDateRange(startDate: LocalDate, endDate: LocalDate): PeriodStatistics {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        val transactionCount = transactionDao.getTransactionCountByDateRange(getCurrentUserId(), startMillis, endMillis)
        
        return PeriodStatistics(
            totalIncome = income / 100.0,
            totalExpense = expense / 100.0,
            balance = (income - expense) / 100.0,
            transactionCount = transactionCount
        )
    }
    
    /**
     * 获取账户总余额
     */
    suspend fun getTotalBalance(): Double {
        val accounts = accountDao.getAccountsByUser(getCurrentUserId()).first()
        val totalBalance = accounts.sumOf { it.balanceCents }
        return totalBalance / 100.0
    }
    
    /**
     * 获取最近交易记录
     */
    fun getRecentTransactions(limit: Int): Flow<List<TransactionItem>> {
        return transactionDao.getTransactionsByUser(getCurrentUserId())
            .map { entities ->
                entities.take(limit).map { entity ->
                    val category = categoryDao.getCategoryById(entity.categoryId)
                    val account = accountDao.getAccountById(entity.accountId)
                    
                    TransactionItem(
                        id = entity.id,
                        amount = entity.amountCents / 100.0,
                        categoryName = category?.name ?: "未分类",
                        categoryIcon = category?.icon,
                        categoryColor = category?.color ?: "#999999",
                        accountName = account?.name ?: "未知账户",
                        note = entity.note,
                        date = Instant.fromEpochMilliseconds(entity.createdAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date
                    )
                }
            }
    }
    
    /**
     * 获取月度收支统计（用于图表）
     */
    suspend fun getMonthlyIncomeAndExpense(year: Int, month: Int): Pair<Double, Double> {
        val startDate = LocalDate(year, month, 1)
        val endDate = startDate.plus(1, DateTimeUnit.MONTH)
        
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        return (income / 100.0) to (expense / 100.0)
    }
    
    /**
     * 获取每日收支统计（用于趋势图）
     */
    suspend fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Pair<Double, Double>> {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val transactions = transactionDao.getTransactionsByDateRangeSync(getCurrentUserId(), startMillis, endMillis)
        
        return transactions.groupBy { transaction ->
            Instant.fromEpochMilliseconds(transaction.createdAt)
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .date
        }.mapValues { (_, dayTransactions) ->
            val income = dayTransactions.filter { 
                categoryDao.getCategoryById(it.categoryId)?.type == "INCOME" 
            }.sumOf { it.amountCents }
            
            val expense = dayTransactions.filter { 
                categoryDao.getCategoryById(it.categoryId)?.type == "EXPENSE" 
            }.sumOf { it.amountCents }
            
            (income / 100.0) to (expense / 100.0)
        }
    }
    
    /**
     * 计算储蓄率
     */
    suspend fun calculateSavingsRate(startDate: LocalDate, endDate: LocalDate): Float {
        val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endMillis = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        
        val income = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "INCOME") ?: 0
        val expense = transactionDao.getTotalByType(getCurrentUserId(), startMillis, endMillis, "EXPENSE") ?: 0
        
        return if (income > 0) {
            ((income - expense).toFloat() / income) * 100
        } else {
            0f
        }
    }
    
    private fun getCurrentUserId(): String {
        // In a real app, this would get the actual current user ID
        return "current_user_id"
    }
}