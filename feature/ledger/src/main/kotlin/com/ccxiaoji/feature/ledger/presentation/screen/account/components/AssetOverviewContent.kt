package com.ccxiaoji.feature.ledger.presentation.screen.account.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AssetOverviewViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.asset.components.*

/**
 * 资产总览内容组件
 * 提取自AssetOverviewScreen，可以在统一页面中复用
 */
@Composable
fun AssetOverviewContent(
    viewModel: AssetOverviewViewModel,
    modifier: Modifier = Modifier,
    showRefreshButton: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val TAG = "AssetOverviewContent"
    
    val netWorthData by viewModel.netWorthData.collectAsStateWithLifecycle()
    val assetDistribution by viewModel.assetDistribution.collectAsStateWithLifecycle()
    val assetTrend by viewModel.assetTrend.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // 错误提示
    errorMessage?.let { message ->
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "错误: $message",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Button(
                onClick = { 
                    Log.d(TAG, "点击重试按钮")
                    viewModel.loadData() 
                }
            ) {
                Text("重试")
            }
            
            if (showRefreshButton) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRefresh) {
                    Text("刷新")
                }
            }
        }
    } ?: run {
        if (isLoading) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 净资产卡片
                item {
                    netWorthData?.let { data ->
                        NetWorthCard(data)
                    } ?: run {
                        Card {
                            Text(
                                text = "净资产数据加载中...",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // 资产分布
                item {
                    assetDistribution?.let { distribution ->
                        AssetDistributionCard(distribution)
                    } ?: run {
                        Card {
                            Text(
                                text = "资产分布数据加载中...",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // 资产趋势
                item {
                    assetTrend?.let { trend ->
                        AssetTrendCard(trend)
                    } ?: run {
                        Card {
                            Text(
                                text = "资产趋势数据加载中...",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                // 账户列表
                item {
                    assetDistribution?.let { distribution ->
                        AccountListCard(distribution)
                    } ?: run {
                        Card {
                            Text(
                                text = "账户列表数据加载中...",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}