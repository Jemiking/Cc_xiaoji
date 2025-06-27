package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.Category

/**
 * 批量删除确认对话框
 */
@Composable
fun BatchDeleteDialog(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(stringResource(R.string.batch_delete_confirm_title))
        },
        text = {
            Text(stringResource(R.string.batch_delete_confirm_message, selectedCount))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 批量修改分类对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchUpdateCategoryDialog(
    selectedCount: Int,
    categories: List<Category>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.batch_change_category))
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.batch_change_category_message, selectedCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分类选择器
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(categories) { category ->
                            ListItem(
                                headlineContent = { Text(category.name) },
                                leadingContent = {
                                    RadioButton(
                                        selected = selectedCategoryId == category.id,
                                        onClick = { selectedCategoryId = category.id }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedCategoryId.isNotEmpty()) {
                        onConfirm(selectedCategoryId)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 批量修改账户对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchUpdateAccountDialog(
    selectedCount: Int,
    accounts: List<Account>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.batch_change_account))
        },
        text = {
            Column {
                Text(
                    stringResource(R.string.batch_change_account_message, selectedCount),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 账户选择器
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(accounts) { account ->
                            ListItem(
                                headlineContent = { Text(account.name) },
                                supportingContent = {
                                    Text(
                                        stringResource(R.string.balance_with_amount, 
                                            account.balanceCents / 100.0),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                leadingContent = {
                                    RadioButton(
                                        selected = selectedAccountId == account.id,
                                        onClick = { selectedAccountId = account.id }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedAccountId.isNotEmpty()) {
                        onConfirm(selectedAccountId)
                        onDismiss()
                    }
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * 批量操作结果提示
 */
@Composable
fun BatchOperationResultSnackbar(
    message: String,
    isSuccess: Boolean = true,
    onDismiss: () -> Unit = {},
    onUndo: (() -> Unit)? = null
) {
    Snackbar(
        action = if (onUndo != null) {
            {
                TextButton(onClick = onUndo) {
                    Text(stringResource(R.string.undo))
                }
            }
        } else null,
        dismissAction = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
        },
        containerColor = if (isSuccess) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(message)
    }
}