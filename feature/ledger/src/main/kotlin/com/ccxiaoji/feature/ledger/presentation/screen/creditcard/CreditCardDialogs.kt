package com.ccxiaoji.feature.ledger.presentation.screen.creditcard

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
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.presentation.utils.CurrencyFormatter.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, creditLimitYuan: Double, usedAmountYuan: Double, billingDay: Int, paymentDueDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var usedAmount by remember { mutableStateOf("") }
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
                
                OutlinedTextField(
                    value = usedAmount,
                    onValueChange = { usedAmount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("当前已用额度") },
                    placeholder = { Text("0") },
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
                    val usedYuan = usedAmount.toDoubleOrNull() ?: 0.0
                    val billing = billingDay.toIntOrNull() ?: 1
                    val payment = paymentDueDay.toIntOrNull() ?: 20
                    
                    if (name.isNotBlank() && limitYuan > 0 && billing in 1..28 && payment in 1..28) {
                        onConfirm(name, limitYuan, usedYuan, billing, payment)
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

