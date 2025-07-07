package com.ccxiaoji.feature.ledger.presentation.screen.account.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.ui.theme.DesignTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: AccountType, initialBalanceCents: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var balance by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.add_account),
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
                
                // Account Type Selection
                ExposedDropdownMenuBox(
                    expanded = showTypeDropdown,
                    onExpandedChange = { showTypeDropdown = !showTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = "${selectedType.icon} ${selectedType.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.account_type)) },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showTypeDropdown,
                        onDismissRequest = { showTypeDropdown = false }
                    ) {
                        AccountType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            DesignTokens.Spacing.small
                                        )
                                    ) {
                                        Text(type.icon)
                                        Text(type.displayName)
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    showTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Initial Balance
                OutlinedTextField(
                    value = balance,
                    onValueChange = { newValue ->
                        balance = newValue.filter { char -> 
                            char.isDigit() || char == '.' || char == '-'
                        }
                    },
                    label = { Text(stringResource(R.string.initial_balance)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceCents = ((balance.toDoubleOrNull() ?: 0.0) * 100).toLong()
                    onConfirm(name, selectedType, balanceCents)
                },
                enabled = name.isNotEmpty()
            ) {
                Text(stringResource(R.string.account_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}