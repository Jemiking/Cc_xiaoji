package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import kotlinx.datetime.Instant

/**
 * 账户选择对话框
 *
 * 显示账户列表供用户选择，支持高亮当前选中项。
 *
 * @param title 对话框标题
 * @param accounts 可选的账户列表
 * @param selectedAccount 当前选中的账户
 * @param onAccountSelected 账户选择回调
 * @param onDismiss 关闭对话框回调
 */
@Composable
fun AccountPickerDialog(
    title: String = "选择账户",
    accounts: List<Account>,
    selectedAccount: Account? = null,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    AccountItemCard(
                        account = account,
                        isSelected = selectedAccount?.id == account.id,
                        onSelected = {
                            onAccountSelected(account)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 账户项卡片
 *
 * 显示单个账户信息，包括账户类型图标、名称、余额等。
 * 选中状态会有特殊的视觉效果。
 */
@Composable
private fun AccountItemCard(
    account: Account,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 账户类型图标
                Text(
                    text = account.type.icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    // 账户名称
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    // 账户余额
                    if (account.type == AccountType.CREDIT_CARD) {
                        Text(
                            text = "可用: ¥${String.format("%.2f", account.availableCreditYuan ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DesignTokens.BrandColors.Success
                        )
                    } else {
                        Text(
                            text = "¥${String.format("%.2f", account.balanceYuan)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // 默认账户标记
            if (account.isDefault) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "默认",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountPickerDialogPreview() {
    val accounts = listOf(
        Account(
            id = "1",
            name = "现金账户",
            type = AccountType.CASH,
            balanceCents = 1000_00,
            isDefault = true,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST
        ),
        Account(
            id = "2",
            name = "工商银行",
            type = AccountType.BANK,
            balanceCents = 50_000_00,
            isDefault = false,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST
        ),
        Account(
            id = "3",
            name = "招商信用卡",
            type = AccountType.CREDIT_CARD,
            balanceCents = -2000_00,
            creditLimitCents = 10_000_00,
            isDefault = false,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST
        )
    )

    AccountPickerDialog(
        title = "选择账户",
        accounts = accounts,
        selectedAccount = accounts[0],
        onAccountSelected = {},
        onDismiss = {}
    )
}