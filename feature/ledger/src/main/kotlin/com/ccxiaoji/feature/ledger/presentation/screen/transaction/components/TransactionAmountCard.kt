package com.ccxiaoji.feature.ledger.presentation.screen.transaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard

@Composable
fun TransactionAmountCard(
    categoryIcon: String,
    amount: Double,
    categoryName: String,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (isIncome) {
            DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
        } else {
            DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
        },
        // shape removed - ModernCard doesn't support this parameter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 分类图标背景
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = if (isIncome) {
                            DesignTokens.BrandColors.Success.copy(alpha = 0.1f)
                        } else {
                            DesignTokens.BrandColors.Error.copy(alpha = 0.1f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryIcon,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            // 金额
            Text(
                text = buildString {
                    append(if (isIncome) "+¥" else "-¥")
                    append("%.2f".format(amount))
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) {
                    DesignTokens.BrandColors.Success
                } else {
                    DesignTokens.BrandColors.Error
                }
            )
            
            // 分类名称
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}