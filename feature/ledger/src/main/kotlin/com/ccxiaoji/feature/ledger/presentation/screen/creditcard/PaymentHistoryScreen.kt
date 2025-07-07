package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.data.local.entity.CreditCardPaymentEntity
import com.ccxiaoji.feature.ledger.data.local.entity.PaymentType
import com.ccxiaoji.feature.ledger.domain.repository.PaymentStats
import com.ccxiaoji.feature.ledger.presentation.viewmodel.PaymentHistoryViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    accountId: String,
    navController: NavController,
    viewModel: PaymentHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPayment by remember { mutableStateOf<CreditCardPaymentEntity?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "${uiState.account?.name ?: "信用卡"} 还款历史",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Payment Statistics
            uiState.paymentStats?.let { stats ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.onTimePaymentRate.toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "按时还款率",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stats.paymentCount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "还款次数",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatPaymentCurrency(stats.totalPayments / 100.0),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "累计还款",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            
            // Payment List
            if (uiState.payments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            "暂无还款记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.payments) { payment ->
                        PaymentHistoryItem(
                            payment = payment,
                            onClick = { selectedPayment = payment }
                        )
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    selectedPayment?.let { payment ->
        AlertDialog(
            onDismissRequest = { selectedPayment = null },
            title = { Text("删除还款记录") },
            text = {
                Text("确定要删除这条还款记录吗？删除后账户余额将会相应调整。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePayment(payment.id)
                        selectedPayment = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPayment = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun PaymentHistoryItem(
    payment: CreditCardPaymentEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatPaymentCurrency(payment.paymentAmountCents / 100.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Payment Type Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (payment.paymentType) {
                            PaymentType.FULL -> MaterialTheme.colorScheme.primary
                            PaymentType.MINIMUM -> MaterialTheme.colorScheme.tertiary
                            PaymentType.CUSTOM -> MaterialTheme.colorScheme.secondary
                        }.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = when (payment.paymentType) {
                                PaymentType.FULL -> "全额"
                                PaymentType.MINIMUM -> "最低"
                                PaymentType.CUSTOM -> "部分"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (payment.paymentType) {
                                PaymentType.FULL -> MaterialTheme.colorScheme.primary
                                PaymentType.MINIMUM -> MaterialTheme.colorScheme.tertiary
                                PaymentType.CUSTOM -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    
                    // On-time Status
                    if (payment.isOnTime) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "按时还款",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "逾期还款",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Text(
                    text = formatDateTime(payment.paymentDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                payment.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "更多操作",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatPaymentCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return formatter.format(amount)
}

private fun formatDateTime(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.year}年${dateTime.monthNumber}月${dateTime.dayOfMonth}日 ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}