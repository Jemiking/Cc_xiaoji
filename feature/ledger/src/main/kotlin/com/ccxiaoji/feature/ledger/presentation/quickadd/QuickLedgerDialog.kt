package com.ccxiaoji.feature.ledger.presentation.quickadd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.StateFlow

@Composable
fun QuickLedgerDialog(
    uiStateFlow: StateFlow<QuickLedgerUiState>,
    onConfirm: (QuickLedgerUiState) -> Unit,
    onCancel: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onEditNote: (String) -> Unit
) {
    val state by uiStateFlow.collectAsState()

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("快速记账确认") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("金额：¥%.2f".format(state.amountCents / 100.0))
                Text("方向：${if (state.direction == "INCOME") "收入" else "支出"}")
                Text("商户：${state.merchant ?: "未知"}")
                val showAccounts = remember { mutableStateOf(false) }
                val showCategories = remember { mutableStateOf(false) }
                val accountSearch = remember { mutableStateOf("") }
                val categorySearch = remember { mutableStateOf("") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = state.accounts.firstOrNull { it.id == state.accountId }?.name ?: (state.accountId ?: ""),
                            onValueChange = { accountSearch.value = it; showAccounts.value = true },
                            label = { Text("账户") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = showAccounts.value, onDismissRequest = { showAccounts.value = false }) {
                            val filtered = state.accounts.filter { it.name.contains(accountSearch.value, true) || it.id.contains(accountSearch.value, true) }
                            filtered.take(20).forEach { opt ->
                                DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                    onSelectAccount(opt.id)
                                    showAccounts.value = false
                                })
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = state.categories.firstOrNull { it.id == state.categoryId }?.name ?: (state.categoryId ?: ""),
                            onValueChange = { categorySearch.value = it; showCategories.value = true },
                            label = { Text("分类") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = showCategories.value, onDismissRequest = { showCategories.value = false }) {
                            val filtered = state.categories.filter { it.name.contains(categorySearch.value, true) || it.id.contains(categorySearch.value, true) }
                            filtered.take(20).forEach { opt ->
                                DropdownMenuItem(text = { Text(opt.name) }, onClick = {
                                    onSelectCategory(opt.id)
                                    showCategories.value = false
                                })
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = state.note ?: "",
                    onValueChange = onEditNote,
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
                if (state.error != null) {
                    Text("错误：${state.error}")
                }
            }
        },
        confirmButton = {
            val canConfirm = !state.loading && state.amountCents > 0 && !state.accountId.isNullOrBlank() && !state.categoryId.isNullOrBlank()
            Button(onClick = { onConfirm(state) }, enabled = canConfirm) {
                Text(if (state.loading) "处理中…" else "记账")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !state.loading) {
                Text("取消")
            }
        }
    )
}
