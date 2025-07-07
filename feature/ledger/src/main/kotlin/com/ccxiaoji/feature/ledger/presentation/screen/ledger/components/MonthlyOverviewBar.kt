package com.ccxiaoji.feature.ledger.presentation.screen.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun MonthlyOverviewBar(
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    val balance = monthlyIncome - monthlyExpense
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DesignTokens.BrandColors.Ledger.copy(alpha = 0.05f),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
            )
            .border(
                width = 1.dp,
                color = DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
            )
            .padding(
                horizontal = DesignTokens.Spacing.medium, 
                vertical = DesignTokens.Spacing.small
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 收入
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.income),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(
                        R.string.amount_format_positive, 
                        stringResource(R.string.currency_symbol),
                        monthlyIncome
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Success
                )
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
            )
            
            // 支出
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.expense),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = stringResource(
                        R.string.amount_format_negative, 
                        stringResource(R.string.currency_symbol),
                        monthlyExpense
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Error
                )
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
            )
            
            // 结余
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (balance >= 0) {
                        DesignTokens.BrandColors.Ledger
                    } else {
                        DesignTokens.BrandColors.Error
                    }
                )
            }
        }
    }
}