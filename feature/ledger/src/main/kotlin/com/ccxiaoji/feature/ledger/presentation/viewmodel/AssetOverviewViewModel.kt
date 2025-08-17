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

    init {
        Log.d(TAG, "AssetOverviewViewModel初始化开始")
        try {
            loadData()
            Log.d(TAG, "AssetOverviewViewModel初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "AssetOverviewViewModel初始化异常", e)
            _errorMessage.value = "初始化失败: ${e.message}"
        }
    }

    fun loadData() {
        Log.d(TAG, "开始加载资产数据")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                Log.d(TAG, "开始并行加载三个数据源")
                // 并行加载所有数据
                launch { 
                    Log.d(TAG, "开始加载净资产数据")
                    loadNetWorth() 
                    Log.d(TAG, "净资产数据加载完成")
                }
                launch { 
                    Log.d(TAG, "开始加载资产分布数据")
                    loadAssetDistribution() 
                    Log.d(TAG, "资产分布数据加载完成")
                }
                launch { 
                    Log.d(TAG, "开始加载资产趋势数据")
                    loadAssetTrend(6) 
                    Log.d(TAG, "资产趋势数据加载完成")
                }
                Log.d(TAG, "所有数据加载启动完成")
            } catch (e: Exception) {
                Log.e(TAG, "加载数据时发生异常", e)
                _errorMessage.value = "数据加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "数据加载状态重置完成")
            }
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
            Log.d(TAG, "开始获取账户列表进行资产分布计算")
            accountRepository.getAccounts().collect { accounts ->
                Log.d(TAG, "获取到${accounts.size}个账户")
                
                try {
                    val totalAssets = accounts
                        .filter { it.type != AccountType.CREDIT_CARD }
                        .sumOf { it.balanceYuan.toBigDecimal() }
                    Log.d(TAG, "总资产计算完成: $totalAssets")
                    
                    val totalLiabilities = accounts
                        .filter { it.type == AccountType.CREDIT_CARD }
                        .sumOf { it.balanceYuan.toBigDecimal().abs() }
                    Log.d(TAG, "总负债计算完成: $totalLiabilities")

                    val assetItems = accounts
                        .filter { it.type != AccountType.CREDIT_CARD && it.balanceYuan > 0 }
                        .map { account ->
                            Log.d(TAG, "处理资产账户: ${account.name}, 余额: ${account.balanceYuan}")
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
                        }
                        .sortedByDescending { it.balance }
                    Log.d(TAG, "资产项目处理完成，共${assetItems.size}项")

                    val liabilityItems = accounts
                        .filter { it.type == AccountType.CREDIT_CARD && it.balanceYuan < 0 }
                        .map { account ->
                            Log.d(TAG, "处理负债账户: ${account.name}, 余额: ${account.balanceYuan}")
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
                        }
                        .sortedByDescending { it.balance }
                    Log.d(TAG, "负债项目处理完成，共${liabilityItems.size}项")

                    _assetDistribution.value = AssetDistribution(
                        assetItems = assetItems,
                        liabilityItems = liabilityItems
                    )
                    Log.d(TAG, "资产分布数据更新完成")
                } catch (e: Exception) {
                    Log.e(TAG, "处理资产分布数据时发生异常", e)
                    _errorMessage.value = "资产分布计算失败: ${e.message}"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载资产分布时发生异常", e)
            _errorMessage.value = "加载资产分布失败: ${e.message}"
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
        try {
            Log.d(TAG, "开始加载资产趋势数据，月数: $months")
            val endDate = LocalDate.now()
            val startDate = endDate.minusMonths(months.toLong() - 1).withDayOfMonth(1)
            Log.d(TAG, "趋势数据时间范围: $startDate 到 $endDate")
            
            val assetsTrend = mutableListOf<TrendPoint>()
            val liabilitiesTrend = mutableListOf<TrendPoint>()
            val netWorthTrend = mutableListOf<TrendPoint>()
            
            var currentDate = startDate
            var monthCounter = 0
            while (!currentDate.isAfter(endDate)) {
                monthCounter++
                Log.d(TAG, "处理第${monthCounter}个月: $currentDate")
                
                val yearMonth = YearMonth.from(currentDate)
                val monthEnd = yearMonth.atEndOfMonth()
                
                try {
                    // 危险：这里在循环中调用collect，可能导致无限循环和内存泄漏！
                    Log.w(TAG, "警告：在循环中调用collect可能导致性能问题")
                    
                    // 修复：使用first()来获取一次性数据
                    val accounts = accountRepository.getAccounts().first()
                    Log.d(TAG, "获取到${accounts.size}个账户用于趋势计算")
                    
                    // 计算到月末为止的余额  
                    var monthAssets = BigDecimal.ZERO
                    for (account in accounts.filter { it.type != AccountType.CREDIT_CARD }) {
                        monthAssets += calculateAccountBalanceAtDate(account.id, monthEnd)
                    }
                    Log.d(TAG, "第${monthCounter}个月资产: $monthAssets")
                        
                    var monthLiabilities = BigDecimal.ZERO
                    for (account in accounts.filter { it.type == AccountType.CREDIT_CARD }) {
                        monthLiabilities += calculateAccountBalanceAtDate(account.id, monthEnd).abs()
                    }
                    Log.d(TAG, "第${monthCounter}个月负债: $monthLiabilities")
                        
                    val monthNetWorth = monthAssets - monthLiabilities
                    Log.d(TAG, "第${monthCounter}个月净资产: $monthNetWorth")
                    
                    val label = currentDate.format(DateTimeFormatter.ofPattern("M月"))
                    
                    assetsTrend.add(TrendPoint(
                        date = monthEnd,
                        value = monthAssets,
                        label = label
                    ))
                    
                    liabilitiesTrend.add(TrendPoint(
                        date = monthEnd,
                        value = monthLiabilities,
                        label = label
                    ))
                    
                    netWorthTrend.add(TrendPoint(
                        date = monthEnd,
                        value = monthNetWorth,
                        label = label
                    ))
                    
                } catch (e: Exception) {
                    Log.e(TAG, "处理第${monthCounter}个月数据时异常", e)
                    // 继续处理下一个月，但记录错误
                    _errorMessage.value = "趋势数据计算异常: ${e.message}"
                }
                
                currentDate = currentDate.plusMonths(1)
                
                // 防止无限循环
                if (monthCounter > 24) {
                    Log.e(TAG, "趋势计算超过24个月，可能存在无限循环，强制退出")
                    break
                }
            }
            
            Log.d(TAG, "趋势数据计算完成，共处理${monthCounter}个月")
            
            _assetTrend.value = AssetTrendData(
                assetsTrend = assetsTrend,
                liabilitiesTrend = liabilitiesTrend,
                netWorthTrend = netWorthTrend,
                months = months
            )
            Log.d(TAG, "资产趋势数据更新完成")
            
        } catch (e: Exception) {
            Log.e(TAG, "加载资产趋势时发生异常", e)
            _errorMessage.value = "加载资产趋势失败: ${e.message}"
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
            combine(
                accountRepository.getAccounts(),
                accountRepository.getAccounts() // 获取上月数据
            ) { currentAccounts, _ ->
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
                            Log.d(TAG, "账户${account.name}上月余额: $lastMonthBalance")
                            
                            if (account.type == AccountType.CREDIT_CARD) {
                                lastMonthLiabilities = lastMonthLiabilities.add(lastMonthBalance.abs())
                            } else {
                                lastMonthAssets = lastMonthAssets.add(lastMonthBalance)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "计算账户${account.name}上月余额时异常", e)
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
            }.collect {}
        } catch (e: Exception) {
            Log.e(TAG, "加载净资产时发生异常", e)
            _errorMessage.value = "加载净资产失败: ${e.message}"
        }
    }

    /**
     * 计算指定日期的账户余额
     */
    private suspend fun calculateAccountBalanceAtDate(accountId: String, date: LocalDate): BigDecimal {
        try {
            Log.d(TAG, "计算账户$accountId 在日期$date 的余额")
            // 简化处理：返回当前余额
            // 实际应该根据交易记录计算到指定日期的余额
            val account = accountRepository.getAccountById(accountId)
            val balance = account?.balanceYuan?.toBigDecimal() ?: BigDecimal.ZERO
            Log.d(TAG, "账户$accountId 余额: $balance")
            return balance
        } catch (e: Exception) {
            Log.e(TAG, "计算账户$accountId 余额时异常", e)
            return BigDecimal.ZERO
        }
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