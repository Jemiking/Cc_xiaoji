package com.ccxiaoji.feature.ledger.presentation.screen.account.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountDialog(
    account: Account,
    onDismiss: () -> Unit,
    onConfirm: (Account) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var balance by remember { mutableStateOf(account.balanceYuan.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.edit_account),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.account_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Account Type (Read-only)
                OutlinedTextField(
                    value = "${account.type.icon} ${account.type.displayName}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.account_type)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                )
                
                // Balance
                OutlinedTextField(
                    value = balance,
                    onValueChange = { newValue ->
                        balance = newValue.filter { char -> 
                            char.isDigit() || char == '.' || char == '-' 
                        }
                    },
                    label = { Text(stringResource(R.string.account_current_balance)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceCents = ((balance.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    val updatedAccount = account.copy(
                        name = name,
                        balanceCents = balanceCents,
                        updatedAt = Instant.DISTANT_FUTURE // Will be set by repository
                    )
                    onConfirm(updatedAccount)
                },
                enabled = name.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}