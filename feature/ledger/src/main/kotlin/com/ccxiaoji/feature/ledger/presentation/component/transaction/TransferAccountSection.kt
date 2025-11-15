package com.ccxiaoji.feature.ledger.presentation.component.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import kotlinx.datetime.Instant

/**
 * 转账账户选择区域组件
 *
 * 展示转账的转出和转入账户，分别显示在两张卡片中。
 * 内部复用AccountSelectionSection组件，减少代码重复。
 *
 * @param fromAccount 转出账户，null表示未选择
 * @param toAccount 转入账户，null表示未选择
 * @param onFromAccountClick 点击转出账户卡片的回调函数
 * @param onToAccountClick 点击转入账户卡片的回调函数
 * @param modifier 修饰符，用于自定义组件布局
 * @param fromPlaceholder 转出账户未选择时的占位文本
 * @param toPlaceholder 转入账户未选择时的占位文本
 */
@Composable
fun TransferAccountSection(
    fromAccount: Account?,
    toAccount: Account?,
    onFromAccountClick: () -> Unit,
    onToAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    fromPlaceholder: String = "选择转出账户",
    toPlaceholder: String = "选择转入账户"
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 转出账户卡片 - 复用AccountSelectionSection
        val fromAccountModified = fromAccount?.let {
            it.copy(name = "从: ${it.name}")
        }
        AccountSelectionSection(
            selectedAccount = fromAccountModified,
            onAccountClick = onFromAccountClick,
            placeholderText = "从: $fromPlaceholder"
        )

        // 转入账户卡片 - 复用AccountSelectionSection
        val toAccountModified = toAccount?.let {
            it.copy(name = "到: ${it.name}")
        }
        AccountSelectionSection(
            selectedAccount = toAccountModified,
            onAccountClick = onToAccountClick,
            placeholderText = "到: $toPlaceholder"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TransferAccountSectionPreview() {
    val fromAccount = Account(
        id = "1",
        name = "工资卡",
        type = AccountType.BANK,
        balanceCents = 10_000_00,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    val toAccount = Account(
        id = "2",
        name = "储蓄卡",
        type = AccountType.BANK,
        balanceCents = 5_000_00,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    TransferAccountSection(
        fromAccount = fromAccount,
        toAccount = toAccount,
        onFromAccountClick = { },
        onToAccountClick = { }
    )
}

@Preview(showBackground = true)
@Composable
fun TransferAccountSectionEmptyPreview() {
    TransferAccountSection(
        fromAccount = null,
        toAccount = null,
        onFromAccountClick = { },
        onToAccountClick = { }
    )
}

@Preview(showBackground = true)
@Composable
fun TransferAccountSectionPartialPreview() {
    val fromAccount = Account(
        id = "3",
        name = "现金账户",
        type = AccountType.CASH,
        balanceCents = 1_000_00,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z")
    )

    TransferAccountSection(
        fromAccount = fromAccount,
        toAccount = null,
        onFromAccountClick = { },
        onToAccountClick = { },
        toPlaceholder = "请选择转入账户"
    )
}