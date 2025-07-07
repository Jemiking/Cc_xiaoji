package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.feature.plan.domain.model.ProgressRange
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 进度分布卡片 - 扁平化设计
 */
@Composable
fun ProgressDistributionCard(
    statistics: PlanStatistics,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            Text(
                text = "进度分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            statistics.progressDistribution.forEach { range ->
                ProgressRangeRow(range)
            }
        }
    }
}

/**
 * 进度区间行
 */
@Composable
private fun ProgressRangeRow(range: ProgressRange) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = range.range,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${range.count}个 (${range.percentage.toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        LinearProgressIndicator(
            progress = { range.percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = getProgressColor(range.range),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * 根据进度区间获取颜色
 */
@Composable
private fun getProgressColor(range: String) = when {
    range.contains("100") -> DesignTokens.BrandColors.Success
    range.contains("75-99") -> MaterialTheme.colorScheme.primary
    range.contains("50-74") -> DesignTokens.BrandColors.Warning
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}