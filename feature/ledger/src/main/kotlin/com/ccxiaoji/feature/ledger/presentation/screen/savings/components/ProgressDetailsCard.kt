package com.ccxiaoji.feature.ledger.presentation.screen.savings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 进度详情卡片
 */
@Composable
fun ProgressDetailsCard(goal: SavingsGoal) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            // 金额详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProgressDetailItem(
                    label = "已储蓄",
                    value = formatCurrency(goal.currentAmount),
                    color = MaterialTheme.colorScheme.primary
                )
                
                ProgressDetailItem(
                    label = "目标金额",
                    value = formatCurrency(goal.targetAmount),
                    color = MaterialTheme.colorScheme.secondary
                )
                
                ProgressDetailItem(
                    label = "还需储蓄",
                    value = formatCurrency(goal.remainingAmount),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            
            // 目标日期信息
            goal.targetDate?.let { date ->
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "目标日期",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    goal.daysRemaining?.let { days ->
                        val (containerColor, contentColor) = when {
                            days <= 7 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            days <= 30 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                            color = containerColor
                        ) {
                            Text(
                                text = "剩余 $days 天",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(
                                    horizontal = DesignTokens.Spacing.medium,
                                    vertical = DesignTokens.Spacing.small
                                ),
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 进度详情项
 */
@Composable
private fun ProgressDetailItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 格式化货币
 */
private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.CHINA)
    return formatter.format(amount)
}