package com.ccxiaoji.feature.ledger.presentation.screen.account.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.ccxiaoji.feature.ledger.domain.model.AccountType
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    // 根据账户类型选择颜色
    val accountColor = when (account.type) {
        AccountType.BANK -> DesignTokens.BrandColors.Info
        AccountType.CASH -> DesignTokens.BrandColors.Success
        AccountType.CREDIT_CARD -> DesignTokens.BrandColors.Warning
        AccountType.ALIPAY -> DesignTokens.BrandColors.Success
        AccountType.WECHAT -> DesignTokens.BrandColors.Info
        AccountType.OTHER -> MaterialTheme.colorScheme.secondary
    }
    
    Box {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            backgroundColor = MaterialTheme.colorScheme.surface,
            borderColor = accountColor.copy(alpha = 0.2f),
            onClick = null // Handle click through modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DesignTokens.Spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Account Icon - 扁平化设计
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = accountColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = account.icon ?: account.type.icon,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (account.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.account_default_label),
                                        modifier = Modifier.padding(
                                            horizontal = DesignTokens.Spacing.xs,
                                            vertical = 2.dp
                                        ),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                        Text(
                            text = account.type.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Text(
                    text = stringResource(
                        R.string.amount_format, 
                        stringResource(R.string.currency_symbol), 
                        account.balanceYuan
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balanceYuan >= 0) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        DesignTokens.BrandColors.Error
                    }
                )
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (!account.isDefault) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.account_set_as_default)) },
                    onClick = {
                        onSetDefault()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                onClick = {
                    onDelete()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Error
                    )
                }
            )
        }
    }
}