package com.ccxiaoji.feature.ledger.domain.usecase

import com.ccxiaoji.common.base.BaseResult
import com.ccxiaoji.feature.ledger.domain.model.*
import com.ccxiaoji.feature.ledger.domain.repository.LedgerRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.datetime.*
import javax.inject.Inject

/**
 * 记账簿筛选统计用例
 * 
 * 提供基于记账簿筛选的各种统计和查询功能，包括：
 * - 单个记账簿或多个记账簿的统计数据
 * - 分类统计、每日收支、顶级交易等
 * - 支持灵活的筛选条件
 */
class LedgerFilteredStatisticsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerRepository: LedgerRepository
) {
    
    /**
     * 获取记账簿的分类统计
     * 
     * @param ledgerFilter 记账簿筛选条件
     * @param categoryType 分类类型（INCOME/EXPENSE），null表示支出
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    suspend fun getCategoryStatistics(
        ledgerFilter: LedgerFilter,
        categoryType: String? = null,
        startDate: LocalDate,
        endDate: LocalDate
    ): BaseResult<List<CategoryStatistic>> {
        return try {
            val startMillis = startDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            val endMillis = endDate.plus(1, kotlinx.datetime.DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
            
            when (ledgerFilter) {
                is LedgerFilter.Single -> {
                    transactionRepository.getCategoryStatisticsByLedger(
                        ledgerId = ledgerFilter.ledgerId,
                        categoryType = categoryType,
                        startDate = startMillis,
                        endDate = endMillis
                    )
                }
                is LedgerFilter.Multiple -> {
                    transactionRepository.getCategoryStatisticsByLedgers(
                        ledgerIds = ledgerFilter.ledgerIds,
                        categoryType = categoryType,
                        startDate = startMillis,
                        endDate = endMillis
                    )
                }
                is LedgerFilter.All -> {
                    transactionRepository.getCategoryStatistics(
                        categoryType = categoryType,
                        startDate = startMillis,
                        endDate = endMillis
                    )
                }
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取分类统计失败: ${e.message}"))
        }
    }
    
    /**
     * 获取每日收支统计
     */
    suspend fun getDailyTotals(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): BaseResult<Map<LocalDate, Pair<Int, Int>>> {
        return try {
            when (ledgerFilter) {
                is LedgerFilter.Single -> {
                    transactionRepository.getDailyTotalsByLedger(
                        ledgerId = ledgerFilter.ledgerId,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                is LedgerFilter.Multiple -> {
                    transactionRepository.getDailyTotalsByLedgers(
                        ledgerIds = ledgerFilter.ledgerIds,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                is LedgerFilter.All -> {
                    transactionRepository.getDailyTotals(
                        startDate = startDate,
                        endDate = endDate
                    )
                }
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取每日统计失败: ${e.message}"))
        }
    }
    
    /**
     * 获取金额最大的交易记录
     */
    suspend fun getTopTransactions(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate,
        type: String,
        limit: Int = 10
    ): BaseResult<List<Transaction>> {
        return try {
            when (ledgerFilter) {
                is LedgerFilter.Single -> {
                    transactionRepository.getTopTransactionsByLedger(
                        ledgerId = ledgerFilter.ledgerId,
                        startDate = startDate,
                        endDate = endDate,
                        type = type,
                        limit = limit
                    )
                }
                is LedgerFilter.Multiple -> {
                    transactionRepository.getTopTransactionsByLedgers(
                        ledgerIds = ledgerFilter.ledgerIds,
                        startDate = startDate,
                        endDate = endDate,
                        type = type,
                        limit = limit
                    )
                }
                is LedgerFilter.All -> {
                    transactionRepository.getTopTransactions(
                        startDate = startDate,
                        endDate = endDate,
                        type = type,
                        limit = limit
                    )
                }
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取顶级交易失败: ${e.message}"))
        }
    }
    
    /**
     * 计算储蓄率
     */
    suspend fun calculateSavingsRate(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): BaseResult<Float> {
        return try {
            when (ledgerFilter) {
                is LedgerFilter.Single -> {
                    transactionRepository.calculateSavingsRateByLedger(
                        ledgerId = ledgerFilter.ledgerId,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                is LedgerFilter.Multiple -> {
                    transactionRepository.calculateSavingsRateByLedgers(
                        ledgerIds = ledgerFilter.ledgerIds,
                        startDate = startDate,
                        endDate = endDate
                    )
                }
                is LedgerFilter.All -> {
                    transactionRepository.calculateSavingsRate(
                        startDate = startDate,
                        endDate = endDate
                    )
                }
            }
        } catch (e: Exception) {
            BaseResult.Error(Exception("计算储蓄率失败: ${e.message}"))
        }
    }
    
    /**
     * 获取分页交易记录
     */
    fun getTransactionsPaginated(
        ledgerFilter: LedgerFilter,
        offset: Int,
        limit: Int,
        accountId: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<BaseResult<Pair<List<Transaction>, Int>>> {
        return when (ledgerFilter) {
            is LedgerFilter.Single -> {
                transactionRepository.getTransactionsPaginatedByLedger(
                    ledgerId = ledgerFilter.ledgerId,
                    offset = offset,
                    limit = limit,
                    accountId = accountId,
                    startDate = startDate,
                    endDate = endDate
                )
            }
            is LedgerFilter.Multiple -> {
                transactionRepository.getTransactionsPaginatedByLedgers(
                    ledgerIds = ledgerFilter.ledgerIds,
                    offset = offset,
                    limit = limit,
                    accountId = accountId,
                    startDate = startDate,
                    endDate = endDate
                )
            }
            is LedgerFilter.All -> {
                transactionRepository.getTransactionsPaginated(
                    offset = offset,
                    limit = limit,
                    accountId = accountId,
                    startDate = startDate,
                    endDate = endDate
                )
            }
        }
    }
    
    /**
     * 获取记账簿的综合统计报告
     */
    suspend fun getComprehensiveStats(
        ledgerFilter: LedgerFilter,
        startDate: LocalDate,
        endDate: LocalDate
    ): BaseResult<LedgerComprehensiveStats> {
        return try {
            // 获取基本统计数据
            val categoryStatsResult = getCategoryStatistics(ledgerFilter, "EXPENSE", startDate, endDate)
            val dailyTotalsResult = getDailyTotals(ledgerFilter, startDate, endDate)
            val savingsRateResult = calculateSavingsRate(ledgerFilter, startDate, endDate)
            val topExpensesResult = getTopTransactions(ledgerFilter, startDate, endDate, "EXPENSE", 5)
            val topIncomesResult = getTopTransactions(ledgerFilter, startDate, endDate, "INCOME", 5)
            
            // 检查是否有错误
            if (categoryStatsResult is BaseResult.Error) return categoryStatsResult
            if (dailyTotalsResult is BaseResult.Error) return dailyTotalsResult
            if (savingsRateResult is BaseResult.Error) return savingsRateResult
            if (topExpensesResult is BaseResult.Error) return topExpensesResult
            if (topIncomesResult is BaseResult.Error) return topIncomesResult
            
            val categoryStats = (categoryStatsResult as BaseResult.Success).data
            val dailyTotals = (dailyTotalsResult as BaseResult.Success).data
            val savingsRate = (savingsRateResult as BaseResult.Success).data
            val topExpenses = (topExpensesResult as BaseResult.Success).data
            val topIncomes = (topIncomesResult as BaseResult.Success).data
            
            // 计算汇总数据
            val totalIncome = dailyTotals.values.sumOf { it.first.toLong() }
            val totalExpense = dailyTotals.values.sumOf { it.second.toLong() }
            val netAmount = totalIncome - totalExpense
            val transactionCount = categoryStats.sumOf { it.transactionCount }
            
            // 获取记账簿信息
            val ledgerInfo = when (ledgerFilter) {
                is LedgerFilter.Single -> {
                    val result = ledgerRepository.getLedgerById(ledgerFilter.ledgerId)
                    if (result is BaseResult.Success) listOf(result.data) else emptyList()
                }
                is LedgerFilter.Multiple -> {
                    ledgerFilter.ledgerIds.mapNotNull { ledgerId ->
                        val result = ledgerRepository.getLedgerById(ledgerId)
                        if (result is BaseResult.Success) result.data else null
                    }
                }
                is LedgerFilter.All -> emptyList()
            }
            
            BaseResult.Success(
                LedgerComprehensiveStats(
                    ledgers = ledgerInfo,
                    period = StatsPeriod(startDate, endDate),
                    summary = StatsSummary(
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        netAmount = netAmount,
                        transactionCount = transactionCount,
                        savingsRate = savingsRate
                    ),
                    categoryStats = categoryStats,
                    dailyTotals = dailyTotals,
                    topExpenses = topExpenses,
                    topIncomes = topIncomes
                )
            )
        } catch (e: Exception) {
            BaseResult.Error(Exception("获取综合统计报告失败: ${e.message}"))
        }
    }
}

/**
 * 记账簿筛选条件
 */
sealed class LedgerFilter {
    /**
     * 单个记账簿
     */
    data class Single(val ledgerId: String) : LedgerFilter()
    
    /**
     * 多个记账簿
     */
    data class Multiple(val ledgerIds: List<String>) : LedgerFilter()
    
    /**
     * 所有记账簿
     */
    object All : LedgerFilter()
}

/**
 * 记账簿综合统计数据
 */
data class LedgerComprehensiveStats(
    val ledgers: List<Ledger>,
    val period: StatsPeriod,
    val summary: StatsSummary,
    val categoryStats: List<CategoryStatistic>,
    val dailyTotals: Map<LocalDate, Pair<Int, Int>>,
    val topExpenses: List<Transaction>,
    val topIncomes: List<Transaction>
)

/**
 * 统计时间段
 */
data class StatsPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * 统计汇总信息
 */
data class StatsSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val netAmount: Long,
    val transactionCount: Int,
    val savingsRate: Float
)