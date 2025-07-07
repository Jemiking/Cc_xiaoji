package com.ccxiaoji.feature.ledger.presentation.screen.savings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.EmptyStateView

/**
 * 空贡献记录状态
 */
@Composable
fun EmptyContributionState() {
    EmptyStateView(
        icon = Icons.Default.AccountBalanceWallet,
        title = "暂无存款记录",
        description = "点击下方「记录存款」按钮开始储蓄",
        modifier = Modifier.fillMaxWidth()
    )
}