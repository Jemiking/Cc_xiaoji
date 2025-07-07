package com.ccxiaoji.feature.ledger.presentation.screen.ledger.components

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
import com.ccxiaoji.feature.ledger.domain.model.Transaction
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
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
    
    // 使用语义化颜色
    val amountColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    // 使用预定义的分类颜色
    val categoryColor = when (transaction.categoryDetails?.type) {
        "INCOME" -> DesignTokens.BrandColors.Success
        "EXPENSE" -> DesignTokens.BrandColors.Error
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(modifier = modifier) {
        ModernCard(
            onClick = onItemClick,
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
            backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null, // Handled by card onClick
                            colors = CheckboxDefaults.colors(
                                checkedColor = DesignTokens.BrandColors.Todo
                            )
                        )
                    }
                    
                    // 分类图标 - 扁平背景
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = categoryColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transaction.categoryDetails?.icon ?: "📝",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    
                    Column {
                        Text(
                            text = transaction.categoryDetails?.name ?: stringResource(R.string.uncategorized),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        transaction.note?.let { note ->
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = transaction.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                                .toJavaLocalDateTime()
                                .format(DateTimeFormatter.ofPattern("MM-dd HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Text(
                    text = if (transaction.categoryDetails?.type == "INCOME") {
                        stringResource(R.string.amount_format_positive, stringResource(R.string.currency_symbol), transaction.amountYuan)
                    } else {
                        stringResource(R.string.amount_format_negative, stringResource(R.string.currency_symbol), transaction.amountYuan)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = amountColor
                )
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.copy)) },
                onClick = {
                    onCopy()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.FileCopy, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    onEdit()
                    showMenu = false
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
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
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}