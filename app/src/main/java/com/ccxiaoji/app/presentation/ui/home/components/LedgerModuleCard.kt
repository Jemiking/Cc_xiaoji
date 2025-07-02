package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun LedgerModuleCard(
    todayIncome: Double,
    todayExpense: Double,
    budgetUsagePercentage: Float,
    onCardClick: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 动画效果
    var animatedBudget by remember { mutableStateOf(0f) }
    LaunchedEffect(budgetUsagePercentage) {
        animate(
            initialValue = animatedBudget,
            targetValue = budgetUsagePercentage,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedBudget = value
        }
    }
    
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onCardClick,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f)
    ) {
        // 简洁头部
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "记账",
                    tint = DesignTokens.BrandColors.Ledger,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "记账",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 今日数据展示
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.small),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 收入
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Success.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "收入",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "+¥%.0f".format(todayIncome),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Success
                    )
                }
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // 支出
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "-¥%.0f".format(todayExpense),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 本月预算进度
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "本月预算",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${animatedBudget.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        animatedBudget > 100 -> MaterialTheme.colorScheme.error
                        animatedBudget > 80 -> DesignTokens.BrandColors.Warning
                        else -> DesignTokens.BrandColors.Success
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 渐进式进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((animatedBudget / 100f).coerceIn(0f, 1f))
                        .background(
                            brush = when {
                                animatedBudget > 100 -> DesignTokens.BrandGradients.Error
                                animatedBudget > 80 -> DesignTokens.BrandGradients.Warning
                                else -> DesignTokens.BrandGradients.Success
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 快速操作按钮
        FlatButton(
            onClick = onQuickAdd,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DesignTokens.BrandColors.Ledger,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            Text(
                text = "记一笔",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}