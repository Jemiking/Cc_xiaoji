package com.ccxiaoji.feature.schedule.presentation.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 班次分布图表 - 扁平化设计
 */
@Composable
fun ShiftDistributionChart(
    distribution: Map<String, Int>,
    shifts: List<Shift>,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_shift_distribution),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            val total = distribution.values.sum()
            
            distribution.forEach { (shiftName, count) ->
                val shift = shifts.find { it.name == shiftName }
                val percentage = if (total > 0) (count * 100f / total) else 0f
                
                ShiftDistributionBar(
                    shiftName = shiftName,
                    count = count,
                    percentage = percentage,
                    color = shift?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}