package com.ccxiaoji.feature.ledger.domain.model

import kotlinx.datetime.LocalDate

/**
 * 统计相关的领域模型
 */

/**
 * 月度统计
 */
data class MonthlyStatistics(
    val year: Int,
    val month: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val budgetAmount: Double,
    val budgetUsage: Double,
    val categoryBreakdown: List<CategoryStatistics>
)

/**
 * 分类统计
 */
data class CategoryStatistics(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

/**
 * 账户统计
 */
data class AccountStatistics(
    val accountId: String,
    val accountName: String,
    val balance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double
)

/**
 * 趋势数据点
 */
data class TrendDataPoint(
    val date: LocalDate,
    val income: Double,
    val expense: Double,
    val balance: Double
)

/**
 * 统计时间范围
 */
enum class StatisticsPeriod {
    WEEK,
    MONTH,
    QUARTER,
    YEAR,
    CUSTOM
}