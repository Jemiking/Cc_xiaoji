package com.ccxiaoji.feature.ledger.presentation.ui.creditcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.api.AccountItem
import com.ccxiaoji.feature.ledger.api.LedgerNavigator
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardViewModel
import com.ccxiaoji.core.common.utils.CreditCardDateUtils
import java.time.LocalDate
import java.time.ZoneId
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccount: (String) -> Unit,
    ledgerNavigator: LedgerNavigator,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.selectedCardPayments.collectAsStateWithLifecycle()
    val paymentStats by viewModel.paymentStats.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<AccountItem?>(null) }
    var showPaymentHistory by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.checkPaymentReminders()
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
                    IconButton(onClick = { showAddDialog = true }) {
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 信用卡列表
            items(creditCards) { card ->
                CreditCardItem(
                    card = card,
                    onClick = { selectedCard = card }
                )
            }
            
            if (creditCards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "暂无信用卡",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { showAddDialog = true }) {
                                Text("添加信用卡")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加信用卡对话框
    if (showAddDialog) {
        AddCreditCardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, creditLimit, usedAmount, billingDay, paymentDueDay ->
                viewModel.addCreditCard(name, creditLimit, usedAmount, billingDay, paymentDueDay)
                showAddDialog = false
            }
        )
    }
    
    // 信用卡详情对话框
    selectedCard?.let { card ->
        CreditCardDetailDialog(
            card = card,
            onDismiss = { selectedCard = null },
            onPayment = { amount ->
                // Determine payment type based on amount
                val paymentType = when {
                    amount >= -card.balanceYuan -> "FULL"
                    else -> "CUSTOM"
                }
                viewModel.recordPaymentWithType(card.id, amount, paymentType)
                selectedCard = null
            },
            onEdit = { creditLimit, usedAmount, billingDay, paymentDueDay ->
                viewModel.updateCreditCardInfo(card.id, creditLimit, usedAmount, billingDay, paymentDueDay)
                selectedCard = null
            },
            onNavigateToTransactions = {
                onNavigateToAccount(card.id)
                selectedCard = null
            },
            onViewPaymentHistory = {
                viewModel.loadPaymentHistory(card.id)
                showPaymentHistory = true
            },
            onViewBills = {
                ledgerNavigator.navigateToCreditCardBills(card.id)
                selectedCard = null
            }
        )
    }
    
    // 还款历史对话框
    if (showPaymentHistory && selectedCard != null) {
        PaymentHistoryDialog(
            card = selectedCard!!,
            payments = paymentHistory,
            stats = paymentStats,
            onDismiss = { showPaymentHistory = false },
            onDeletePayment = { paymentId ->
                viewModel.deletePaymentRecord(paymentId)
            }
        )
    }
    
    // 显示消息
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }
    
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show error snackbar or toast
            viewModel.clearMessage()
        }
    }
}

@Composable
fun CreditCardItem(
    card: AccountItem,
    onClick: () -> Unit
) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val utilizationRate = if (card.creditLimitCents != null && card.creditLimitCents > 0) {
        (-card.balanceCents.toDouble() / card.creditLimitCents) * 100
    } else 0.0
    
    val utilizationColor = when {
        utilizationRate >= 90 -> MaterialTheme.colorScheme.error
        utilizationRate >= 70 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    // 计算剩余还款天数
    val today = LocalDate.now(ZoneId.systemDefault())
    val daysUntilPayment = if (card.paymentDueDay != null && card.billingDay != null) {
        CreditCardDateUtils.calculateDaysUntilPayment(card.paymentDueDay, card.billingDay, today)
    } else null
    
    val urgentPayment = daysUntilPayment?.let { it in 0..3 } ?: false
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 账单日
                        card.billingDay?.let { billingDay ->
                            Text(
                                text = "账单日: $billingDay",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 还款日
                        card.paymentDueDay?.let { dueDay ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "还款日: $dueDay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (urgentPayment) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // 剩余天数提醒
                                daysUntilPayment?.let { days ->
                                    if (days <= 7) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (days <= 3) MaterialTheme.colorScheme.error
                                                           else MaterialTheme.colorScheme.tertiary,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (days == 0) "今天" else "${days}天",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onError
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 额度信息
            card.creditLimitCents?.let { limitCents ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "可用额度",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currencyFormatter.format(card.availableCreditYuan),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 使用率进度条
                    LinearProgressIndicator(
                        progress = { (utilizationRate / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = utilizationColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "已用 ${currencyFormatter.format(-card.balanceYuan)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "额度 ${currencyFormatter.format(card.creditLimitYuan)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}