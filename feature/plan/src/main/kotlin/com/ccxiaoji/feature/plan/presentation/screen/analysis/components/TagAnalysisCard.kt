package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.feature.plan.domain.model.TagStats
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 标签分析卡片 - 扁平化设计
 */
@Composable
fun TagAnalysisCard(
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
                text = "标签分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            if (statistics.tagStats.isEmpty()) {
                Box(
                    modifier = Modifier.padding(vertical = DesignTokens.Spacing.large)
                ) {
                    Text(
                        text = "暂无标签数据",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    statistics.tagStats.take(10).forEach { tagStat ->
                        TagStatRow(tagStat)
                    }
                }
            }
        }
    }
}

/**
 * 标签统计行
 */
@Composable
private fun TagStatRow(tagStat: TagStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlatChip(
            label = tagStat.tag,
            onClick = { },
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            contentColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${tagStat.count}个计划",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                Text(
                    text = "平均进度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "${tagStat.avgProgress.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = getProgressColor(tagStat.avgProgress)
                )
            }
        }
    }
}

/**
 * 根据进度获取颜色
 */
@Composable
private fun getProgressColor(progress: Float) = when {
    progress >= 80f -> DesignTokens.BrandColors.Success
    progress >= 50f -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}