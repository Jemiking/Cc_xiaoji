package com.ccxiaoji.feature.ledger.presentation.screen.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.screen.account.components.AssetOverviewContent
import com.ccxiaoji.feature.ledger.presentation.screen.account.components.AccountManagementContent
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AssetOverviewViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AccountViewModel
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation
import kotlinx.coroutines.launch

/**
 * 统一的账户资产主页面
 * 整合资产总览和账户管理功能，提供一站式的账户资产体验
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UnifiedAccountAssetScreen(
    onNavigateBack: () -> Unit,
    navController: NavController? = null,
    assetViewModel: AssetOverviewViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel()
) {
    // Tab状态管理
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    
    val tabItems = listOf(
        TabItem(
            title = "资产总览",
            icon = Icons.Default.PieChart
        ),
        TabItem(
            title = "账户管理", 
            icon = Icons.Default.AccountBalanceWallet
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when (pagerState.currentPage) {
                            0 -> "账户资产总览"
                            1 -> "账户管理"
                            else -> "账户资产"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab导航栏
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabItems.forEachIndexed { index, tabItem ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(tabItem.title) },
                        icon = { 
                            Icon(
                                imageVector = tabItem.icon,
                                contentDescription = tabItem.title
                            )
                        }
                    )
                }
            }
            
            // Tab内容区域
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        // 资产总览Tab
                        AssetOverviewContent(
                            viewModel = assetViewModel,
                            modifier = Modifier.fillMaxSize(),
                            showRefreshButton = false,
                            onRefresh = { assetViewModel.loadData() }
                        )
                    }
                    1 -> {
                        // 账户管理Tab
                        AccountManagementContent(
                            viewModel = accountViewModel,
                            navController = navController,
                            modifier = Modifier.fillMaxSize(),
                            showFab = true,
                            onAddAccount = {
                                navController?.navigate(LedgerNavigation.addAccountRoute())
                            }
                        )
                    }
                }
            }
        }
    }
}


/**
 * Tab项数据类
 */
private data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)