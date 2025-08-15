package com.ccxiaoji.feature.ledger.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * 账户资产项
 * @property accountId 账户ID
 * @property accountName 账户名称
 * @property accountType 账户类型
 * @property balance 余额
 * @property percentage 占总资产的百分比
 * @property isAsset 是否为资产账户（true为资产，false为负债）
 */
data class AssetItem(
    val accountId: String,
    val accountName: String,
    val accountType: String,
    val balance: BigDecimal,
    val percentage: Float,
    val isAsset: Boolean
)

/**
 * 趋势点
 * @property date 日期
 * @property value 值
 * @property label 显示标签
 */
data class TrendPoint(
    val date: LocalDate,
    val value: BigDecimal,
    val label: String
)

/**
 * 净资产数据
 * @property totalAssets 总资产
 * @property totalLiabilities 总负债
 * @property netWorth 净资产（总资产 - 总负债）
 * @property assetsChange 资产变化率（相比上月）
 * @property liabilitiesChange 负债变化率（相比上月）
 * @property netWorthChange 净资产变化率（相比上月）
 */
data class NetWorthData(
    val totalAssets: BigDecimal,
    val totalLiabilities: BigDecimal,
    val netWorth: BigDecimal,
    val assetsChange: Float = 0f,
    val liabilitiesChange: Float = 0f,
    val netWorthChange: Float = 0f
)

/**
 * 资产分布数据
 * @property assetItems 资产账户列表
 * @property liabilityItems 负债账户列表
 */
data class AssetDistribution(
    val assetItems: List<AssetItem>,
    val liabilityItems: List<AssetItem>
)

/**
 * 资产趋势数据
 * @property assetsTrend 资产趋势
 * @property liabilitiesTrend 负债趋势
 * @property netWorthTrend 净资产趋势
 * @property months 趋势数据的月份数
 */
data class AssetTrendData(
    val assetsTrend: List<TrendPoint>,
    val liabilitiesTrend: List<TrendPoint>,
    val netWorthTrend: List<TrendPoint>,
    val months: Int
)