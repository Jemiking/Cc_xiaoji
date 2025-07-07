package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard

@Composable
fun AddCategoryBudgetCard(
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        borderColor = DesignTokens.BrandColors.Ledger.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = DesignTokens.BrandColors.Ledger
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            Text(
                text = "添加分类预算",
                style = MaterialTheme.typography.bodyLarge,
                color = DesignTokens.BrandColors.Ledger
            )
        }
    }
}