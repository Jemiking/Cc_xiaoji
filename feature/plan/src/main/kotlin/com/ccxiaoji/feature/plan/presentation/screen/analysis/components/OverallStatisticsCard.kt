package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 总体统计卡片 - 扁平化设计
 */
@Composable
fun OverallStatisticsCard(
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
                text = "总体统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "总计划数",
                    value = statistics.totalPlans.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                StatisticItem(
                    label = "完成率",
                    value = "${statistics.completionRate.toInt()}%",
                    color = DesignTokens.BrandColors.Success
                )
                StatisticItem(
                    label = "平均进度",
                    value = "${statistics.averageProgress.toInt()}%",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}