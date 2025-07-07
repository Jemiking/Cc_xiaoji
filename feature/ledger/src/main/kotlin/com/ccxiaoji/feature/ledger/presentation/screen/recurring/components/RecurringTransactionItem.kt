package com.ccxiaoji.feature.ledger.presentation.screen.recurring.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.feature.ledger.data.local.entity.RecurringTransactionEntity
import com.ccxiaoji.common.model.RecurringFrequency
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionItem(
    transaction: RecurringTransactionEntity,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    formatNextExecutionDate: (Long) -> String,
    getFrequencyText: (RecurringTransactionEntity) -> String
) {
    var showMenu by remember { mutableStateOf(false) }
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (transaction.isEnabled) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        borderColor = if (transaction.isEnabled) {
            if (transaction.amountCents >= 0) {
                DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
            } else {
                DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
            }
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        },
        // shape removed - ModernCard doesn't support this parameter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (transaction.isEnabled) {
                            if (transaction.amountCents >= 0) {
                                DesignTokens.BrandColors.Success.copy(alpha = 0.1f)
                            } else {
                                DesignTokens.BrandColors.Error.copy(alpha = 0.1f)
                            }
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.amountCents >= 0) {
                        Icons.Default.TrendingUp
                    } else {
                        Icons.Default.TrendingDown
                    },
                    contentDescription = null,
                    tint = if (transaction.isEnabled) {
                        if (transaction.amountCents >= 0) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 交易信息
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Text(
                        text = transaction.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (transaction.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                    if (!transaction.isEnabled) {
                        Surface(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                        ) {
                            Text(
                                text = "已暂停",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Text(
                    text = buildString {
                        if (transaction.amountCents >= 0) append("+")
                        append(NumberFormat.getCurrencyInstance(Locale.CHINA).format(
                            transaction.amountCents / 100.0
                        ))
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (transaction.isEnabled) {
                        if (transaction.amountCents >= 0) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    Text(
                        text = getFrequencyText(transaction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    if (transaction.isEnabled) {
                        Text(
                            text = "下次: ${formatNextExecutionDate(transaction.nextExecutionDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // 操作按钮
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Text(if (transaction.isEnabled) "暂停" else "启用")
                        },
                        onClick = {
                            onToggleEnabled()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                if (transaction.isEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            onEdit()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("删除") },
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
    }
}