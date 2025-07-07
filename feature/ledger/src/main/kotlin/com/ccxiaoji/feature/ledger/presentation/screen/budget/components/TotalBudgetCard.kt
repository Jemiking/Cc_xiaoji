package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard
import java.text.NumberFormat

@Composable
fun TotalBudgetCard(
    budget: BudgetWithSpent,
    currencyFormat: NumberFormat,
    onClick: () -> Unit
) {
    val usagePercentage = if (budget.budgetAmountCents > 0) {
        (budget.spentAmountCents.toFloat() / budget.budgetAmountCents.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val isExceeded = budget.spentAmountCents > budget.budgetAmountCents
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = if (isExceeded) {
            DesignTokens.BrandColors.Error.copy(alpha = 0.05f)
        } else {
            DesignTokens.BrandColors.Success.copy(alpha = 0.05f)
        },
        borderColor = if (isExceeded) {
            DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
        } else {
            DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "总预算",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currencyFormat.format(budget.budgetAmountCents / 100.0),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isExceeded) {
                        DesignTokens.BrandColors.Error
                    } else {
                        DesignTokens.BrandColors.Success
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 进度条
            LinearProgressIndicator(
                progress = { usagePercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isExceeded) {
                    DesignTokens.BrandColors.Error
                } else if (usagePercentage >= budget.alertThreshold) {
                    DesignTokens.BrandColors.Warning
                } else {
                    DesignTokens.BrandColors.Success
                },
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已支出: ${currencyFormat.format(budget.spentAmountCents / 100.0)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = "${(usagePercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isExceeded) {
                        DesignTokens.BrandColors.Error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            if (budget.note?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                Text(
                    text = budget.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}