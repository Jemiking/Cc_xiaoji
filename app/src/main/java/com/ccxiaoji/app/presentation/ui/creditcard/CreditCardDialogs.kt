package com.ccxiaoji.app.presentation.ui.creditcard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.app.domain.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, creditLimitYuan: Double, billingDay: Int, paymentDueDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("") }
    var paymentDueDay by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.CreditCard, contentDescription = null)
        },
        title = {
            Text("添加信用卡")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("卡片名称") },
                    placeholder = { Text("如：招行信用卡") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("信用额度") },
                    placeholder = { Text("10000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text("元") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = billingDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                billingDay = filtered
                            }
                        },
                        label = { Text("账单日") },
                        placeholder = { Text("1-28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = paymentDueDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                paymentDueDay = filtered
                            }
                        },
                        label = { Text("还款日") },
                        placeholder = { Text("1-28") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Text(
                    text = "提示：账单日和还款日请填写1-28之间的数字",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limitYuan = creditLimit.toDoubleOrNull() ?: 0.0
                    val billing = billingDay.toIntOrNull() ?: 1
                    val payment = paymentDueDay.toIntOrNull() ?: 20
                    
                    if (name.isNotBlank() && limitYuan > 0 && billing in 1..28 && payment in 1..28) {
                        onConfirm(name, limitYuan, billing, payment)
                    }
                },
                enabled = name.isNotBlank() && 
                         (creditLimit.toDoubleOrNull() ?: 0.0) > 0 &&
                         (billingDay.toIntOrNull() ?: 0) in 1..28 &&
                         (paymentDueDay.toIntOrNull() ?: 0) in 1..28
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
    card: Account,
    onDismiss: () -> Unit,
    onPayment: (amountYuan: Double) -> Unit,
    onEdit: (creditLimitYuan: Double, billingDay: Int, paymentDueDay: Int) -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CreditCard, contentDescription = null)
                Text(card.name)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 额度信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("信用额度", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrency(card.creditLimitYuan ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("已用额度", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrency(-card.balanceYuan),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (card.balanceYuan < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("可用额度", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                formatCurrency(card.availableCreditYuan ?: 0.0),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        card.utilizationRate?.let { rate ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("使用率", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                LinearProgressIndicator(
                                    progress = { (rate.toFloat() / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp),
                                    color = when {
                                        rate <= 30 -> MaterialTheme.colorScheme.primary
                                        rate <= 70 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${rate.toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // 日期信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "账单日",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${card.billingDay}号",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "还款日",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${card.paymentDueDay}号",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // 操作按钮
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (card.balanceYuan < 0) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("还款")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("查看账单")
                    }
                    
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("编辑信息")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
    
    // 还款对话框
    if (showPaymentDialog) {
        PaymentDialog(
            currentDebt = -card.balanceYuan,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount ->
                onPayment(amount)
                showPaymentDialog = false
            }
        )
    }
    
    // 编辑对话框
    if (showEditDialog) {
        EditCreditCardDialog(
            card = card,
            onDismiss = { showEditDialog = false },
            onConfirm = { creditLimit, billingDay, paymentDueDay ->
                onEdit(creditLimit, billingDay, paymentDueDay)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    currentDebt: Double,
    onDismiss: () -> Unit,
    onConfirm: (amountYuan: Double) -> Unit
) {
    var paymentAmount by remember { mutableStateOf(currentDebt.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Payment, contentDescription = null)
        },
        title = {
            Text("信用卡还款")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "当前欠款：${formatCurrency(currentDebt)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("还款金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text("元") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { paymentAmount = currentDebt.toString() },
                        label = { Text("全额还款") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { paymentAmount = (currentDebt * 0.1).toString() },
                        label = { Text("最低还款") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount)
                    }
                },
                enabled = (paymentAmount.toDoubleOrNull() ?: 0.0) > 0
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
fun EditCreditCardDialog(
    card: Account,
    onDismiss: () -> Unit,
    onConfirm: (creditLimitYuan: Double, billingDay: Int, paymentDueDay: Int) -> Unit
) {
    var creditLimit by remember { mutableStateOf((card.creditLimitYuan ?: 0.0).toString()) }
    var billingDay by remember { mutableStateOf((card.billingDay ?: 1).toString()) }
    var paymentDueDay by remember { mutableStateOf((card.paymentDueDay ?: 20).toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Edit, contentDescription = null)
        },
        title = {
            Text("编辑信用卡信息")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("信用额度") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    suffix = { Text("元") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = billingDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                billingDay = filtered
                            }
                        },
                        label = { Text("账单日") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = paymentDueDay,
                        onValueChange = { 
                            val filtered = it.filter { char -> char.isDigit() }
                            if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) in 1..28) {
                                paymentDueDay = filtered
                            }
                        },
                        label = { Text("还款日") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        suffix = { Text("号") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limitYuan = creditLimit.toDoubleOrNull() ?: 0.0
                    val billing = billingDay.toIntOrNull() ?: 1
                    val payment = paymentDueDay.toIntOrNull() ?: 20
                    
                    if (limitYuan > 0 && billing in 1..28 && payment in 1..28) {
                        onConfirm(limitYuan, billing, payment)
                    }
                },
                enabled = (creditLimit.toDoubleOrNull() ?: 0.0) > 0 &&
                         (billingDay.toIntOrNull() ?: 0) in 1..28 &&
                         (paymentDueDay.toIntOrNull() ?: 0) in 1..28
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}