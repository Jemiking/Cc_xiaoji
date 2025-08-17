package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components.*
import com.ccxiaoji.feature.ledger.presentation.navigation.LedgerNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    navController: androidx.navigation.NavController,
    onNavigateBack: () -> Unit,
    onNavigateToAccount: (String) -> Unit,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val TAG = "CreditCardScreen"
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 调试初始化信息
    LaunchedEffect(Unit) {
        Log.d(TAG, "CreditCardScreen初始化")
    }
    
    // 调试信用卡状态变化
    LaunchedEffect(creditCards) {
        Log.d(TAG, "信用卡列表更新，共${creditCards.size}张信用卡")
        creditCards.forEach { card ->
            Log.d(TAG, "  - ${card.name} (${card.id})")
        }
    }
    
    // 处理导航返回结果
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<Boolean>("credit_card_added")
            ?.let { added ->
                if (added) {
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("credit_card_added")
                    // 信用卡列表会自动刷新（通过Flow）
                }
            }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("信用卡管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(LedgerNavigation.AddCreditCardRoute) }) {
                        Icon(Icons.Default.Add, contentDescription = "添加信用卡")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 信用卡列表
            items(creditCards) { card ->
                CreditCardItem(
                    card = card,
                    onClick = { 
                        Log.d(TAG, "CreditCardScreen收到点击请求，信用卡: ${card.name}")
                        navController.navigate(LedgerNavigation.creditCardDetailRoute(card.id))
                    },
                    onEdit = {
                        Log.d(TAG, "CreditCardScreen收到编辑请求，信用卡: ${card.name}, ID: ${card.id}")
                        val editRoute = LedgerNavigation.editCreditCardRoute(card.id)
                        Log.d(TAG, "生成编辑路由: $editRoute")
                        try {
                            Log.d(TAG, "导航到信用卡编辑页面")
                            navController.navigate(editRoute)
                            Log.d(TAG, "信用卡编辑导航调用成功")
                        } catch (e: Exception) {
                            Log.e(TAG, "导航到信用卡编辑页面时异常", e)
                        }
                    }
                )
            }
            
            if (creditCards.isEmpty()) {
                item {
                    EmptyCreditCardState(
                        onAddCreditCard = { navController.navigate(LedgerNavigation.AddCreditCardRoute) }
                    )
                }
            }
        }
    }
    
    // 添加信用卡对话框已改为全屏页面
    // 原对话框功能已通过导航到AddCreditCardScreen实现
    
    // 信用卡详情对话框已改为全屏页面
    // 原对话框功能已通过导航到CreditCardDetailScreen实现
    
    // 还款历史对话框
    // TODO: 还款历史功能需要改造为独立页面
}