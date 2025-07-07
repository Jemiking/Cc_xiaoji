package com.ccxiaoji.feature.ledger.presentation.screen.savings.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.components.FlatSelectChip
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化储蓄贡献对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatContributionDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String?, isDeposit: Boolean) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isDeposit by remember { mutableStateOf(true) }
    var amountError by remember { mutableStateOf<String?>(null) }
    
    FlatDialog(
        title = if (isDeposit) "存入金额" else "取出金额",
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
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
            }) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 副标题
            Text(
                text = "储蓄目标: $goalName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
            )
            
            // 存入/取出切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FlatSelectChip(
                    selected = isDeposit,
                    onSelectedChange = { isDeposit = true },
                    label = "存入",
                    selectedIcon = Icons.Default.Add,
                    modifier = Modifier.padding(end = DesignTokens.Spacing.small)
                )
                
                FlatSelectChip(
                    selected = !isDeposit,
                    onSelectedChange = { isDeposit = false },
                    label = "取出",
                    selectedIcon = Icons.Default.Remove
                )
            }
            
            // 金额输入
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
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
            
            // 备注输入
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注 (可选)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}