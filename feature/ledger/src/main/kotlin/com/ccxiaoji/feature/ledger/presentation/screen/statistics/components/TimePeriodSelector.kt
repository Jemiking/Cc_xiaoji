package com.ccxiaoji.feature.ledger.presentation.screen.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.ledger.presentation.viewmodel.TimePeriod
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatSelectChip

@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        TimePeriod.values().forEach { period ->
            FlatSelectChip(
                label = when (period) {
                    TimePeriod.THIS_MONTH -> "本月"
                    TimePeriod.THIS_YEAR -> "本年"
                    TimePeriod.CUSTOM -> "自定义"
                },
                selected = selectedPeriod == period,
                onSelectedChange = { if (it) onPeriodSelected(period) },
                modifier = Modifier.weight(1f),
                contentColor = if (selectedPeriod == period) {
                    DesignTokens.BrandColors.Ledger
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}