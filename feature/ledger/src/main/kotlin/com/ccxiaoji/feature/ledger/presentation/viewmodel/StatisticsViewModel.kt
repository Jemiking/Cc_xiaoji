package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.CategoryStatistic
import com.ccxiaoji.feature.ledger.domain.model.Ledger
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerFilteredStatisticsUseCase
import com.ccxiaoji.feature.ledger.domain.usecase.LedgerFilter
import com.ccxiaoji.feature.ledger.domain.usecase.ManageLedgerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.datetime.*
import javax.inject.Inject
import android.util.Log

// Extension function to convert java.time.LocalDate to kotlinx.datetime.LocalDate
private fun java.time.LocalDate.toKotlinLocalDate(): kotlinx.datetime.LocalDate {
    return kotlinx.datetime.LocalDate(this.year, this.monthValue, this.dayOfMonth)
}

// Extension function to convert kotlinx.datetime.LocalDate to java.time.LocalDate
private fun kotlinx.datetime.LocalDate.toJavaLocalDate(): java.time.LocalDate {
    return java.time.LocalDate.of(this.year, this.monthNumber, this.dayOfMonth)
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val ledgerFilteredStatisticsUseCase: LedgerFilteredStatisticsUseCase,
    private val manageLedgerUseCase: ManageLedgerUseCase
) : ViewModel() {
    
    companion object {
        private const val TAG = "StatisticsViewModel"
        private const val ENABLE_DEBUG_LOGS = false // 生产环境禁用调试日志
    }
    
    // 错误类型枚举
    enum class StatisticsErrorType {
        DATA_LOADING_ERROR,
        CALCULATION_ERROR,
        DATE_RANGE_ERROR,
        UNKNOWN_ERROR
    }
    
    // 详细错误信息数据类
    data class StatisticsError(
        val type: StatisticsErrorType,
        override val message: String,
        override val cause: Throwable? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : Exception(message, cause)
    
    // 调试日志辅助方法
    private fun debugLog(message: String, throwable: Throwable? = null) {
        if (ENABLE_DEBUG_LOGS) {
            if (throwable != null) {
                android.util.Log.d(TAG, message, throwable)
            } else {
                android.util.Log.d(TAG, message)
            }
        }
    }
    
    private fun errorLog(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e(TAG, message, throwable)
        } else {
            android.util.Log.e(TAG, message)
        }
    }
    
    /**
     * 统一错误处理方法
     */
    private fun handleStatisticsError(error: StatisticsError) {
        errorLog("发生${error.type}错误: ${error.message}", error.cause)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = when (error.type) {
                StatisticsErrorType.DATA_LOADING_ERROR -> "统计数据加载失败，请检查网络连接"
                StatisticsErrorType.CALCULATION_ERROR -> "统计计算失败，请稍后重试"
                StatisticsErrorType.DATE_RANGE_ERROR -> "日期范围无效，请检查日期设置"
                StatisticsErrorType.UNKNOWN_ERROR -> "发生未知错误: ${error.message}"
            }
        )
    }
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        debugLog("StatisticsViewModel 初始化开始")
        
        // 🔍 监控UI状态变化
        viewModelScope.launch {
            uiState.collect { state ->
                debugLog("📱 UI状态变化监控 - selectedPeriod: ${state.selectedPeriod}, showDateRangePicker: ${state.showDateRangePicker}, customStartDate: ${state.customStartDate}, customEndDate: ${state.customEndDate}")
            }
        }
        
        try {
            loadLedgers()
            loadStatistics(TimePeriod.THIS_MONTH)
            debugLog("StatisticsViewModel 初始化完成")
        } catch (e: Exception) {
            val error = StatisticsError(
                type = StatisticsErrorType.UNKNOWN_ERROR,
                message = "初始化失败: ${e.message}",
                cause = e
            )
            handleStatisticsError(error)
        }
    }
    
    fun selectTimePeriod(period: TimePeriod) {
        debugLog("🔄 切换时间周期: $period")
        debugLog("🔄 当前UI状态 - selectedPeriod: ${_uiState.value.selectedPeriod}")
        debugLog("🔄 新选择的周期: $period")
        
        // 检查是否点击了自定义选项
        if (period == TimePeriod.CUSTOM) {
            debugLog("⚡ 用户点击了自定义分析选项")
            debugLog("⚡ 当前自定义日期范围: ${_uiState.value.customStartDate} 到 ${_uiState.value.customEndDate}")
            
            // 🔧 修复：每次点击自定义都显示日期选择器，允许用户重新选择日期
            debugLog("📅 显示日期选择器，允许用户${if (_uiState.value.customStartDate != null) "重新" else ""}选择日期范围")
            _uiState.value = _uiState.value.copy(showDateRangePicker = true)
            debugLog("📅 已设置显示日期选择器状态为true")
            return // 暂停执行，等待用户选择日期
        }
        
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadStatistics(period)
    }
    
    // 🔍 添加手动重新加载方法
    fun refreshStatistics() {
        debugLog("🔄 手动刷新统计数据")
        loadStatistics(_uiState.value.selectedPeriod)
    }
    
    // 记账簿筛选相关方法
    fun selectLedgerFilter(ledgerFilter: LedgerFilter) {
        debugLog("🔄 切换记账簿筛选: $ledgerFilter")
        _uiState.value = _uiState.value.copy(
            selectedLedgerFilter = ledgerFilter,
            showLedgerSelector = false
        )
        loadStatistics(_uiState.value.selectedPeriod)
    }
    
    fun showLedgerSelector() {
        _uiState.value = _uiState.value.copy(showLedgerSelector = true)
    }
    
    fun hideLedgerSelector() {
        _uiState.value = _uiState.value.copy(showLedgerSelector = false)
    }
    
    private fun loadLedgers() {
        viewModelScope.launch {
            try {
                // TODO: 暂时使用默认用户ID，后续应通过参数传递
                val currentUserId = "default_user"
                manageLedgerUseCase.getUserLedgers(currentUserId).collect { ledgers ->
                    _uiState.value = _uiState.value.copy(availableLedgers = ledgers)
                }
            } catch (e: Exception) {
                debugLog("加载记账簿列表失败: ${e.message}", e)
            }
        }
    }
    
    fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        debugLog("📅 设置自定义日期范围: $startDate 到 $endDate")
        _uiState.value = _uiState.value.copy(
            customStartDate = startDate,
            customEndDate = endDate,
            selectedPeriod = TimePeriod.CUSTOM,
            showDateRangePicker = false // 隐藏日期选择器
        )
        loadStatistics(TimePeriod.CUSTOM)
    }
    
    // 🆕 隐藏日期选择器
    fun hideDateRangePicker() {
        debugLog("📅 隐藏日期选择器")
        _uiState.value = _uiState.value.copy(showDateRangePicker = false)
        
        // 如果用户取消了自定义选择，恢复到之前的时间周期
        if (_uiState.value.customStartDate == null && _uiState.value.selectedPeriod == TimePeriod.CUSTOM) {
            debugLog("📅 用户取消自定义选择，恢复到本月模式")
            _uiState.value = _uiState.value.copy(selectedPeriod = TimePeriod.THIS_MONTH)
            loadStatistics(TimePeriod.THIS_MONTH)
        }
    }
    
    private fun loadStatistics(period: TimePeriod) {
        debugLog("开始加载统计数据，时间周期: $period")
        val startTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val (startDate, endDate) = getDateRange(period)
                debugLog("统计时间范围: $startDate 到 $endDate")
                
                // 验证日期范围
                if (startDate > endDate) {
                    throw IllegalArgumentException("开始日期不能晚于结束日期")
                }
                
                // 转换时间戳
                val startTimestamp = startDate
                    .atStartOfDayIn(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()
                val endTimestamp = endDate
                    .atTime(23, 59, 59)
                    .toInstant(TimeZone.currentSystemDefault())
                    .toEpochMilliseconds()
                    
                debugLog("🕰️ 原始日期: $startDate 到 $endDate")
                debugLog("🕰️ 转换后时间戳: $startTimestamp 到 $endTimestamp")
                
                debugLog("时间戳范围: $startTimestamp 到 $endTimestamp")
                
                // 🔍 添加时间戳验证
                val startDateFormatted = java.time.Instant.ofEpochMilli(startTimestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                val endDateFormatted = java.time.Instant.ofEpochMilli(endTimestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                debugLog("时间戳验证 - 开始: $startDateFormatted, 结束: $endDateFormatted")
                
                // 🔍 简化的数据验证（避免复杂Flow操作）
                debugLog("🔍 将在后续查询中验证交易数据存在性")
                
                // 获取当前的记账簿筛选条件
                val ledgerFilter = _uiState.value.selectedLedgerFilter
                debugLog("🔍 当前记账簿筛选: $ledgerFilter")
                
                // 🚀 使用记账簿筛选的统计数据加载
                val dailyTotalsJob = async { 
                    measureStatisticsTime("每日汇总数据") {
                        ledgerFilteredStatisticsUseCase.getDailyTotals(ledgerFilter, startDate, endDate).getOrThrow()
                    }
                }
                
                val expenseCategoriesJob = async {
                    measureStatisticsTime("支出分类统计") {
                        ledgerFilteredStatisticsUseCase.getCategoryStatistics(ledgerFilter, "EXPENSE", startDate, endDate).getOrThrow()
                    }
                }
                
                val incomeCategoriesJob = async {
                    measureStatisticsTime("收入分类统计") {
                        ledgerFilteredStatisticsUseCase.getCategoryStatistics(ledgerFilter, "INCOME", startDate, endDate).getOrThrow()
                    }
                }
                
                val topExpensesJob = async {
                    measureStatisticsTime("支出排行数据") {
                        ledgerFilteredStatisticsUseCase.getTopTransactions(ledgerFilter, startDate, endDate, "EXPENSE", 10).getOrThrow()
                    }
                }
                
                val topIncomesJob = async {
                    measureStatisticsTime("收入排行数据") {
                        ledgerFilteredStatisticsUseCase.getTopTransactions(ledgerFilter, startDate, endDate, "INCOME", 10).getOrThrow()
                    }
                }
                
                val savingsRateJob = async {
                    measureStatisticsTime("储蓄率计算") {
                        ledgerFilteredStatisticsUseCase.calculateSavingsRate(ledgerFilter, startDate, endDate).getOrThrow()
                    }
                }
                
                // 等待所有数据加载完成
                val dailyTotals = dailyTotalsJob.await()
                val expenseCategories = expenseCategoriesJob.await()
                val incomeCategories = incomeCategoriesJob.await()
                val topExpenses = topExpensesJob.await()
                val topIncomes = topIncomesJob.await()
                val savingsRate = savingsRateJob.await()
                
                // 🔍 详细记录查询结果
                debugLog("📊 查询结果统计:")
                debugLog("📊 每日汇总数量: ${dailyTotals.size}")
                debugLog("📊 支出分类数量: ${expenseCategories.size}")
                debugLog("📊 收入分类数量: ${incomeCategories.size}")
                debugLog("📊 支出排行数量: ${topExpenses.size}")
                debugLog("📊 收入排行数量: ${topIncomes.size}")
                debugLog("📊 储蓄率: $savingsRate")
                
                if (expenseCategories.isNotEmpty()) {
                    expenseCategories.take(3).forEach { category ->
                        debugLog("📊 支出分类示例: ${category.categoryName}, 金额: ${category.totalAmount}")
                    }
                } else {
                    debugLog("📊 支出分类为空")
                }
                
                if (incomeCategories.isNotEmpty()) {
                    incomeCategories.take(3).forEach { category ->
                        debugLog("📊 收入分类示例: ${category.categoryName}, 金额: ${category.totalAmount}")
                    }
                } else {
                    debugLog("📊 收入分类为空")
                }
                
                if (topExpenses.isNotEmpty()) {
                    topExpenses.take(3).forEach { transaction ->
                        debugLog("📊 支出排行示例: ID=${transaction.id}, 金额: ${transaction.amountCents}")
                    }
                } else {
                    debugLog("📊 支出排行为空")
                }
                
                if (topIncomes.isNotEmpty()) {
                    topIncomes.take(3).forEach { transaction ->
                        debugLog("📊 收入排行示例: ID=${transaction.id}, 金额: ${transaction.amountCents}")
                    }
                } else {
                    debugLog("📊 收入排行为空")
                }
                
                // 计算汇总数据
                val totalIncome = incomeCategories.sumOf { it.totalAmount }
                val totalExpense = expenseCategories.sumOf { it.totalAmount }
                val balance = totalIncome - totalExpense
                
                debugLog("📊 统计数据汇总 - 收入: $totalIncome, 支出: $totalExpense, 结余: $balance, 储蓄率: $savingsRate%")
                
                // 🔍 数据质量检查
                if (totalIncome == 0 && totalExpense == 0) {
                    debugLog("⚠️ 数据质量检查: 所有统计数据为零，可能存在查询问题")
                } else {
                    debugLog("✅ 数据质量检查: 找到有效的统计数据")
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    dailyTotals = dailyTotals,
                    expenseCategories = expenseCategories,
                    incomeCategories = incomeCategories,
                    topExpenses = topExpenses,
                    topIncomes = topIncomes,
                    savingsRate = savingsRate,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = balance
                )
                
                val totalTime = System.currentTimeMillis() - startTime
                debugLog("统计数据加载完成，总耗时: ${totalTime}ms")
                
            } catch (e: IllegalArgumentException) {
                val error = StatisticsError(
                    type = StatisticsErrorType.DATE_RANGE_ERROR,
                    message = "日期范围错误: ${e.message}",
                    cause = e
                )
                handleStatisticsError(error)
            } catch (e: Exception) {
                val error = StatisticsError(
                    type = StatisticsErrorType.DATA_LOADING_ERROR,
                    message = "统计数据加载失败: ${e.message}",
                    cause = e
                )
                handleStatisticsError(error)
            }
        }
    }
    
    /**
     * 性能监控辅助方法
     */
    private suspend inline fun <T> measureStatisticsTime(operation: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        debugLog("开始执行: $operation")
        return try {
            val result = block()
            val duration = System.currentTimeMillis() - startTime
            
            // 🔍 详细记录查询结果
            when {
                result is List<*> -> {
                    debugLog("完成执行: $operation，耗时: ${duration}ms，返回数量: ${result.size}")
                }
                result is Map<*, *> -> {
                    debugLog("完成执行: $operation，耗时: ${duration}ms，返回数量: ${result.size}")
                }
                result is Number -> {
                    debugLog("完成执行: $operation，耗时: ${duration}ms，返回值: $result")
                }
                else -> {
                    debugLog("完成执行: $operation，耗时: ${duration}ms")
                }
            }
            
            result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            errorLog("执行失败: $operation，耗时: ${duration}ms", e)
            throw e
        }
    }
    
    private fun getDateRange(period: TimePeriod): Pair<LocalDate, LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        debugLog("📅 当前日期: $today")
        
        val result = when (period) {
            TimePeriod.TODAY -> {
                debugLog("📅 今日范围: $today 到 $today")
                today to today
            }
            TimePeriod.THIS_WEEK -> {
                val startOfWeek = today.minus(today.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY)
                debugLog("📅 本周范围: $startOfWeek 到 $today")
                startOfWeek to today
            }
            TimePeriod.THIS_MONTH -> {
                val startOfMonth = LocalDate(today.year, today.month, 1)
                debugLog("📅 本月范围: $startOfMonth 到 $today")
                startOfMonth to today
            }
            TimePeriod.LAST_MONTH -> {
                val lastMonth = today.minus(1, DateTimeUnit.MONTH)
                val startOfLastMonth = LocalDate(lastMonth.year, lastMonth.month, 1)
                val endOfLastMonth = startOfLastMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                debugLog("📅 上月范围: $startOfLastMonth 到 $endOfLastMonth")
                startOfLastMonth to endOfLastMonth
            }
            TimePeriod.LAST_QUARTER -> {
                val currentQuarter = (today.monthNumber - 1) / 3
                val lastQuarterMonth = currentQuarter * 3 + 1 - 3
                val lastQuarterStart = LocalDate(
                    if (lastQuarterMonth <= 0) today.year - 1 else today.year,
                    if (lastQuarterMonth <= 0) lastQuarterMonth + 12 else lastQuarterMonth, 1
                )
                val lastQuarterEnd = lastQuarterStart.plus(3, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
                debugLog("📅 上季度范围: $lastQuarterStart 到 $lastQuarterEnd")
                lastQuarterStart to lastQuarterEnd
            }
            TimePeriod.LAST_YEAR -> {
                val startOfLastYear = LocalDate(today.year - 1, 1, 1)
                val endOfLastYear = LocalDate(today.year - 1, 12, 31)
                debugLog("📅 去年范围: $startOfLastYear 到 $endOfLastYear")
                startOfLastYear to endOfLastYear
            }
            TimePeriod.RECENT_3_MONTHS -> {
                val start = today.minus(3, DateTimeUnit.MONTH)
                debugLog("📅 近3月范围: $start 到 $today")
                start to today
            }
            TimePeriod.RECENT_6_MONTHS -> {
                val start = today.minus(6, DateTimeUnit.MONTH)
                debugLog("📅 近半年范围: $start 到 $today")
                start to today
            }
            TimePeriod.THIS_QUARTER -> {
                val currentQuarter = (today.monthNumber - 1) / 3
                val quarterStartMonth = currentQuarter * 3 + 1
                val startOfQuarter = LocalDate(today.year, quarterStartMonth, 1)
                debugLog("📅 本季度范围: $startOfQuarter 到 $today")
                startOfQuarter to today
            }
            TimePeriod.THIS_YEAR -> {
                val startOfYear = LocalDate(today.year, 1, 1)
                debugLog("📅 本年范围: $startOfYear 到 $today")
                startOfYear to today
            }
            TimePeriod.CUSTOM -> {
                val state = _uiState.value
                val customStart = state.customStartDate ?: today.minus(30, DateTimeUnit.DAY)
                val customEnd = state.customEndDate ?: today
                debugLog("📅 自定义范围: $customStart 到 $customEnd")
                customStart to customEnd
            }
        }
        
        return result
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: TimePeriod = TimePeriod.THIS_MONTH,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val showDateRangePicker: Boolean = false,
    // 记账簿筛选相关状态
    val availableLedgers: List<Ledger> = emptyList(),
    val selectedLedgerFilter: LedgerFilter = LedgerFilter.All,
    val showLedgerSelector: Boolean = false,
    val dailyTotals: Map<LocalDate, Pair<Int, Int>> = emptyMap(),
    val expenseCategories: List<CategoryStatistic> = emptyList(),
    val incomeCategories: List<CategoryStatistic> = emptyList(),
    val topExpenses: List<Transaction> = emptyList(),
    val topIncomes: List<Transaction> = emptyList(),
    val savingsRate: Float = 0f,
    val totalIncome: Int = 0,
    val totalExpense: Int = 0,
    val balance: Int = 0
)

enum class TimePeriod {
    // 常用时间段
    TODAY,           // 今日
    THIS_WEEK,       // 本周
    THIS_MONTH,      // 本月
    
    // 对比分析
    LAST_MONTH,      // 上月
    LAST_QUARTER,    // 上季度
    LAST_YEAR,       // 去年
    
    // 长期分析
    RECENT_3_MONTHS, // 近3月
    RECENT_6_MONTHS, // 近半年
    THIS_QUARTER,    // 本季度
    THIS_YEAR,       // 本年
    
    // 自定义
    CUSTOM           // 自定义
}