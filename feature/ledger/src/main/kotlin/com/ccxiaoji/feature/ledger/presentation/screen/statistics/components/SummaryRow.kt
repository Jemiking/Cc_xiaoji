package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun SummaryRow(
    totalIncome: Int,
    totalExpense: Int,
    balance: Int,
    savingsRate: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            SummaryCard(
                title = "总收入",
                amount = totalIncome,
                color = DesignTokens.BrandColors.Success,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "总支出",
                amount = totalExpense,
                color = DesignTokens.BrandColors.Error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            SummaryCard(
                title = "结余",
                amount = balance,
                color = if (balance >= 0) DesignTokens.BrandColors.Info else DesignTokens.BrandColors.Error,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "储蓄率",
                percentage = savingsRate,
                color = DesignTokens.BrandColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}