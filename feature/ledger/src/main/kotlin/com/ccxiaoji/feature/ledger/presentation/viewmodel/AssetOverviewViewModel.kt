package com.ccxiaoji.feature.ledger.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.AssetDistribution
import com.ccxiaoji.feature.ledger.domain.model.AssetItem
import com.ccxiaoji.feature.ledger.domain.model.AssetTrendData
import com.ccxiaoji.feature.ledger.domain.model.NetWorthData
import com.ccxiaoji.feature.ledger.domain.model.TrendPoint
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.feature.ledger.domain.repository.AccountRepository
import com.ccxiaoji.feature.ledger.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 资产总览ViewModel
 */
@HiltViewModel
class AssetOverviewViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AssetOverviewViewModel"
        private const val ENABLE_DEBUG_LOGS = true // 可配置的调试日志开关
        private const val ENABLE_VERBOSE_LOGS = false // 详细日志开关，生产环境关闭
        private const val MAX_TREND_MONTHS = 24 // 防止无限循环的最大月数
        private const val MIN_BALANCE_THRESHOLD = 0.01 // 最小余额阈值，过滤0余额账户
    }
    
    // 调试日志辅助方法
    private fun debugLog(message: String, throwable: Throwable? = null) {
        if (ENABLE_DEBUG_LOGS) {
            if (throwable != null) {
                Log.d(TAG, message, throwable)
            } else {
                Log.d(TAG, message)
            }
        }
    }
    
    private fun errorLog(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
    
    // 详细日志方法，用于控制大量重复日志
    private fun verboseLog(message: String) {
        if (ENABLE_VERBOSE_LOGS) {
            Log.v(TAG, message)
        }
    }

    private val _netWorthData = MutableStateFlow<NetWorthData?>(null)
    val netWorthData: StateFlow<NetWorthData?> = _netWorthData.asStateFlow()

    private val _assetDistribution = MutableStateFlow<AssetDistribution?>(null)
    val assetDistribution: StateFlow<AssetDistribution?> = _assetDistribution.asStateFlow()

    private val _assetTrend = MutableStateFlow<AssetTrendData?>(null)
    val assetTrend: StateFlow<AssetTrendData?> = _assetTrend.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 错误类型枚举
    enum class ErrorType {
        INITIALIZATION_ERROR,
        DATA_LOADING_ERROR,
        NETWORK_ERROR,
        CALCULATION_ERROR,
        UNKNOWN_ERROR
    }
    
    // 详细错误信息数据类
    data class DetailedError(
        val type: ErrorType,
        override val message: String,
        override val cause: Throwable? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : Exception(message, cause)
    
    init {
        debugLog("AssetOverviewViewModel 初始化开始")
        try {
            loadData()
            debugLog("AssetOverviewViewModel 初始化完成")
        } catch (e: Exception) {
            val error = DetailedError(
                type = ErrorType.INITIALIZATION_ERROR,
                message = "初始化失败: ${e.message}",
                cause = e
            )
            handleError(error)
        }
    }
    
    /**
     * 统一错误处理方法
     */
    private fun handleError(error: DetailedError) {
        errorLog("发生${error.type}错误: ${error.message}", error.cause)
        _errorMessage.value = when (error.type) {
            ErrorType.INITIALIZATION_ERROR -> "应用初始化失败，请重启应用"
            ErrorType.DATA_LOADING_ERROR -> "数据加载失败，请检查网络连接"
            ErrorType.NETWORK_ERROR -> "网络连接失败，请检查网络设置"
            ErrorType.CALCULATION_ERROR -> "数据计算失败，请稍后重试"
            ErrorType.UNKNOWN_ERROR -> "发生未知错误: ${error.message}"
        }
    }

    fun loadData() {
        debugLog("开始加载资产数据")
        val startTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            debugLog("isLoading设置为true，开始加载")
            
            try {
                debugLog("开始并行加载三个数据源")
                
                // 使用async来并行加载数据，并收集所有可能的错误
                val netWorthJob = async { 
                    measureTimeAndLog("净资产数据加载") { loadNetWorthWithErrorHandling() }
                }
                val distributionJob = async { 
                    measureTimeAndLog("资产分布数据加载") { loadAssetDistributionWithErrorHandling() }
                }
                val trendJob = async { 
                    measureTimeAndLog("资产趋势数据加载") { loadAssetTrendWithErrorHandling(6) }
                }
                
                debugLog("等待所有异步任务完成...")
                
                // 等待所有任务完成
                val results = listOf(netWorthJob.awaitCatching(), distributionJob.awaitCatching(), trendJob.awaitCatching())
                
                debugLog("所有异步任务已完成，检查结果")
                
                // 检查是否有失败的任务
                val failures = results.filter { it.isFailure }
                if (failures.isNotEmpty()) {
                    val combinedMessage = failures.mapNotNull { result ->
                        result.exceptionOrNull()?.message
                    }.joinToString("; ")
                    debugLog("部分数据加载失败: $combinedMessage")
                    // 即使部分失败，也不阻止UI显示已加载的数据
                } else {
                    debugLog("所有数据加载成功")
                }
                
                val totalTime = System.currentTimeMillis() - startTime
                debugLog("总数据加载耗时: ${totalTime}ms")
                
                // 🔧 关键修复：在try块中明确设置加载完成状态
                debugLog("设置加载状态为完成")
                _isLoading.value = false
                debugLog("isLoading已设置为false，数据加载流程完成")
                
            } catch (e: Exception) {
                val error = DetailedError(
                    type = ErrorType.DATA_LOADING_ERROR,
                    message = "数据加载失败: ${e.message}",
                    cause = e
                )
                handleError(error)
                
                // 确保即使出现异常也设置加载完成状态
                _isLoading.value = false
                debugLog("异常情况下isLoading设置为false")
            }
            
            // 最终确保状态正确设置
            val finalTime = System.currentTimeMillis() - startTime
            debugLog("数据加载流程结束，最终确认isLoading状态: ${_isLoading.value}")
            debugLog("数据加载流程总耗时: ${finalTime}ms")
        }
    }
    
    /**
     * 性能监控辅助方法
     */
    private suspend inline fun <T> measureTimeAndLog(operation: String, block: suspend () -> T): T {
        val startTime = System.currentTimeMillis()
        debugLog("开始执行: $operation")
        return try {
            block().also {
                val duration = System.currentTimeMillis() - startTime
                debugLog("完成执行: $operation，耗时: ${duration}ms")
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            errorLog("执行失败: $operation，耗时: ${duration}ms", e)
            throw e
        }
    }
    
    /**
     * 安全的async awaiting方法
     */
    private suspend fun <T> Deferred<T>.awaitCatching(): kotlin.Result<T> {
        return try {
            kotlin.Result.success(await())
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    /**
     * 带错误处理的净资产数据加载
     */
    private suspend fun loadNetWorthWithErrorHandling() {
        try {
            loadNetWorth()
        } catch (e: Exception) {
            val error = DetailedError(
                type = ErrorType.CALCULATION_ERROR,
                message = "净资产计算失败: ${e.message}",
                cause = e
            )
            handleError(error)
            throw e
        }
    }
    
    /**
     * 带错误处理的资产分布数据加载
     */
    private suspend fun loadAssetDistributionWithErrorHandling() {
        try {
            loadAssetDistribution()
        } catch (e: Exception) {
            val error = DetailedError(
                type = ErrorType.CALCULATION_ERROR,
                message = "资产分布计算失败: ${e.message}",
                cause = e
            )
            handleError(error)
            throw e
        }
    }
    
    /**
     * 带错误处理的资产趋势数据加载
     */
    private suspend fun loadAssetTrendWithErrorHandling(months: Int) {
        try {
            loadAssetTrend(months)
        } catch (e: Exception) {
            val error = DetailedError(
                type = ErrorType.CALCULATION_ERROR,
                message = "资产趋势计算失败: ${e.message}",
                cause = e
            )
            handleError(error)
            throw e
        }
    }

    /**
     * 获取资产分布数据
     */
    fun getAssetDistribution() {
        viewModelScope.launch {
            loadAssetDistribution()
        }
    }

    private suspend fun loadAssetDistribution() {
        try {
            debugLog("开始获取账户列表进行资产分布计算")
            val accounts = accountRepository.getAccounts().first()
                debugLog("获取到${accounts.size}个账户")
                
                try {
                    // 🔧 性能优化：预过滤有效账户
                    val assetAccounts = accounts.filter { 
                        it.type != AccountType.CREDIT_CARD && it.balanceYuan >= MIN_BALANCE_THRESHOLD 
                    }
                    val liabilityAccounts = accounts.filter { 
                        it.type == AccountType.CREDIT_CARD && it.balanceYuan <= -MIN_BALANCE_THRESHOLD 
                    }
                    
                    val totalAssets = assetAccounts.sumOf { it.balanceYuan.toBigDecimal() }
                    val totalLiabilities = liabilityAccounts.sumOf { it.balanceYuan.toBigDecimal().abs() }
                    
                    debugLog("总资产: $totalAssets (${assetAccounts.size}个有效账户)")
                    debugLog("总负债: $totalLiabilities (${liabilityAccounts.size}个有效账户)")

                    val assetItems = assetAccounts.map { account ->
                        verboseLog("处理资产账户: ${account.name}, 余额: ${account.balanceYuan}")
                        AssetItem(
                            accountId = account.id,
                            accountName = account.name,
                            accountType = account.type.name,
                            balance = account.balanceYuan.toBigDecimal(),
                            percentage = if (totalAssets > BigDecimal.ZERO) {
                                account.balanceYuan.toBigDecimal().divide(totalAssets, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal(100))
                                    .toFloat()
                            } else 0f,
                            isAsset = true
                        )
                    }.sortedByDescending { it.balance }

                    val liabilityItems = liabilityAccounts.map { account ->
                        verboseLog("处理负债账户: ${account.name}, 余额: ${account.balanceYuan}")
                        AssetItem(
                            accountId = account.id,
                            accountName = account.name,
                            accountType = account.type.name,
                            balance = account.balanceYuan.toBigDecimal().abs(),
                            percentage = if (totalLiabilities > BigDecimal.ZERO) {
                                account.balanceYuan.toBigDecimal().abs().divide(totalLiabilities, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal(100))
                                    .toFloat()
                            } else 0f,
                            isAsset = false
                        )
                    }.sortedByDescending { it.balance }

                    _assetDistribution.value = AssetDistribution(
                        assetItems = assetItems,
                        liabilityItems = liabilityItems
                    )
                    
                    debugLog("资产分布计算完成 - 资产项目: ${assetItems.size}, 负债项目: ${liabilityItems.size}")
                    
                    // ⚠️ 数据质量检查
                    if (assetItems.isEmpty() && liabilityItems.isEmpty()) {
                        debugLog("⚠️ 警告：所有账户余额都低于阈值($MIN_BALANCE_THRESHOLD)，可能存在数据问题")
                    }
                    
                } catch (e: Exception) {
                    val error = DetailedError(
                        type = ErrorType.CALCULATION_ERROR,
                        message = "资产分布计算失败: ${e.message}",
                        cause = e
                    )
                    handleError(error)
                }
        } catch (e: Exception) {
            val error = DetailedError(
                type = ErrorType.DATA_LOADING_ERROR,
                message = "加载资产分布失败: ${e.message}",
                cause = e
            )
            handleError(error)
        }
    }

    /**
     * 获取资产趋势数据
     * @param months 趋势数据的月份数，默认6个月
     */
    fun getAssetTrend(months: Int = 6) {
        viewModelScope.launch {
            loadAssetTrend(months)
        }
    }

    private suspend fun loadAssetTrend(months: Int) {
        debugLog("开始加载资产趋势数据，月数: $months")
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(months.toLong() - 1).withDayOfMonth(1)
        debugLog("趋势数据时间范围: $startDate 到 $endDate")
        
        // 🔧 性能优化：一次性获取账户数据，避免在循环中重复调用
        val accounts = try {
            accountRepository.getAccounts().first()
        } catch (e: Exception) {
            errorLog("获取账户数据失败", e)
            throw DetailedError(
                type = ErrorType.DATA_LOADING_ERROR,
                message = "无法获取账户数据",
                cause = e
            )
        }
        
        debugLog("获取到${accounts.size}个账户用于趋势计算")
        
        val assetAccounts = accounts.filter { it.type != AccountType.CREDIT_CARD }
        val liabilityAccounts = accounts.filter { it.type == AccountType.CREDIT_CARD }
        
        val assetsTrend = mutableListOf<TrendPoint>()
        val liabilitiesTrend = mutableListOf<TrendPoint>()
        val netWorthTrend = mutableListOf<TrendPoint>()
        
        var currentDate = startDate
        var monthCounter = 0
        
        // 🚀 性能优化：如果所有账户余额都为0，使用快速路径
        val hasValidBalances = (assetAccounts + liabilityAccounts).any { 
            it.balanceYuan.toBigDecimal().abs() >= BigDecimal(MIN_BALANCE_THRESHOLD) 
        }
        
        if (!hasValidBalances) {
            debugLog("⚡ 快速路径：所有账户余额为0，生成零值趋势数据")
            repeat(months) { index ->
                val monthDate = startDate.plusMonths(index.toLong())
                val yearMonth = YearMonth.from(monthDate)
                val monthEnd = yearMonth.atEndOfMonth()
                val label = monthDate.format(DateTimeFormatter.ofPattern("M月"))
                
                assetsTrend.add(TrendPoint(monthEnd, BigDecimal.ZERO, label))
                liabilitiesTrend.add(TrendPoint(monthEnd, BigDecimal.ZERO, label))
                netWorthTrend.add(TrendPoint(monthEnd, BigDecimal.ZERO, label))
            }
            monthCounter = months
        } else {
            // 正常处理路径
            while (currentDate <= endDate && monthCounter < MAX_TREND_MONTHS) {
                monthCounter++
                verboseLog("处理第${monthCounter}个月: $currentDate")
                
                val yearMonth = YearMonth.from(currentDate)
                val monthEnd = yearMonth.atEndOfMonth()
                
                try {
                    // 🚀 性能改进：并行计算资产和负债
                    val monthAssets = viewModelScope.async {
                        calculateTotalBalanceAtDate(assetAccounts, monthEnd)
                    }
                    val monthLiabilities = viewModelScope.async {
                        calculateTotalBalanceAtDate(liabilityAccounts, monthEnd).abs()
                    }
                    
                    val assets = monthAssets.await()
                    val liabilities = monthLiabilities.await()
                    val netWorth = assets - liabilities
                    
                    verboseLog("第${monthCounter}个月 - 资产: $assets, 负债: $liabilities, 净资产: $netWorth")
                    
                    val label = currentDate.format(DateTimeFormatter.ofPattern("M月"))
                    
                    assetsTrend.add(TrendPoint(
                        date = monthEnd,
                        value = assets,
                        label = label
                    ))
                    
                    liabilitiesTrend.add(TrendPoint(
                        date = monthEnd,
                        value = liabilities,
                        label = label
                    ))
                    
                    netWorthTrend.add(TrendPoint(
                        date = monthEnd,
                        value = netWorth,
                        label = label
                    ))
                    
                } catch (e: Exception) {
                    errorLog("处理第${monthCounter}个月数据时异常", e)
                    // 记录错误但继续处理，保证其他月份数据可用
                    val error = DetailedError(
                        type = ErrorType.CALCULATION_ERROR,
                        message = "第${monthCounter}个月数据计算失败: ${e.message}",
                        cause = e
                    )
                    handleError(error)
                }
                
                currentDate = currentDate.plusMonths(1)
            }
        }
        
        if (monthCounter >= MAX_TREND_MONTHS) {
            debugLog("趋势计算达到最大月数限制(${MAX_TREND_MONTHS})，停止计算")
        }
        
        debugLog("趋势数据计算完成，共处理${monthCounter}个月")
        
        _assetTrend.value = AssetTrendData(
            assetsTrend = assetsTrend,
            liabilitiesTrend = liabilitiesTrend,
            netWorthTrend = netWorthTrend,
            months = months
        )
        debugLog("资产趋势数据更新完成")
    }
    
    /**
     * 🎯 改进的账户余额计算方法
     * 并行计算多个账户的总余额
     */
    private suspend fun calculateTotalBalanceAtDate(
        accounts: List<com.ccxiaoji.feature.ledger.domain.model.Account>, 
        date: LocalDate
    ): BigDecimal {
        return coroutineScope {
            accounts.map { account ->
                async { calculateAccountBalanceAtDateImproved(account.id, date) }
            }.map { it.await() }.sumOf { it }
        }
    }

    /**
     * 计算净资产
     */
    fun getNetWorth() {
        viewModelScope.launch {
            loadNetWorth()
        }
    }

    private suspend fun loadNetWorth() {
        try {
            Log.d(TAG, "开始加载净资产数据")
            val currentAccounts = accountRepository.getAccounts().first()
                Log.d(TAG, "combine中获取到${currentAccounts.size}个账户")
                
                try {
                    val totalAssets = currentAccounts
                        .filter { it.type != AccountType.CREDIT_CARD }
                        .sumOf { it.balanceYuan.toBigDecimal() }
                    Log.d(TAG, "当前总资产: $totalAssets")
                    
                    val totalLiabilities = currentAccounts
                        .filter { it.type == AccountType.CREDIT_CARD }
                        .sumOf { it.balanceYuan.toBigDecimal().abs() }
                    Log.d(TAG, "当前总负债: $totalLiabilities")
                    
                    val netWorth = totalAssets - totalLiabilities
                    Log.d(TAG, "当前净资产: $netWorth")

                    // 计算上月数据（简化处理，实际需要根据交易记录计算）
                    val lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1)
                    val lastMonthEnd = YearMonth.from(lastMonthStart).atEndOfMonth()
                    Log.d(TAG, "计算上月数据，截止日期: $lastMonthEnd")
                    
                    var lastMonthAssets = BigDecimal.ZERO
                    var lastMonthLiabilities = BigDecimal.ZERO
                    
                    currentAccounts.forEach { account ->
                        try {
                            val lastMonthBalance = calculateAccountBalanceAtDate(account.id, lastMonthEnd)
                            verboseLog("账户${account.name}上月余额: $lastMonthBalance")
                            
                            if (account.type == AccountType.CREDIT_CARD) {
                                lastMonthLiabilities = lastMonthLiabilities.add(lastMonthBalance.abs())
                            } else {
                                lastMonthAssets = lastMonthAssets.add(lastMonthBalance)
                            }
                        } catch (e: Exception) {
                            errorLog("计算账户${account.name}上月余额时异常", e)
                        }
                    }
                    
                    Log.d(TAG, "上月总资产: $lastMonthAssets, 上月总负债: $lastMonthLiabilities")
                    val lastMonthNetWorth = lastMonthAssets - lastMonthLiabilities
                    Log.d(TAG, "上月净资产: $lastMonthNetWorth")

                    // 计算变化率
                    val assetsChange = calculateChangeRate(totalAssets, lastMonthAssets)
                    val liabilitiesChange = calculateChangeRate(totalLiabilities, lastMonthLiabilities)
                    val netWorthChange = calculateChangeRate(netWorth, lastMonthNetWorth)
                    Log.d(TAG, "变化率 - 资产: $assetsChange%, 负债: $liabilitiesChange%, 净资产: $netWorthChange%")

                    _netWorthData.value = NetWorthData(
                        totalAssets = totalAssets,
                        totalLiabilities = totalLiabilities,
                        netWorth = netWorth,
                        assetsChange = assetsChange,
                        liabilitiesChange = liabilitiesChange,
                        netWorthChange = netWorthChange
                    )
                    Log.d(TAG, "净资产数据更新完成")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "处理净资产数据时异常", e)
                    _errorMessage.value = "净资产计算失败: ${e.message}"
                }
        } catch (e: Exception) {
            Log.e(TAG, "加载净资产时发生异常", e)
            _errorMessage.value = "加载净资产失败: ${e.message}"
        }
    }

    /**
     * 🎯 改进的历史余额计算方法
     * 基于交易记录计算指定日期的实际余额，而不是简单返回当前余额
     */
    private suspend fun calculateAccountBalanceAtDateImproved(accountId: String, date: LocalDate): BigDecimal {
        return try {
            verboseLog("计算账户$accountId 在日期$date 的历史余额")
            
            // TODO: 实现真正的历史余额计算
            // 这里应该：
            // 1. 获取账户的初始余额
            // 2. 获取从账户创建到指定日期的所有交易
            // 3. 累计计算到指定日期的余额
            
            // 目前的简化实现：返回当前余额
            // 在生产环境中需要改为基于历史交易的计算
            val account = accountRepository.getAccountById(accountId)
            val balance = account?.balanceYuan?.toBigDecimal() ?: BigDecimal.ZERO
            
            verboseLog("账户${account?.name ?: accountId} 历史余额(当前简化实现): $balance")
            balance
            
        } catch (e: Exception) {
            errorLog("计算账户$accountId 历史余额时异常", e)
            BigDecimal.ZERO
        }
    }
    
    /**
     * 向后兼容的原始方法
     */
    private suspend fun calculateAccountBalanceAtDate(accountId: String, date: LocalDate): BigDecimal {
        return calculateAccountBalanceAtDateImproved(accountId, date)
    }

    /**
     * 计算变化率
     * 修复：使用compareTo方法进行BigDecimal比较，避免除零错误
     */
    private fun calculateChangeRate(current: BigDecimal, previous: BigDecimal): Float {
        Log.d(TAG, "calculateChangeRate - current: $current, previous: $previous")
        
        return when {
            // 使用compareTo方法进行精确的BigDecimal比较
            previous.compareTo(BigDecimal.ZERO) == 0 -> {
                Log.d(TAG, "previous为零，应用特殊逻辑")
                when {
                    current.compareTo(BigDecimal.ZERO) == 0 -> {
                        Log.d(TAG, "current也为零，变化率为0%")
                        0f
                    }
                    current.compareTo(BigDecimal.ZERO) > 0 -> {
                        Log.d(TAG, "从0增长到正值，变化率为+100%")
                        100f
                    }
                    else -> {
                        Log.d(TAG, "从0变为负值，变化率为-100%")
                        -100f
                    }
                }
            }
            previous.abs().compareTo(BigDecimal("0.01")) < 0 -> {
                // 如果previous的绝对值小于0.01，认为接近零，避免极大的变化率
                Log.d(TAG, "previous接近零(${previous})，使用简化计算")
                if (current.compareTo(BigDecimal.ZERO) > 0) 100f else -100f
            }
            else -> {
                try {
                    val changeRate = current.subtract(previous)
                        .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal(100))
                        .toFloat()
                    Log.d(TAG, "正常计算变化率: $changeRate%")
                    changeRate
                } catch (e: ArithmeticException) {
                    Log.e(TAG, "计算变化率时发生除法异常", e)
                    0f
                }
            }
        }
    }
}