package com.ccxiaoji.feature.ledger.presentation.screen.account.components

import android.util.Log
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
    val TAG = "AccountItem"
    var showMenu by remember { mutableStateOf(false) }
    
    // 调试账户信息
    LaunchedEffect(account) {
        Log.d(TAG, "渲染账户项目: ${account.name}, ID: ${account.id}, 类型: ${account.type}")
    }
    
    // 调试菜单状态
    LaunchedEffect(showMenu) {
        Log.d(TAG, "账户${account.name}菜单状态变化: $showMenu")
    }
    
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
                    onClick = {
                        Log.d(TAG, "点击账户: ${account.name}")
                        onClick()
                    },
                    onLongClick = { 
                        Log.d(TAG, "长按账户: ${account.name}，显示菜单")
                        showMenu = true 
                    }
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
                        // 显示账户类型和信用卡专门信息
                        if (account.type == AccountType.CREDIT_CARD) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = account.type.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                
                                // 显示还款日和账单日
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                                ) {
                                    account.billingDay?.let { billingDay ->
                                        Text(
                                            text = "账单日: ${billingDay}日",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }
                                    
                                    account.paymentDueDay?.let { dueDay ->
                                        Text(
                                            text = "还款日: ${dueDay}日",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DesignTokens.BrandColors.Warning.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = account.type.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    // 信用卡显示专门信息，其他账户显示余额
                    if (account.type == AccountType.CREDIT_CARD) {
                        // 信用卡负债显示
                        Text(
                            text = stringResource(
                                R.string.amount_format, 
                                stringResource(R.string.currency_symbol), 
                                -account.balanceYuan // 信用卡负债显示为正数
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (account.balanceYuan <= 0) {
                                DesignTokens.BrandColors.Error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        
                        // 可用额度显示
                        account.availableCreditYuan?.let { availableCredit ->
                            Text(
                                text = "可用: ¥${String.format("%.2f", availableCredit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 使用率显示
                        account.utilizationRate?.let { utilization ->
                            Text(
                                text = "使用率: ${String.format("%.1f", utilization)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    utilization <= 30 -> DesignTokens.BrandColors.Success
                                    utilization <= 80 -> DesignTokens.BrandColors.Warning
                                    else -> DesignTokens.BrandColors.Error
                                }
                            )
                        }
                    } else {
                        // 普通账户显示余额
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
                    
                    // 添加明显的编辑按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                    ) {
                        // 编辑按钮
                        IconButton(
                            onClick = {
                                Log.d(TAG, "点击编辑按钮，账户: ${account.name}")
                                onEdit()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "编辑账户",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // 更多操作按钮（保留原有的长按菜单功能）
                        IconButton(
                            onClick = { 
                                Log.d(TAG, "点击更多按钮，账户: ${account.name}")
                                showMenu = true 
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "更多操作",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Dropdown Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // 信用卡专有菜单项
            if (account.type == AccountType.CREDIT_CARD) {
                DropdownMenuItem(
                    text = { Text("查看账单") },
                    onClick = {
                        Log.d(TAG, "点击查看账单菜单项，信用卡: ${account.name}")
                        // TODO: 在阶段二功能迁移时实现
                        onClick() // 暂时跳转到交易列表
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Receipt, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
                
                DropdownMenuItem(
                    text = { Text("记录还款") },
                    onClick = {
                        Log.d(TAG, "点击记录还款菜单项，信用卡: ${account.name}")
                        // TODO: 在阶段二功能迁移时实现
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Payment, 
                            contentDescription = null,
                            tint = DesignTokens.BrandColors.Success
                        )
                    }
                )
                
                if (showMenu) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = DesignTokens.Spacing.xs),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
            
            // 通用菜单项
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
                    Log.d(TAG, "点击编辑菜单项，账户: ${account.name}, ID: ${account.id}")
                    try {
                        onEdit()
                        Log.d(TAG, "成功调用onEdit回调")
                    } catch (e: Exception) {
                        Log.e(TAG, "调用onEdit回调时异常", e)
                    }
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