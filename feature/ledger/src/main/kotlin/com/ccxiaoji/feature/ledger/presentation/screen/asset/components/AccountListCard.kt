package com.ccxiaoji.feature.ledger.presentation.screen.asset.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.AssetDistribution
import com.ccxiaoji.feature.ledger.domain.model.AssetItem
import java.text.DecimalFormat

/**
 * 账户列表卡片
 */
@Composable
fun AccountListCard(distribution: AssetDistribution) {
    val decimalFormat = remember { DecimalFormat("#,##0.00") }
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "账户列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 资产账户
            if (distribution.assetItems.isNotEmpty()) {
                Text(
                    text = "资产账户",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                distribution.assetItems.forEach { item ->
                    AccountItem(item = item, decimalFormat = decimalFormat)
                }
            }
            
            // 负债账户
            if (distribution.liabilityItems.isNotEmpty()) {
                if (distribution.assetItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                }
                
                Text(
                    text = "负债账户",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                
                distribution.liabilityItems.forEach { item ->
                    AccountItem(item = item, decimalFormat = decimalFormat)
                }
            }
        }
    }
}

/**
 * 账户项
 */
@Composable
fun AccountItem(
    item: AssetItem,
    decimalFormat: DecimalFormat
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DesignTokens.Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (item.accountType) {
                "CREDIT_CARD" -> Icons.Default.CreditCard
                "CASH" -> Icons.Default.AccountBalanceWallet
                else -> Icons.Default.AccountBalance
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.accountName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${String.format("%.1f", item.percentage)}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "¥${decimalFormat.format(item.balance)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (item.isAsset) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}