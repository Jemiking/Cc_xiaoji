package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard

@Composable
fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Ledger
                )
                action?.invoke()
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            content()
        }
    }
}