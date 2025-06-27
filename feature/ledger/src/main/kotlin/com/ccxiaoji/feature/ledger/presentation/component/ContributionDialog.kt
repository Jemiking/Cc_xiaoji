package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String?, isDeposit: Boolean) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDeposit by remember { mutableStateOf(true) }
    var amountError by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isDeposit) "存入金额" else "取出金额",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Text(
                    text = "储蓄目标: $goalName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Deposit/Withdrawal toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    FilterChip(
                        selected = isDeposit,
                        onClick = { isDeposit = true },
                        label = { Text("存入") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    FilterChip(
                        selected = !isDeposit,
                        onClick = { isDeposit = false },
                        label = { Text("取出") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it.filter { char -> char.isDigit() || char == '.' }
                        amountError = null
                    },
                    label = { Text("金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val amountValue = amount.toDoubleOrNull()
                            when {
                                amountValue == null || amountValue <= 0 -> {
                                    amountError = "请输入有效金额"
                                }
                                else -> {
                                    val finalAmount = if (isDeposit) amountValue else -amountValue
                                    onConfirm(finalAmount, note.ifBlank { null }, isDeposit)
                                }
                            }
                        }
                    ) {
                        Text("确认")
                    }
                }
            }
        }
    }
}