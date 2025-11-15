package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import kotlinx.datetime.Instant

/**
 * 账户选择区域组件
 *
 * 展示当前选中的账户信息，点击后触发账户选择流程。
 *
 * @param selectedAccount 当前选中的账户，null表示未选择
 * @param onAccountClick 点击账户卡片的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param placeholderText 未选择账户时显示的占位文本
 * @param trailingIcon 卡片右侧显示的图标
 *
 * @sample AccountSelectionSectionPreview
 */
@Composable
fun AccountSelectionSection(
    selectedAccount: Account?,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "选择账户",
    trailingIcon: ImageVector = Icons.Default.ArrowForward
) {
    Card(
        onClick = onAccountClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedAccount?.name ?: placeholderText,
                style = MaterialTheme.typography.bodyLarge
            )
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true, name = "No Account Selected")
@Composable
private fun AccountSelectionSectionEmptyPreview() {
    AccountSelectionSection(
        selectedAccount = null,
        onAccountClick = {}
    )
}

@Preview(showBackground = true, name = "Cash Account")
@Composable
private fun AccountSelectionSectionCashPreview() {
    val account = Account(
        id = "1",
        name = "现金账户",
        type = AccountType.CASH,
        balanceCents = 123_45,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )
    AccountSelectionSection(
        selectedAccount = account,
        onAccountClick = {}
    )
}

@Preview(showBackground = true, name = "Bank Account")
@Composable
private fun AccountSelectionSectionBankPreview() {
    val account = Account(
        id = "2",
        name = "工商银行",
        type = AccountType.BANK,
        balanceCents = 10_000_00,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )
    AccountSelectionSection(
        selectedAccount = account,
        onAccountClick = {}
    )
}

