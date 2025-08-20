package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.LedgerWithStats
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 获取记账簿统计数据用例
 * 
 * 提供记账簿的各种统计信息，包括交易总数、收支统计、月度数据等
 */
class GetLedgerStatsUseCase @Inject constructor(
    private val ledgerRepository: LedgerRepository,
    private val transactionRepository: TransactionRepository
) {
    
    /**
     * 获取记账簿的基本统计数据
     */
    fun getLedgerWithStats(userId: String): Flow<List<LedgerWithStats>> {
        return ledgerRepository.getUserLedgersWithStats(userId)
    }
    
    /**
     * 获取单个记账簿的详细统计数据
     */
    suspend fun getLedgerDetailStats(
        ledgerId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): BaseResult<LedgerDetailStats> {
        return try {
            val ledgerResult = ledgerRepository.getLedgerById(ledgerId)
            if (ledgerResult is BaseResult.Error) {
                return ledgerResult
            }
            
            val ledger = (ledgerResult as BaseResult.Success).data
            val actualStartDate = startDate ?: getDefaultStartDate()
            val actualEndDate = endDate ?: getDefaultEndDate()
            
            // 获取指定时间范围内的统计数据
            val transactions = transactionRepository.getTransactionsByLedgerAndDateRange(
                ledgerId = ledgerId,
                startDate = actualStartDate,
                endDate = actualEndDate
            ).first()
            
            // 计算统计数据
            val stats = calculateDetailStats(transactions)
            
            BaseResult.Success(
                LedgerDetailStats(
                    ledger = ledger,
                    totalTransactions = stats.totalCount,
                    totalIncome = stats.totalIncome,
                    totalExpense = stats.totalExpense,
                    netAmount = stats.totalIncome - stats.totalExpense,
                    averageTransactionAmount = if (stats.totalCount > 0) 
                        (stats.totalIncome + stats.totalExpense) / stats.totalCount else 0L,
                    startDate = actualStartDate,
                    endDate = actualEndDate,
                    dailyStats = stats.dailyStats,
                    categoryStats = stats.categoryStats
                )
            )
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取记账簿统计数据失败: ${e.message}"))
        }
    }
    
    /**
     * 获取记账簿的月度统计数据
     */
    suspend fun getLedgerMonthlyStats(
        ledgerId: String,
        year: Int,
        month: Int
    ): BaseResult<MonthlyLedgerStats> {
        return try {
            val ledgerResult = ledgerRepository.getLedgerById(ledgerId)
            if (ledgerResult is BaseResult.Error) {
                return ledgerResult
            }
            
            val ledger = (ledgerResult as BaseResult.Success).data
            
            // 获取月度数据
            val monthlyResult = transactionRepository.getMonthlyIncomesAndExpensesByLedger(ledgerId, year, month)
            if (monthlyResult is BaseResult.Error) {
                return monthlyResult
            }
            
            val (income, expense) = (monthlyResult as BaseResult.Success).data
            
            BaseResult.Success(
                MonthlyLedgerStats(
                    ledger = ledger,
                    year = year,
                    month = month,
                    totalIncome = income.toLong(),
                    totalExpense = expense.toLong(),
                    netAmount = income.toLong() - expense.toLong(),
                    transactionCount = 0 // TODO: 实现交易数量统计
                )
            )
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取月度统计数据失败: ${e.message}"))
        }
    }
    
    /**
     * 比较多个记账簿的统计数据
     */
    suspend fun compareLedgers(
        ledgerIds: List<String>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): BaseResult<List<LedgerComparisonStats>> {
        return try {
            val results = mutableListOf<LedgerComparisonStats>()
            
            for (ledgerId in ledgerIds) {
                val detailStatsResult = getLedgerDetailStats(ledgerId, startDate, endDate)
                if (detailStatsResult is BaseResult.Success) {
                    val detailStats = detailStatsResult.data
                    results.add(
                        LedgerComparisonStats(
                            ledger = detailStats.ledger,
                            totalIncome = detailStats.totalIncome,
                            totalExpense = detailStats.totalExpense,
                            netAmount = detailStats.netAmount,
                            transactionCount = detailStats.totalTransactions
                        )
                    )
                }
            }
            
            BaseResult.Success(results)
        } catch (e: Exception) {
            BaseResult.Error(Exception("比较记账簿数据失败: ${e.message}"))
        }
    }
    
    /**
     * 获取默认开始日期（当前月第一天）
     */
    private fun getDefaultStartDate(): LocalDate {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return LocalDate(today.year, today.month, 1)
    }
    
    /**
     * 获取默认结束日期（今天）
     */
    private fun getDefaultEndDate(): LocalDate {
        return Clock.System.todayIn(TimeZone.currentSystemDefault())
    }
    
    /**
     * 计算详细统计数据
     */
    private fun calculateDetailStats(transactions: List<com.ccxiaoji.feature.ledger.domain.model.Transaction>): DetailedStats {
        val totalIncome = transactions.filter { it.amountCents > 0 }.sumOf { it.amountCents.toLong() }
        val totalExpense = transactions.filter { it.amountCents < 0 }.sumOf { kotlin.math.abs(it.amountCents.toLong()) }
        
        // TODO: 实现每日统计和分类统计
        return DetailedStats(
            totalCount = transactions.size,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            dailyStats = emptyList(),
            categoryStats = emptyList()
        )
    }
}

/**
 * 记账簿详细统计数据
 */
data class LedgerDetailStats(
    val ledger: com.ccxiaoji.feature.ledger.domain.model.Ledger,
    val totalTransactions: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val netAmount: Long,
    val averageTransactionAmount: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val dailyStats: List<DailyStats>,
    val categoryStats: List<CategoryStats>
)

/**
 * 月度记账簿统计数据
 */
data class MonthlyLedgerStats(
    val ledger: com.ccxiaoji.feature.ledger.domain.model.Ledger,
    val year: Int,
    val month: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val netAmount: Long,
    val transactionCount: Int
)

/**
 * 记账簿比较统计数据
 */
data class LedgerComparisonStats(
    val ledger: com.ccxiaoji.feature.ledger.domain.model.Ledger,
    val totalIncome: Long,
    val totalExpense: Long,
    val netAmount: Long,
    val transactionCount: Int
)

/**
 * 详细统计数据（内部使用）
 */
private data class DetailedStats(
    val totalCount: Int,
    val totalIncome: Long,
    val totalExpense: Long,
    val dailyStats: List<DailyStats>,
    val categoryStats: List<CategoryStats>
)

/**
 * 每日统计数据
 */
data class DailyStats(
    val date: LocalDate,
    val income: Long,
    val expense: Long,
    val transactionCount: Int
)

/**
 * 分类统计数据
 */
data class CategoryStats(
    val categoryId: String,
    val categoryName: String,
    val amount: Long,
    val transactionCount: Int
)