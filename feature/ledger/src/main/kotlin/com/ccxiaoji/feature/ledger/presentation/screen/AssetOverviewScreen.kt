package com.ccxiaoji.feature.ledger.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.core.ui.R
import com.ccxiaoji.feature.ledger.domain.model.AssetDistribution
import com.ccxiaoji.feature.ledger.domain.model.AssetItem
import com.ccxiaoji.feature.ledger.domain.model.AssetTrendData
import com.ccxiaoji.feature.ledger.domain.model.NetWorthData
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AssetOverviewViewModel
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 资产总览页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetOverviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssetOverviewViewModel = hiltViewModel()
) {
    val netWorthData by viewModel.netWorthData.collectAsStateWithLifecycle()
    val assetDistribution by viewModel.assetDistribution.collectAsStateWithLifecycle()
    val assetTrend by viewModel.assetTrend.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资产总览") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 净资产卡片
                item {
                    netWorthData?.let { data ->
                        NetWorthCard(data)
                    }
                }

                // 资产分布
                item {
                    assetDistribution?.let { distribution ->
                        AssetDistributionCard(distribution)
                    }
                }

                // 资产趋势
                item {
                    assetTrend?.let { trend ->
                        AssetTrendCard(trend)
                    }
                }

                // 账户列表
                item {
                    assetDistribution?.let { distribution ->
                        AccountListCard(distribution)
                    }
                }
            }
        }
    }
}

/**
 * 净资产卡片
 */
@Composable
fun NetWorthCard(data: NetWorthData) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "净资产",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 净资产金额
            Text(
                text = "¥${decimalFormat.format(data.netWorth)}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (data.netWorth >= BigDecimal.ZERO) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            // 变化率
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = if (data.netWorthChange >= 0) {
                        Icons.AutoMirrored.Filled.TrendingUp
                    } else {
                        Icons.AutoMirrored.Filled.TrendingDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (data.netWorthChange >= 0) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFFF5252)
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (data.netWorthChange >= 0) "+" else ""}${String.format("%.2f", data.netWorthChange)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (data.netWorthChange >= 0) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFFF5252)
                    }
                )
                Text(
                    text = " 较上月",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // 资产和负债
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 总资产
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总资产",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${decimalFormat.format(data.totalAssets)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "${if (data.assetsChange >= 0) "+" else ""}${String.format("%.2f", data.assetsChange)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (data.assetsChange >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFFF5252)
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                
                // 总负债
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "总负债",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¥${decimalFormat.format(data.totalLiabilities)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "${if (data.liabilitiesChange >= 0) "+" else ""}${String.format("%.2f", data.liabilitiesChange)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (data.liabilitiesChange >= 0) {
                            Color(0xFFFF5252)
                        } else {
                            Color(0xFF4CAF50)
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * 资产分布卡片
 */
@Composable
fun AssetDistributionCard(distribution: AssetDistribution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "资产分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 资产占比图（简化版本，使用水平条形图）
            if (distribution.assetItems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    distribution.assetItems.take(5).forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .weight(item.percentage / 100f)
                                .fillMaxHeight()
                                .background(
                                    color = getColorForIndex(index)
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 图例
                distribution.assetItems.take(5).forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = getColorForIndex(index),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.accountName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${String.format("%.1f", item.percentage)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 资产趋势卡片
 */
@Composable
fun AssetTrendCard(trend: AssetTrendData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "资产趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "近${trend.months}个月",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 简化的趋势图（显示最近值）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 资产趋势
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "资产",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (trend.assetsTrend.isNotEmpty()) {
                        val latest = trend.assetsTrend.last()
                        Text(
                            text = "¥${DecimalFormat("#,##0").format(latest.value)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                // 负债趋势
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "负债",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (trend.liabilitiesTrend.isNotEmpty()) {
                        val latest = trend.liabilitiesTrend.last()
                        Text(
                            text = "¥${DecimalFormat("#,##0").format(latest.value)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                // 净资产趋势
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "净资产",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (trend.netWorthTrend.isNotEmpty()) {
                        val latest = trend.netWorthTrend.last()
                        Text(
                            text = "¥${DecimalFormat("#,##0").format(latest.value)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 趋势月份标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                trend.netWorthTrend.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 账户列表卡片
 */
@Composable
fun AccountListCard(distribution: AssetDistribution) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "账户列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 资产账户
            if (distribution.assetItems.isNotEmpty()) {
                Text(
                    text = "资产账户",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                distribution.assetItems.forEach { item ->
                    AccountItem(item = item, decimalFormat = decimalFormat)
                }
            }
            
            // 负债账户
            if (distribution.liabilityItems.isNotEmpty()) {
                if (distribution.assetItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(
                    text = "负债账户",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                distribution.liabilityItems.forEach { item ->
                    AccountItem(item = item, decimalFormat = decimalFormat)
                }
            }
        }
    }
}

/**
 * 账户项
 */
@Composable
fun AccountItem(
    item: AssetItem,
    decimalFormat: DecimalFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (item.accountType) {
                "CREDIT_CARD" -> Icons.Default.CreditCard
                "CASH" -> Icons.Default.AccountBalanceWallet
                else -> Icons.Default.AccountBalance
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.accountName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${String.format("%.1f", item.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "¥${decimalFormat.format(item.balance)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (item.isAsset) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}

/**
 * 获取指定索引的颜色
 */
@Composable
fun getColorForIndex(index: Int): Color {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFF4CAF50),
        Color(0xFFFF9800)
    )
    return colors[index % colors.size]
}