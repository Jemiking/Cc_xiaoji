package com.ccxiaoji.feature.ledger.presentation.screen.savings.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 快速操作卡片
 */
@Composable
fun QuickActionsCard(
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FlatButton(
                onClick = onDeposit,
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text("存入")
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            
            FlatButton(
                onClick = onWithdraw,
                modifier = Modifier.weight(1f),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                borderColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text("取出")
            }
        }
    }
}