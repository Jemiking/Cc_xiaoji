package com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.Account
import com.ccxiaoji.common.utils.CreditCardDateUtils
import com.ccxiaoji.feature.ledger.presentation.utils.CurrencyFormatter

/**
 * 信用卡项
 */
@Composable
fun CreditCardItem(
    card: Account,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    
                    // 可用额度
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "可用额度：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(card.availableCreditYuan ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // 已用额度
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "已用额度：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(-card.balanceYuan),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (card.balanceYuan < 0) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // 使用率指示器
                card.utilizationRate?.let { rate ->
                    CreditUtilizationIndicator(rate = rate)
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 账单日和还款日
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = "账单日：${card.billingDay}号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                    Text(
                        text = "还款日：${card.paymentDueDay}号",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 还款状态提示
                if (card.balanceYuan < 0 && card.paymentDueDay != null && card.billingDay != null) {
                    val daysUntilDue = CreditCardDateUtils.calculateDaysUntilPayment(
                        paymentDueDay = card.paymentDueDay,
                        billingDay = card.billingDay
                    )
                    
                    if (daysUntilDue <= 3) {
                        Text(
                            text = when (daysUntilDue) {
                                0 -> "今天还款"
                                1 -> "明天还款"
                                else -> "${daysUntilDue}天内还款"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}