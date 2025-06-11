package com.ccxiaoji.feature.ledger.presentation.ui.creditcard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.ledger.api.AccountItem
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, creditLimit: Double, usedAmount: Double, billingDay: Int, paymentDueDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var usedAmount by remember { mutableStateOf("0") }
    var billingDay by remember { mutableIntStateOf(1) }
    var paymentDueDay by remember { mutableIntStateOf(20) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加信用卡") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("信用卡名称") },
                    placeholder = { Text("例如：招商银行信用卡") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("信用额度") },
                    placeholder = { Text("例如：10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text("元") }
                )
                
                OutlinedTextField(
                    value = usedAmount,
                    onValueChange = { usedAmount = it },
                    label = { Text("已使用额度") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text("元") },
                    supportingText = { Text("新卡填0，如有欠款请填写欠款金额") }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "账单日",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = billingDay.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { day ->
                                    if (day in 1..28) billingDay = day
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = { Text("号") }
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "还款日",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = paymentDueDay.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { day ->
                                    if (day in 1..28) paymentDueDay = day
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = { Text("号") }
                        )
                    }
                }
                
                Text(
                    "* 账单日和还款日限制在1-28号，避免月份问题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limit = creditLimit.toDoubleOrNull() ?: 0.0
                    val used = usedAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && limit > 0) {
                        onConfirm(name, limit, used, billingDay, paymentDueDay)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditCardDetailDialog(
    card: AccountItem,
    onDismiss: () -> Unit,
    onPayment: (Double) -> Unit,
    onEdit: (creditLimit: Double, usedAmount: Double, billingDay: Int, paymentDueDay: Int) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onViewPaymentHistory: () -> Unit,
    onViewBills: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 额度信息
                card.creditLimitCents?.let { limitCents ->
                    CreditCardInfoSection(card)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 第一行按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("还款")
                        }
                        
                        OutlinedButton(
                            onClick = onViewBills,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("账单")
                        }
                    }
                    
                    // 第二行按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onViewPaymentHistory,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("还款记录")
                        }
                        
                        OutlinedButton(
                            onClick = onNavigateToTransactions,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("交易明细")
                        }
                    }
                    
                    // 编辑按钮
                    TextButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑信息")
                    }
                }
            }
        }
    }
    
    // 编辑对话框
    if (showEditDialog) {
        EditCreditCardDialog(
            card = card,
            onDismiss = { showEditDialog = false },
            onConfirm = { creditLimit, usedAmount, billingDay, paymentDueDay ->
                onEdit(creditLimit, usedAmount, billingDay, paymentDueDay)
                showEditDialog = false
            }
        )
    }
    
    // 还款对话框
    if (showPaymentDialog) {
        PaymentDialog(
            card = card,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount ->
                onPayment(amount)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun CreditCardInfoSection(card: AccountItem) {
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val utilizationRate = if (card.creditLimitCents != null && card.creditLimitCents > 0) {
        (-card.balanceCents.toDouble() / card.creditLimitCents) * 100
    } else 0.0
    
    val utilizationColor = when {
        utilizationRate >= 90 -> MaterialTheme.colorScheme.error
        utilizationRate >= 70 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 额度使用情况
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "可用额度",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        currencyFormatter.format(card.availableCreditYuan),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // 使用率进度条
                Column {
                    LinearProgressIndicator(
                        progress = { (utilizationRate / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = utilizationColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "使用率 ${String.format("%.1f", utilizationRate)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = utilizationColor
                    )
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "已使用",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            currencyFormatter.format(-card.balanceYuan),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "信用额度",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            currencyFormatter.format(card.creditLimitYuan ?: 0.0),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        
        // 账单日和还款日信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            card.billingDay?.let { billingDay ->
                InfoChip(
                    label = "账单日",
                    value = "$billingDay 号",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
            }
            
            card.paymentDueDay?.let { dueDay ->
                InfoChip(
                    label = "还款日",
                    value = "$dueDay 号",
                    icon = Icons.Default.DateRange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun EditCreditCardDialog(
    card: AccountItem,
    onDismiss: () -> Unit,
    onConfirm: (creditLimit: Double, usedAmount: Double, billingDay: Int, paymentDueDay: Int) -> Unit
) {
    var creditLimit by remember { mutableStateOf((card.creditLimitYuan ?: 0.0).toString()) }
    var usedAmount by remember { mutableStateOf((-card.balanceYuan).toString()) }
    var billingDay by remember { mutableIntStateOf(card.billingDay ?: 1) }
    var paymentDueDay by remember { mutableIntStateOf(card.paymentDueDay ?: 20) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑信用卡信息") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("信用额度") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text("元") }
                )
                
                OutlinedTextField(
                    value = usedAmount,
                    onValueChange = { usedAmount = it },
                    label = { Text("已使用额度") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text("元") }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "账单日",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = billingDay.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { day ->
                                    if (day in 1..28) billingDay = day
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = { Text("号") }
                        )
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "还款日",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = paymentDueDay.toString(),
                            onValueChange = { 
                                it.toIntOrNull()?.let { day ->
                                    if (day in 1..28) paymentDueDay = day
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = { Text("号") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limit = creditLimit.toDoubleOrNull() ?: 0.0
                    val used = usedAmount.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onConfirm(limit, used, billingDay, paymentDueDay)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun PaymentDialog(
    card: AccountItem,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var paymentAmount by remember { mutableStateOf("") }
    val debtAmount = -card.balanceYuan
    val minimumPayment = debtAmount * 0.1
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("信用卡还款") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 欠款信息
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "当前欠款",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "¥ ${String.format("%.2f", debtAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // 还款金额输入
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    label = { Text("还款金额") },
                    placeholder = { Text("请输入还款金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("¥") }
                )
                
                // 快捷选项
                Text(
                    "快捷选项",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 全额还款
                    FilterChip(
                        selected = paymentAmount == String.format("%.2f", debtAmount),
                        onClick = { paymentAmount = String.format("%.2f", debtAmount) },
                        label = { Text("全额还款") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 最低还款
                    FilterChip(
                        selected = paymentAmount == String.format("%.2f", minimumPayment),
                        onClick = { paymentAmount = String.format("%.2f", minimumPayment) },
                        label = { Text("最低还款") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Text(
                    "最低还款额：¥${String.format("%.2f", minimumPayment)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = paymentAmount.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                }
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