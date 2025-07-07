package com.ccxiaoji.feature.ledger.presentation.screen.account.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (fromId: String, toId: String, amountCents: Long) -> Unit
) {
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var amount by remember { mutableStateOf("") }
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("账户转账") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // From Account
                ExposedDropdownMenuBox(
                    expanded = showFromDropdown,
                    onExpandedChange = { showFromDropdown = !showFromDropdown }
                ) {
                    OutlinedTextField(
                        value = fromAccount?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("从账户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFromDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showFromDropdown,
                        onDismissRequest = { showFromDropdown = false }
                    ) {
                        accounts.forEach { account ->
                            if (account.id != toAccount?.id) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                text = "¥${account.balanceYuan}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        fromAccount = account
                                        showFromDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // To Account
                ExposedDropdownMenuBox(
                    expanded = showToDropdown,
                    onExpandedChange = { showToDropdown = !showToDropdown }
                ) {
                    OutlinedTextField(
                        value = toAccount?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("到账户") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showToDropdown,
                        onDismissRequest = { showToDropdown = false }
                    ) {
                        accounts.forEach { account ->
                            if (account.id != fromAccount?.id) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(account.name)
                                            Text(
                                                text = "¥${account.balanceYuan}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        toAccount = account
                                        showToDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Transfer Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("转账金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Available balance hint
                fromAccount?.let { account ->
                    Text(
                        text = "可用余额：¥${account.balanceYuan}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountCents = ((amount.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    if (fromAccount != null && toAccount != null) {
                        onConfirm(fromAccount!!.id, toAccount!!.id, amountCents)
                    }
                },
                enabled = fromAccount != null && toAccount != null && amount.isNotEmpty() && 
                         (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("转账")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}