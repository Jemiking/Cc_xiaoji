package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardBillDetailViewModel
import com.ccxiaoji.feature.ledger.presentation.utils.CurrencyFormatter.formatCurrency
import com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components.*

/**
 * 信用卡账单详情界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardBillDetailScreen(
    billId: String,
    navController: NavController,
    viewModel: CreditCardBillDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(billId) {
        viewModel.loadBillDetail(billId)
    }
    
    val bill = uiState.bill
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账单详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (bill != null && !bill.isPaid) {
                        IconButton(onClick = { /* TODO: 导出账单 */ }) {
                            Icon(Icons.Default.Share, contentDescription = "分享账单")
                        }
                    }
                }
            )
        },
        bottomBar = {
            bill?.let { billData ->
                if (!billData.isPaid) {
                    BottomAppBar(
                        modifier = Modifier.height(80.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = DesignTokens.Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "待还金额",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    formatCurrency(billData.remainingAmountYuan),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            
                            Button(
                                onClick = { 
                                    // 导航到还款页面，传递信用卡账户ID
                                    navController.navigate("payment/${billData.accountId}")
                                },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                                Text("立即还款")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bill != null) {
            BillDetailContent(
                bill = bill,
                transactions = uiState.transactions,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示错误提示
        }
    }
}

@Composable
private fun BillDetailContent(
    bill: CreditCardBill,
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(DesignTokens.Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 账单概览卡片
        item {
            BillOverviewCard(bill)
        }
        
        // 还款进度卡片
        if (!bill.isPaid) {
            item {
                PaymentProgressCard(bill)
            }
        }
        
        // 账单明细标题
        item {
            Text(
                "账单明细",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = DesignTokens.Spacing.small)
            )
        }
        
        // 交易列表
        if (transactions.isEmpty()) {
            item {
                EmptyTransactionState()
            }
        } else {
            items(transactions) { transaction ->
                BillTransactionItem(transaction)
            }
        }
    }
}
