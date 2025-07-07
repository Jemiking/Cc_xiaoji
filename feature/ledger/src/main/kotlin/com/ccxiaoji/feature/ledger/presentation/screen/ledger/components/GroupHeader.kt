package com.ccxiaoji.feature.ledger.presentation.screen.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun GroupHeader(
    title: String,
    subtitle: String? = null,
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
            )
            .padding(
                horizontal = DesignTokens.Spacing.medium, 
                vertical = DesignTokens.Spacing.small
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 收入
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.income),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(
                            R.string.amount_format_positive, 
                            stringResource(R.string.currency_symbol), 
                            totalIncome
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DesignTokens.BrandColors.Success
                    )
                }
                
                // 支出
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.expense),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = stringResource(
                            R.string.amount_format_negative, 
                            stringResource(R.string.currency_symbol), 
                            totalExpense
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DesignTokens.BrandColors.Error
                    )
                }
                
                // 结余
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.balance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (balance >= 0) {
                            stringResource(
                                R.string.amount_format_positive, 
                                stringResource(R.string.currency_symbol), 
                                balance
                            )
                        } else {
                            stringResource(
                                R.string.amount_format_negative, 
                                stringResource(R.string.currency_symbol), 
                                -balance
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (balance >= 0) {
                            DesignTokens.BrandColors.Success
                        } else {
                            DesignTokens.BrandColors.Error
                        }
                    )
                }
            }
        }
    }
}