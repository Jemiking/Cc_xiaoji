package com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.feature.ledger.domain.model.CreditCardBill
import com.ccxiaoji.feature.ledger.presentation.utils.CurrencyFormatter.formatCurrency
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 账单项
 */
@Composable
fun BillItem(
    bill: CreditCardBill,
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
            // 账单周期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatBillingPeriod(
                        bill.billStartDate.toEpochMilliseconds(), 
                        bill.billEndDate.toEpochMilliseconds()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // 账单状态
                BillStatusBadge(
                    isPaid = bill.isPaid,
                    isOverdue = bill.isOverdue
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 金额信息
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "账单金额",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatCurrency(bill.totalAmountYuan),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (!bill.isPaid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "待还金额",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatCurrency(bill.remainingAmountYuan),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "最低还款",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatCurrency(bill.minimumPaymentYuan),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 还款日
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = "还款日：${formatDate(bill.paymentDueDate.toEpochMilliseconds())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BillStatusBadge(
    isPaid: Boolean,
    isOverdue: Boolean
) {
    val (statusText, statusColor) = when {
        isPaid -> "已还清" to MaterialTheme.colorScheme.primary
        isOverdue -> "已逾期" to MaterialTheme.colorScheme.error
        else -> "待还款" to MaterialTheme.colorScheme.tertiary
    }
    
    Surface(
        shape = MaterialTheme.shapes.small,
        color = statusColor.copy(alpha = 0.1f)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(
                horizontal = DesignTokens.Spacing.small,
                vertical = DesignTokens.Spacing.xs
            ),
            style = MaterialTheme.typography.labelMedium,
            color = statusColor
        )
    }
}

private fun formatBillingPeriod(startDate: Long, endDate: Long): String {
    val start = Instant.fromEpochMilliseconds(startDate).toLocalDateTime(TimeZone.currentSystemDefault())
    val end = Instant.fromEpochMilliseconds(endDate).toLocalDateTime(TimeZone.currentSystemDefault())
    
    return if (start.year == end.year && start.monthNumber == end.monthNumber) {
        "${start.year}年${start.monthNumber}月账单"
    } else {
        "${start.monthNumber}月${start.dayOfMonth}日-${end.monthNumber}月${end.dayOfMonth}日"
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${date.monthNumber}月${date.dayOfMonth}日"
}