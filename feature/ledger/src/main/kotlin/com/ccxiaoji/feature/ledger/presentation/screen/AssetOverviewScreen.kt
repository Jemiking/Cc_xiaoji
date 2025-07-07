package com.ccxiaoji.feature.ledger.presentation.screen

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
                contentPadding = PaddingValues(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
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