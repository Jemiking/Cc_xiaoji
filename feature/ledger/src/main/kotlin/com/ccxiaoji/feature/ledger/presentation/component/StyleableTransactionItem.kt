package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.foundation.BorderStroke
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
import com.ccxiaoji.feature.ledger.domain.model.LedgerUIStyle
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 支持多种风格的交易项组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StyleableTransactionItem(
    transaction: Transaction,
    style: LedgerUIStyle,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onItemClick: () -> Unit = {},
    onItemLongClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCopy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when (style) {
        LedgerUIStyle.BALANCED -> BalancedTransactionItem(
            transaction = transaction,
            isSelected = isSelected,
            isSelectionMode = isSelectionMode,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            onEdit = onEdit,
            onDelete = onDelete,
            onCopy = onCopy,
            modifier = modifier
        )
        LedgerUIStyle.HIERARCHICAL -> HierarchicalTransactionItem(
            transaction = transaction,
            isSelected = isSelected,
            isSelectionMode = isSelectionMode,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick,
            onEdit = onEdit,
            onDelete = onDelete,
            onCopy = onCopy,
            modifier = modifier
        )
    }
}

/**
 * 平衡风格的交易项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BalancedTransactionItem(
    transaction: Transaction,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val amountColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val categoryColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(modifier = modifier) {
        // 紧凑的交易项设计
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onItemClick,
                    onLongClick = {
                        if (!isSelectionMode) {
                            showMenu = true
                        }
                    }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = DesignTokens.BrandColors.Ledger
                            )
                        )
                    }
                    
                    // 更小的图标
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.categoryDetails?.icon ?: "📝",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Column {
                        Text(
                            text = transaction.categoryDetails?.name ?: stringResource(R.string.uncategorized),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // 只显示一行备注（如果有）
                        transaction.note?.let { note ->
                            Text(
                                text = if (note.length > 15) "${note.take(15)}..." else note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (transaction.categoryDetails?.type == "INCOME") {
                            "+¥${transaction.amountYuan}"
                        } else {
                            "-¥${transaction.amountYuan}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = amountColor
                    )
                    Text(
                        text = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                            .toJavaLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("MM-dd")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // 上下文菜单
        if (showMenu) {
            TransactionContextMenu(
                onEdit = {
                    onEdit()
                    showMenu = false
                },
                onDelete = {
                    onDelete()
                    showMenu = false
                },
                onCopy = {
                    onCopy()
                    showMenu = false
                },
                onDismiss = { showMenu = false }
            )
        }
    }
}

/**
 * 层次化风格的交易项
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HierarchicalTransactionItem(
    transaction: Transaction,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    
    val amountColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(modifier = modifier) {
        // 极简化的交易项设计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    RoundedCornerShape(4.dp)
                )
                .combinedClickable(
                    onClick = onItemClick,
                    onLongClick = {
                        if (!isSelectionMode) {
                            showMenu = true
                        }
                    }
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = DesignTokens.BrandColors.Ledger
                        )
                    )
                }
                
                Text(
                    text = transaction.categoryDetails?.icon ?: "📝",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Text(
                    text = transaction.categoryDetails?.name ?: stringResource(R.string.uncategorized),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = if (transaction.categoryDetails?.type == "INCOME") {
                    "+¥${transaction.amountYuan}"
                } else {
                    "-¥${transaction.amountYuan}"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = amountColor
            )
        }
        
        // 上下文菜单
        if (showMenu) {
            TransactionContextMenu(
                onEdit = {
                    onEdit()
                    showMenu = false
                },
                onDelete = {
                    onDelete()
                    showMenu = false
                },
                onCopy = {
                    onCopy()
                    showMenu = false
                },
                onDismiss = { showMenu = false }
            )
        }
    }
}

/**
 * 交易上下文菜单
 */
@Composable
private fun TransactionContextMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(DesignTokens.Spacing.medium)
            )
    ) {
        // 复制交易
        DropdownMenuItem(
            text = { 
                Text(
                    text = "复制",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            onClick = onCopy,
            leadingIcon = { 
                Icon(
                    Icons.Default.ContentCopy, 
                    contentDescription = null,
                    tint = DesignTokens.BrandColors.Ledger
                ) 
            },
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
        )
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        
        // 退款功能 (替换原编辑功能)
        DropdownMenuItem(
            text = { 
                Column {
                    Text(
                        text = "退款",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "待开发中",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            },
            onClick = {
                // 暂时不执行任何操作，等待功能开发
                onDismiss()
            },
            leadingIcon = { 
                Icon(
                    Icons.Default.AccountBalance, 
                    contentDescription = null,
                    tint = DesignTokens.BrandColors.Warning
                ) 
            },
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
        )
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            thickness = 1.dp
        )
        
        // 删除交易
        DropdownMenuItem(
            text = { 
                Text(
                    text = "删除",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            onClick = onDelete,
            leadingIcon = { 
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                ) 
            },
            modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
        )
    }
}