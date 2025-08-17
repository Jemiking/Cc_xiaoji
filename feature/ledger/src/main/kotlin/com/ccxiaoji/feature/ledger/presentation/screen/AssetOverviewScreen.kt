package com.ccxiaoji.feature.ledger.presentation.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AssetOverviewViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.asset.components.*

/**
 * 资产总览页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetOverviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssetOverviewViewModel = hiltViewModel()
) {
    val TAG = "AssetOverviewScreen"
    
    Log.d(TAG, "AssetOverviewScreen 开始Compose")
    
    val netWorthData by viewModel.netWorthData.collectAsStateWithLifecycle()
    val assetDistribution by viewModel.assetDistribution.collectAsStateWithLifecycle()
    val assetTrend by viewModel.assetTrend.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    // 调试状态变化
    LaunchedEffect(netWorthData) {
        Log.d(TAG, "NetWorthData状态变化: ${netWorthData != null}")
    }
    
    LaunchedEffect(assetDistribution) {
        Log.d(TAG, "AssetDistribution状态变化: ${assetDistribution != null}")
    }
    
    LaunchedEffect(assetTrend) {
        Log.d(TAG, "AssetTrend状态变化: ${assetTrend != null}")
    }
    
    LaunchedEffect(isLoading) {
        Log.d(TAG, "Loading状态变化: $isLoading")
    }
    
    LaunchedEffect(errorMessage) {
        Log.d(TAG, "ErrorMessage状态变化: $errorMessage")
    }

    // 错误提示
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            Log.e(TAG, "显示错误消息: $message")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资产总览") },
                navigationIcon = {
                    IconButton(onClick = {
                        Log.d(TAG, "点击返回按钮")
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Log.d(TAG, "点击刷新按钮")
                        viewModel.loadData() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        // 显示错误消息
        errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
            }
        } ?: run {
            if (isLoading) {
                Log.d(TAG, "显示加载指示器")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Log.d(TAG, "显示主要内容")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 净资产卡片
                    item {
                        netWorthData?.let { data ->
                            Log.d(TAG, "渲染NetWorthCard")
                            NetWorthCard(data)
                        } ?: run {
                            Log.d(TAG, "NetWorthData为null，显示占位符")
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
                            Log.d(TAG, "渲染AssetDistributionCard")
                            AssetDistributionCard(distribution)
                        } ?: run {
                            Log.d(TAG, "AssetDistribution为null，显示占位符")
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
                            Log.d(TAG, "渲染AssetTrendCard")
                            AssetTrendCard(trend)
                        } ?: run {
                            Log.d(TAG, "AssetTrend为null，显示占位符")
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
                            Log.d(TAG, "渲染AccountListCard")
                            AccountListCard(distribution)
                        } ?: run {
                            Log.d(TAG, "账户列表数据为null，显示占位符")
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
    
    Log.d(TAG, "AssetOverviewScreen Compose完成")
}