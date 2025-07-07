package com.ccxiaoji.feature.ledger.presentation.screen.creditcard.components

import androidx.compose.foundation.layout.*
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

/**
 * 还款进度卡片
 */
@Composable
fun PaymentProgressCard(bill: CreditCardBill) {
    val paidAmountYuan = bill.paidAmountCents / 100.0
    val progress = if (bill.totalAmountYuan > 0) {
        (paidAmountYuan / bill.totalAmountYuan).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            Text(
                "还款进度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 进度条
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (progress >= 1f) {
                    MaterialTheme.colorScheme.primary
                } else if (bill.isOverdue) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                }
            )
            
            // 进度文字
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (progress >= 1f) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "已还 ${formatCurrency(paidAmountYuan)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "剩余 ${formatCurrency(bill.remainingAmountYuan)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (bill.isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // 最低还款提示
            if (bill.minimumPaymentYuan > 0 && paidAmountYuan < bill.minimumPaymentYuan) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(DesignTokens.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "最低还款额 ${formatCurrency(bill.minimumPaymentYuan)} 尚未达到",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}