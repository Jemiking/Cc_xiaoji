package com.ccxiaoji.feature.ledger.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.api.AccountItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelector(
    accounts: List<AccountItem>,
    selectedAccount: AccountItem?,
    onAccountSelected: (AccountItem) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "选择账户"
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedAccount?.let { 
                if (it.type == "CREDIT_CARD") {
                    "${getAccountTypeIcon(it.type)} ${it.name} (可用: ¥%.2f)".format(it.availableCreditYuan ?: 0.0)
                } else {
                    "${getAccountTypeIcon(it.type)} ${it.name}"
                }
            } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = getAccountTypeIcon(account.type))
                                Column {
                                    Text(text = account.name)
                                    if (account.type == "CREDIT_CARD") {
                                        // 信用卡显示可用额度
                                        Text(
                                            text = "可用: ¥%.2f".format(account.availableCreditYuan ?: 0.0),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        // 普通账户显示余额
                                        Text(
                                            text = "¥%.2f".format(account.balanceYuan),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (account.isDefault) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "默认",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CompactAccountSelector(
    accounts: List<AccountItem>,
    selectedAccount: AccountItem?,
    onAccountSelected: (AccountItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedAccount != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getAccountTypeIcon(selectedAccount.type))
                        Text(
                            text = selectedAccount.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (selectedAccount.type == "CREDIT_CARD") {
                        Text(
                            text = "可用: ¥%.2f".format(selectedAccount.availableCreditYuan ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "¥%.2f".format(selectedAccount.balanceYuan),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "选择账户",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = getAccountTypeIcon(account.type))
                                Text(text = account.name)
                            }
                            if (account.type == "CREDIT_CARD") {
                                Text(
                                    text = "可用: ¥%.2f".format(account.availableCreditYuan ?: 0.0),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "¥%.2f".format(account.balanceYuan),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    onClick = {
                        onAccountSelected(account)
                        expanded = false
                    }
                )
            }
        }
    }
}

// 辅助函数
private fun getAccountTypeIcon(type: String): String {
    return when (type) {
        "CASH" -> "💵"
        "BANK_CARD" -> "💳"
        "ALIPAY" -> "📱"
        "WECHAT" -> "💬"
        "CREDIT_CARD" -> "💳"
        else -> "📋"
    }
}