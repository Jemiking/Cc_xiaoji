package com.ccxiaoji.feature.ledger.presentation.viewmodel

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

    private val _netWorthData = MutableStateFlow<NetWorthData?>(null)
    val netWorthData: StateFlow<NetWorthData?> = _netWorthData.asStateFlow()

    private val _assetDistribution = MutableStateFlow<AssetDistribution?>(null)
    val assetDistribution: StateFlow<AssetDistribution?> = _assetDistribution.asStateFlow()

    private val _assetTrend = MutableStateFlow<AssetTrendData?>(null)
    val assetTrend: StateFlow<AssetTrendData?> = _assetTrend.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 并行加载所有数据
                launch { loadNetWorth() }
                launch { loadAssetDistribution() }
                launch { loadAssetTrend(6) }
            } finally {
                _isLoading.value = false
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
        accountRepository.getAccounts().collect { accounts ->
            val totalAssets = accounts
                .filter { it.type != AccountType.CREDIT_CARD }
                .sumOf { it.balanceYuan.toBigDecimal() }
            val totalLiabilities = accounts
                .filter { it.type == AccountType.CREDIT_CARD }
                .sumOf { it.balanceYuan.toBigDecimal().abs() }

            val assetItems = accounts
                .filter { it.type != AccountType.CREDIT_CARD && it.balanceYuan > 0 }
                .map { account ->
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

            val liabilityItems = accounts
                .filter { it.type == AccountType.CREDIT_CARD && it.balanceYuan < 0 }
                .map { account ->
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

            _assetDistribution.value = AssetDistribution(
                assetItems = assetItems,
                liabilityItems = liabilityItems
            )
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
        val endDate = LocalDate.now()
        val startDate = endDate.minusMonths(months.toLong() - 1).withDayOfMonth(1)
        
        val assetsTrend = mutableListOf<TrendPoint>()
        val liabilitiesTrend = mutableListOf<TrendPoint>()
        val netWorthTrend = mutableListOf<TrendPoint>()
        
        var currentDate = startDate
        while (!currentDate.isAfter(endDate)) {
            val yearMonth = YearMonth.from(currentDate)
            val monthEnd = yearMonth.atEndOfMonth()
            
            // 获取每个月末的资产状况
            accountRepository.getAccounts().collect { accounts ->
                // 计算到月末为止的余额
                val monthAssets = accounts
                    .filter { it.type != AccountType.CREDIT_CARD }
                    .sumOf { account ->
                        calculateAccountBalanceAtDate(account.id, monthEnd)
                    }
                    
                val monthLiabilities = accounts
                    .filter { it.type == AccountType.CREDIT_CARD }
                    .sumOf { account ->
                        calculateAccountBalanceAtDate(account.id, monthEnd).abs()
                    }
                    
                val monthNetWorth = monthAssets - monthLiabilities
                
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
            }
            
            currentDate = currentDate.plusMonths(1)
        }
        
        _assetTrend.value = AssetTrendData(
            assetsTrend = assetsTrend,
            liabilitiesTrend = liabilitiesTrend,
            netWorthTrend = netWorthTrend,
            months = months
        )
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
        combine(
            accountRepository.getAccounts(),
            accountRepository.getAccounts() // 获取上月数据
        ) { currentAccounts, _ ->
            val totalAssets = currentAccounts
                .filter { it.type != AccountType.CREDIT_CARD }
                .sumOf { it.balanceYuan.toBigDecimal() }
            val totalLiabilities = currentAccounts
                .filter { it.type == AccountType.CREDIT_CARD }
                .sumOf { it.balanceYuan.toBigDecimal().abs() }
            val netWorth = totalAssets - totalLiabilities

            // 计算上月数据（简化处理，实际需要根据交易记录计算）
            val lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1)
            val lastMonthEnd = YearMonth.from(lastMonthStart).atEndOfMonth()
            
            var lastMonthAssets = BigDecimal.ZERO
            var lastMonthLiabilities = BigDecimal.ZERO
            
            currentAccounts.forEach { account ->
                val lastMonthBalance = calculateAccountBalanceAtDate(account.id, lastMonthEnd)
                if (account.type == AccountType.CREDIT_CARD) {
                    lastMonthLiabilities = lastMonthLiabilities.add(lastMonthBalance.abs())
                } else {
                    lastMonthAssets = lastMonthAssets.add(lastMonthBalance)
                }
            }
            
            val lastMonthNetWorth = lastMonthAssets - lastMonthLiabilities

            // 计算变化率
            val assetsChange = calculateChangeRate(totalAssets, lastMonthAssets)
            val liabilitiesChange = calculateChangeRate(totalLiabilities, lastMonthLiabilities)
            val netWorthChange = calculateChangeRate(netWorth, lastMonthNetWorth)

            _netWorthData.value = NetWorthData(
                totalAssets = totalAssets,
                totalLiabilities = totalLiabilities,
                netWorth = netWorth,
                assetsChange = assetsChange,
                liabilitiesChange = liabilitiesChange,
                netWorthChange = netWorthChange
            )
        }.collect {}
    }

    /**
     * 计算指定日期的账户余额
     */
    private suspend fun calculateAccountBalanceAtDate(accountId: String, date: LocalDate): BigDecimal {
        // 简化处理：返回当前余额
        // 实际应该根据交易记录计算到指定日期的余额
        return accountRepository.getAccountById(accountId)?.balanceYuan?.toBigDecimal() ?: BigDecimal.ZERO
    }

    /**
     * 计算变化率
     */
    private fun calculateChangeRate(current: BigDecimal, previous: BigDecimal): Float {
        return if (previous == BigDecimal.ZERO) {
            if (current == BigDecimal.ZERO) 0f else 100f
        } else {
            current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toFloat()
        }
    }
}