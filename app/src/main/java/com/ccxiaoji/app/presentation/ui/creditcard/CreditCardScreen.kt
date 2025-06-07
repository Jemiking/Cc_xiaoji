package com.ccxiaoji.app.presentation.ui.creditcard

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
import com.ccxiaoji.app.domain.model.Account
import com.ccxiaoji.app.domain.model.AccountType
import com.ccxiaoji.app.presentation.viewmodel.CreditCardViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccount: (String) -> Unit,
    viewModel: CreditCardViewModel = hiltViewModel()
) {
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCard by remember { mutableStateOf<Account?>(null) }
    
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
            onConfirm = { name, creditLimit, billingDay, paymentDueDay ->
                viewModel.addCreditCard(name, creditLimit, billingDay, paymentDueDay)
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
                viewModel.recordPayment(card.id, amount)
                selectedCard = null
            },
            onEdit = { creditLimit, billingDay, paymentDueDay ->
                viewModel.updateCreditCardInfo(card.id, creditLimit, billingDay, paymentDueDay)
                selectedCard = null
            },
            onNavigateToTransactions = {
                onNavigateToAccount(card.id)
                selectedCard = null
            }
        )
    }
}

@Composable
fun CreditCardItem(
    card: Account,
    onClick: () -> Unit
) {
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // 可用额度
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "可用额度：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(card.availableCreditYuan ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 已用额度
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "已用额度：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(-card.balanceYuan),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (card.balanceYuan < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 使用率指示器
                card.utilizationRate?.let { rate ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    rate <= 30 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                    rate <= 70 -> Color(0xFFFFC107).copy(alpha = 0.1f)
                                    else -> Color(0xFFF44336).copy(alpha = 0.1f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${rate.toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    rate <= 30 -> Color(0xFF4CAF50)
                                    rate <= 70 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                            Text(
                                text = "使用率",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 账单日和还款日
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "账单日：${card.billingDay}号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "还款日：${card.paymentDueDay}号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 还款状态提示
                val currentDay = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).dayOfMonth
                if (card.balanceYuan < 0 && card.paymentDueDay != null) {
                    val daysUntilDue = if (currentDay <= card.paymentDueDay) {
                        card.paymentDueDay - currentDay
                    } else {
                        // 下个月的还款日
                        30 - currentDay + card.paymentDueDay
                    }
                    
                    if (daysUntilDue <= 3) {
                        Text(
                            text = "${daysUntilDue}天内还款",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return formatter.format(amount)
}