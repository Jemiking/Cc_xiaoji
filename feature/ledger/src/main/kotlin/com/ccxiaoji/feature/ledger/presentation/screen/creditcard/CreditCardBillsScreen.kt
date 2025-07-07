package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardBillViewModel
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components.BillItem
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components.EmptyBillState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardBillsScreen(
    accountId: String,
    navController: NavController,
    viewModel: CreditCardBillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val bills by viewModel.getBills(accountId).collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }
    
    // 显示成功消息
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }
    
    // 显示错误消息
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.accountName ?: "信用卡账单") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.generateBillForAccount(accountId) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "生成账单")
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
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            if (bills.isEmpty()) {
                item {
                    EmptyBillState()
                }
            } else {
                items(bills) { bill ->
                    BillItem(
                        bill = bill,
                        onClick = {
                            navController.navigate("credit_card_bill_detail/${bill.id}")
                        }
                    )
                }
            }
        }
    }
}
