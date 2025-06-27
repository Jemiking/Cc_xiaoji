package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.feature.ledger.presentation.viewmodel.CreditCardBillDetailViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.Locale

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
    var showPaymentDialog by remember { mutableStateOf(false) }
    
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                                .padding(horizontal = 16.dp),
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
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
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
    
    // 还款对话框
    if (showPaymentDialog && bill != null) {
        PaymentDialog(
            bill = bill,
            accounts = uiState.accounts,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, fromAccountId ->
                viewModel.recordPayment(billId, amount, fromAccountId)
                showPaymentDialog = false
            }
        )
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        // 交易列表
        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "暂无交易记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(transactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
private fun BillOverviewCard(bill: CreditCardBill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 账单周期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatBillingPeriod(bill.billStartDate, bill.billEndDate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // 账单状态标签
                val statusText = when {
                    bill.isPaid -> "已还清"
                    bill.isOverdue -> "已逾期"
                    else -> "待还款"
                }
                val statusColor = when {
                    bill.isPaid -> MaterialTheme.colorScheme.primary
                    bill.isOverdue -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.tertiary
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 金额明细
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AmountRow("上期结余", bill.previousBalanceCents / 100.0)
                AmountRow("本期消费", bill.newChargesCents / 100.0, color = MaterialTheme.colorScheme.error)
                AmountRow("本期还款", bill.paymentsCents / 100.0, color = MaterialTheme.colorScheme.primary)
                if (bill.adjustmentsCents != 0L) {
                    AmountRow("调整金额", bill.adjustmentsCents / 100.0)
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                AmountRow(
                    "应还总额",
                    bill.totalAmountYuan,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (!bill.isPaid) {
                    AmountRow(
                        "最低还款",
                        bill.minimumPaymentYuan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 还款日期
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (bill.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "还款日：${formatDate(bill.paymentDueDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (bill.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PaymentProgressCard(bill: CreditCardBill) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "还款进度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = bill.paymentProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "已还：${formatCurrency(bill.paidAmountCents / 100.0)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "待还：${formatCurrency(bill.remainingAmountYuan)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(android.graphics.Color.parseColor(transaction.categoryDetails?.color ?: "#E0E0E0"))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.categoryDetails?.icon ?: "💰",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryDetails?.name ?: "未分类",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDateTime(transaction.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                transaction.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
        
        Text(
            text = formatCurrency(transaction.amountCents / 100.0),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (transaction.amountCents > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: Double,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = style,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatCurrency(amount),
            style = style,
            color = color,
            fontWeight = fontWeight
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDialog(
    bill: CreditCardBill,
    accounts: List<com.ccxiaoji.feature.ledger.domain.model.Account>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Int, fromAccountId: String?) -> Unit
) {
    var paymentAmount by remember { mutableStateOf(bill.totalAmountYuan.toString()) }
    var selectedOption by remember { mutableStateOf(0) } // 0: 全额, 1: 最低, 2: 自定义
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }
    
    // 过滤掉信用卡账户，只显示可用于还款的账户
    val paymentAccounts = accounts.filter { 
        it.type != com.ccxiaoji.feature.ledger.domain.model.AccountType.CREDIT_CARD
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("信用卡还款") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "应还金额：${formatCurrency(bill.totalAmountYuan)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // 还款选项
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 0,
                            onClick = { 
                                selectedOption = 0
                                paymentAmount = bill.totalAmountYuan.toString()
                            }
                        )
                        Text("全额还款")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 1,
                            onClick = { 
                                selectedOption = 1
                                paymentAmount = bill.minimumPaymentYuan.toString()
                            }
                        )
                        Text("最低还款（${formatCurrency(bill.minimumPaymentYuan)}）")
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedOption == 2,
                            onClick = { selectedOption = 2 }
                        )
                        Text("自定义金额")
                    }
                }
                
                // 金额输入框
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { 
                        paymentAmount = it
                        selectedOption = 2
                    },
                    label = { Text("还款金额") },
                    prefix = { Text("¥") },
                    enabled = selectedOption == 2,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 账户选择
                if (paymentAccounts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = paymentAccounts.find { it.id == selectedAccountId }?.name ?: "选择还款账户（可选）",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            paymentAccounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                formatCurrency(account.balanceCents / 100.0),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedAccountId = account.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Text(
                        "* 如果选择还款账户，将在该账户记录一笔支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = (paymentAmount.toDoubleOrNull() ?: 0.0) * 100
                    onConfirm(amount.toInt(), selectedAccountId)
                },
                enabled = paymentAmount.toDoubleOrNull() != null && paymentAmount.toDouble() > 0
            ) {
                Text("确认还款")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun formatBillingPeriod(startDate: Instant, endDate: Instant): String {
    val start = startDate.toLocalDateTime(TimeZone.currentSystemDefault())
    val end = endDate.toLocalDateTime(TimeZone.currentSystemDefault())
    
    return if (start.year == end.year && start.monthNumber == end.monthNumber) {
        "${start.year}年${start.monthNumber}月账单"
    } else {
        "${start.monthNumber}月${start.dayOfMonth}日-${end.monthNumber}月${end.dayOfMonth}日"
    }
}

private fun formatDate(instant: Instant): String {
    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
}

private fun formatDateTime(instant: Instant): String {
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${dateTime.monthNumber}月${dateTime.dayOfMonth}日 ${dateTime.hour}:${dateTime.minute.toString().padStart(2, '0')}"
}

