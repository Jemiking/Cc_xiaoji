package com.ccxiaoji.feature.ledger.presentation.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.api.AccountItem
import com.ccxiaoji.feature.ledger.presentation.ui.components.AccountSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountTransferDialog(
    accounts: List<AccountItem>,
    onDismiss: () -> Unit,
    onConfirm: (fromAccountId: String, toAccountId: String, amountCents: Long) -> Unit
) {
    var fromAccount by remember { mutableStateOf<AccountItem?>(null) }
    var toAccount by remember { mutableStateOf<AccountItem?>(null) }
    var amount by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("账户转账") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // From Account Selection
                AccountSelector(
                    accounts = accounts,
                    selectedAccount = fromAccount,
                    onAccountSelected = { fromAccount = it },
                    label = "转出账户"
                )
                
                // To Account Selection
                AccountSelector(
                    accounts = accounts.filter { it.id != fromAccount?.id },
                    selectedAccount = toAccount,
                    onAccountSelected = { toAccount = it },
                    label = "转入账户"
                )
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("转账金额") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Balance hint
                fromAccount?.let { account ->
                    Text(
                        text = "转出账户余额：¥%.2f".format(account.balanceYuan),
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
                    val from = fromAccount?.id ?: return@TextButton
                    val to = toAccount?.id ?: return@TextButton
                    onConfirm(from, to, amountCents)
                },
                enabled = amount.isNotEmpty() && 
                         fromAccount != null && 
                         toAccount != null && 
                         fromAccount?.id != toAccount?.id &&
                         (amount.toDoubleOrNull() ?: 0.0) <= (fromAccount?.balanceYuan ?: 0.0)
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