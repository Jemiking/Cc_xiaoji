package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.ModernCard
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummaryCard(
    title: String,
    amount: Int? = null,
    percentage: Float? = null,
    color: Color,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
    
    ModernCard(
        modifier = modifier,
        backgroundColor = color.copy(alpha = 0.05f),
        borderColor = color.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier
                .padding(DesignTokens.Spacing.medium)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            when {
                amount != null -> {
                    Text(
                        text = currencyFormat.format(amount / 100.0),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
                percentage != null -> {
                    Text(
                        text = "%.1f%%".format(percentage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}