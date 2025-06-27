package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

/**
 * 处理交易过滤和分组逻辑的ViewModel
 */
@HiltViewModel
class FilterViewModel @Inject constructor() : ViewModel() {
    
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()
    
    private val _groupedTransactions = MutableStateFlow<List<TransactionGroup>>(emptyList())
    val groupedTransactions: StateFlow<List<TransactionGroup>> = _groupedTransactions.asStateFlow()
    
    /**
     * 更新过滤器
     */
    fun updateFilter(filter: TransactionFilter) {
        _filterState.update { it.copy(activeFilter = filter) }
    }
    
    /**
     * 清除过滤器
     */
    fun clearFilter() {
        _filterState.update { it.copy(activeFilter = TransactionFilter()) }
    }
    
    /**
     * 应用预设过滤器
     */
    fun applyPresetFilter(preset: FilterPreset) {
        _filterState.update { it.copy(activeFilter = preset.filter) }
    }
    
    /**
     * 获取预设过滤器列表
     */
    fun getFilterPresets(): List<FilterPreset> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        
        return listOf(
            FilterPreset(
                id = "today",
                name = "今日交易",
                icon = "today",
                filter = TransactionFilter(
                    dateRange = DateRange(
                        startDate = today.toJavaLocalDate(),
                        endDate = today.toJavaLocalDate()
                    )
                )
            ),
            FilterPreset(
                id = "this_week",
                name = "本周交易",
                icon = "date_range",
                filter = TransactionFilter(
                    dateRange = DateRange(
                        startDate = today.minus(today.dayOfWeek.value - 1, DateTimeUnit.DAY).toJavaLocalDate(),
                        endDate = today.toJavaLocalDate()
                    )
                )
            ),
            FilterPreset(
                id = "this_month",
                name = "本月交易",
                icon = "calendar_month",
                filter = TransactionFilter(
                    dateRange = DateRange(
                        startDate = LocalDate.of(today.year, today.monthNumber, 1),
                        endDate = today.toJavaLocalDate()
                    )
                )
            ),
            FilterPreset(
                id = "income_only",
                name = "仅收入",
                icon = "trending_up",
                filter = TransactionFilter(
                    transactionType = TransactionType.INCOME
                )
            ),
            FilterPreset(
                id = "expense_only",
                name = "仅支出",
                icon = "trending_down",
                filter = TransactionFilter(
                    transactionType = TransactionType.EXPENSE
                )
            ),
            FilterPreset(
                id = "large_amount",
                name = "大额交易",
                icon = "attach_money",
                filter = TransactionFilter(
                    minAmount = 500.0
                )
            )
        )
    }
    
    /**
     * 设置分组模式
     */
    fun setGroupingMode(mode: GroupingMode) {
        _filterState.update { it.copy(groupingMode = mode) }
    }
    
    /**
     * 应用过滤器到交易列表
     */
    fun applyFilter(transactions: List<Transaction>): List<Transaction> {
        val filter = _filterState.value.activeFilter
        
        return transactions.filter { transaction ->
            // 按交易类型过滤
            val typeMatch = when (filter.transactionType) {
                TransactionType.ALL -> true
                TransactionType.INCOME -> transaction.categoryDetails?.type == "INCOME"
                TransactionType.EXPENSE -> transaction.categoryDetails?.type == "EXPENSE"
            }
            
            // 按分类过滤
            val categoryMatch = if (filter.categoryIds.isEmpty()) {
                true
            } else {
                filter.categoryIds.contains(transaction.categoryId)
            }
            
            // 按金额范围过滤
            val amountMatch = (filter.minAmount == null || transaction.amountYuan >= filter.minAmount) &&
                    (filter.maxAmount == null || transaction.amountYuan <= filter.maxAmount)
            
            // 按日期范围过滤
            val dateMatch = if (filter.dateRange == null) {
                true
            } else {
                val transactionDate = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                val kotlinDate = kotlinx.datetime.LocalDate(
                    transactionDate.year,
                    transactionDate.monthNumber,
                    transactionDate.dayOfMonth
                )
                val startDate = kotlinx.datetime.LocalDate(
                    filter.dateRange.startDate.year,
                    filter.dateRange.startDate.monthValue,
                    filter.dateRange.startDate.dayOfMonth
                )
                val endDate = kotlinx.datetime.LocalDate(
                    filter.dateRange.endDate.year,
                    filter.dateRange.endDate.monthValue,
                    filter.dateRange.endDate.dayOfMonth
                )
                kotlinDate >= startDate && kotlinDate <= endDate
            }
            
            // 按账户过滤
            val accountMatch = filter.accountId == null || transaction.accountId == filter.accountId
            
            // 按关键词过滤（在备注中搜索）
            val keywordMatch = if (filter.keyword.isNullOrBlank()) {
                true
            } else {
                transaction.note?.contains(filter.keyword, ignoreCase = true) == true
            }
            
            typeMatch && categoryMatch && amountMatch && dateMatch && accountMatch && keywordMatch
        }
    }
    
    /**
     * 对交易进行分组
     */
    fun groupTransactions(transactions: List<Transaction>) {
        viewModelScope.launch {
            val groups = when (_filterState.value.groupingMode) {
                GroupingMode.NONE -> listOf(
                    TransactionGroup(
                        id = "all",
                        title = "所有交易",
                        transactions = transactions,
                        totalIncome = transactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                        totalExpense = transactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                    )
                )
                GroupingMode.DAY -> groupByDay(transactions)
                GroupingMode.WEEK -> groupByWeek(transactions)
                GroupingMode.MONTH -> groupByMonth(transactions)
                GroupingMode.YEAR -> groupByYear(transactions)
            }
            _groupedTransactions.value = groups
        }
    }
    
    /**
     * 按天分组
     */
    private fun groupByDay(transactions: List<Transaction>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val today = now.date
        val yesterday = today.minus(1, DateTimeUnit.DAY)
        
        return transactions
            .groupBy { transaction ->
                transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            .map { (date, dayTransactions) ->
                val title = when (date) {
                    today -> "今天"
                    yesterday -> "昨天"
                    else -> {
                        val javaDate = date.toJavaLocalDate()
                        "${javaDate.monthValue}月${javaDate.dayOfMonth}日"
                    }
                }
                
                TransactionGroup(
                    id = date.toString(),
                    title = title,
                    subtitle = if (date != today && date != yesterday) {
                        getWeekdayName(date.toJavaLocalDate().dayOfWeek.value)
                    } else null,
                    transactions = dayTransactions.sortedByDescending { it.createdAt },
                    totalIncome = dayTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = dayTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    /**
     * 按周分组
     */
    private fun groupByWeek(transactions: List<Transaction>): List<TransactionGroup> {
        // 简化的按周分组实现
        return groupByDay(transactions) // 暂时使用按天分组
    }
    
    /**
     * 按月分组
     */
    private fun groupByMonth(transactions: List<Transaction>): List<TransactionGroup> {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentMonth = now.monthNumber
        val currentYear = now.year
        
        return transactions
            .groupBy { transaction ->
                val date = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date
                "${date.year}-${date.monthNumber}"
            }
            .map { (monthKey, monthTransactions) ->
                val parts = monthKey.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                
                val title = when {
                    year == currentYear && month == currentMonth -> "本月"
                    year == currentYear && month == currentMonth - 1 -> "上月"
                    year == currentYear -> "${month}月"
                    else -> "${year}年${month}月"
                }
                
                TransactionGroup(
                    id = monthKey,
                    title = title,
                    transactions = monthTransactions.sortedByDescending { it.createdAt },
                    totalIncome = monthTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = monthTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    /**
     * 按年分组
     */
    private fun groupByYear(transactions: List<Transaction>): List<TransactionGroup> {
        return transactions
            .groupBy { transaction ->
                transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).year
            }
            .map { (year, yearTransactions) ->
                TransactionGroup(
                    id = year.toString(),
                    title = "${year}年",
                    transactions = yearTransactions.sortedByDescending { it.createdAt },
                    totalIncome = yearTransactions.filter { it.categoryDetails?.type == "INCOME" }.sumOf { it.amountCents },
                    totalExpense = yearTransactions.filter { it.categoryDetails?.type == "EXPENSE" }.sumOf { it.amountCents }
                )
            }
            .sortedByDescending { it.id }
    }
    
    /**
     * 获取星期几的名称
     */
    private fun getWeekdayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "周一"
            2 -> "周二"
            3 -> "周三"
            4 -> "周四"
            5 -> "周五"
            6 -> "周六"
            7 -> "周日"
            else -> ""
        }
    }
}

/**
 * 过滤器状态
 */
data class FilterState(
    val activeFilter: TransactionFilter = TransactionFilter(),
    val groupingMode: GroupingMode = GroupingMode.NONE
)

/**
 * 交易过滤器
 */
data class TransactionFilter(
    val categoryIds: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val transactionType: TransactionType = TransactionType.ALL,
    val accountId: String? = null,
    val keyword: String? = null,  // 备注关键词
    val tags: Set<String> = emptySet()  // 标签筛选（预留）
)

/**
 * 交易类型
 */
enum class TransactionType {
    ALL, INCOME, EXPENSE
}

/**
 * 日期范围
 */
data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)

/**
 * 分组模式
 */
enum class GroupingMode {
    NONE, DAY, WEEK, MONTH, YEAR
}

/**
 * 交易分组
 */
data class TransactionGroup(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val transactions: List<Transaction>,
    val totalIncome: Int,
    val totalExpense: Int
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
 * 过滤器预设
 */
data class FilterPreset(
    val id: String,
    val name: String,
    val icon: String,
    val filter: TransactionFilter
)