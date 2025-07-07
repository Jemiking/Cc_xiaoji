package com.ccxiaoji.feature.ledger.presentation.screen.savings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 扁平化储蓄目标卡片
 */
@Composable
fun SavingsGoalItem(
    goal: SavingsGoal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 图标
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getGoalColor(goal.color).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIconForGoal(goal.iconName),
                            contentDescription = null,
                            tint = getGoalColor(goal.color),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        goal.targetDate?.let { date ->
                            Text(
                                text = "目标日期: ${date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // 完成标记
                if (goal.isCompleted) {
                    FlatChip(
                        label = "已完成",
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 进度条
            LinearProgressIndicator(
                progress = goal.progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = getGoalColor(goal.color),
                trackColor = getGoalColor(goal.color).copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 金额详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AmountItem(
                    label = "已存",
                    amount = formatCurrency(goal.currentAmount)
                )
                
                Text(
                    text = "${goal.progressPercentage}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = getGoalColor(goal.color)
                )
                
                AmountItem(
                    label = "目标",
                    amount = formatCurrency(goal.targetAmount),
                    alignment = Alignment.End
                )
            }
            
            // 剩余天数
            goal.daysRemaining?.let { days ->
                if (days > 0 && !goal.isCompleted) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                    FlatChip(
                        label = "剩余 $days 天",
                        onClick = { },
                        containerColor = when {
                            days <= 7 -> MaterialTheme.colorScheme.errorContainer
                            days <= 30 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = when {
                            days <= 7 -> MaterialTheme.colorScheme.onErrorContainer
                            days <= 30 -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 金额项
 */
@Composable
private fun AmountItem(
    label: String,
    amount: String,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 获取目标图标
 */
private fun getIconForGoal(iconName: String): ImageVector {
    return when (iconName) {
        "house", "home" -> Icons.Default.Home
        "car" -> Icons.Default.DirectionsCar
        "vacation" -> Icons.Default.BeachAccess
        "education" -> Icons.Default.School
        "emergency", "medical" -> Icons.Default.LocalHospital
        "shopping" -> Icons.Default.ShoppingCart
        "gift" -> Icons.Default.CardGiftcard
        "phone" -> Icons.Default.PhoneAndroid
        "computer" -> Icons.Default.Computer
        "camera" -> Icons.Default.CameraAlt
        "travel", "flight" -> Icons.Default.Flight
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Savings
    }
}

/**
 * 获取目标颜色
 */
@Composable
private fun getGoalColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
}

/**
 * 格式化货币
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return format.format(amount)
}