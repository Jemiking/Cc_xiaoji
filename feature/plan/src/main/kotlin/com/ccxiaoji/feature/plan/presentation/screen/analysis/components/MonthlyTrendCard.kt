package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.MonthlyStats
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 月度趋势卡片 - 扁平化设计
 */
@Composable
fun MonthlyTrendCard(
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
                text = "月度趋势",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (statistics.monthlyStats.isEmpty()) {
                Box(
                    modifier = Modifier.padding(vertical = DesignTokens.Spacing.large)
                ) {
                    Text(
                        text = "暂无月度数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    statistics.monthlyStats.forEach { monthly ->
                        MonthlyStatRow(monthly)
                    }
                }
            }
        }
    }
}

/**
 * 月度统计行
 */
@Composable
private fun MonthlyStatRow(monthly: MonthlyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = monthly.yearMonth,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Text(
                        text = "创建",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = monthly.created.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Text(
                        text = "完成",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = monthly.completed.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = DesignTokens.BrandColors.Success
                    )
                }
            }
        }
    }
}