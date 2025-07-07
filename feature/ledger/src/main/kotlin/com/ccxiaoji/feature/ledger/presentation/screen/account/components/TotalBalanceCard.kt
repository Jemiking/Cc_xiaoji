package com.ccxiaoji.feature.ledger.presentation.screen.account.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.R
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun TotalBalanceCard(
    totalBalance: Double,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        onClick = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.account_total_assets),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            Text(
                text = stringResource(
                    R.string.amount_format, 
                    stringResource(R.string.currency_symbol), 
                    totalBalance
                ),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (totalBalance >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    DesignTokens.BrandColors.Error
                }
            )
        }
    }
}