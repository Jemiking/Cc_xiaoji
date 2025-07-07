package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatButton

@Composable
fun EmptyBudgetState(
    onAddBudget: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignTokens.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        Text(
            text = "暂无预算设置",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
        
        Text(
            text = "设置预算，合理控制支出",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
        
        FlatButton(
            text = "设置预算",
            onClick = onAddBudget,
            backgroundColor = DesignTokens.BrandColors.Ledger
        )
    }
}