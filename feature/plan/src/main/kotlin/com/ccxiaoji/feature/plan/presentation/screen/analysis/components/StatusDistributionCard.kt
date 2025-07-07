package com.ccxiaoji.feature.plan.presentation.screen.analysis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.PlanStatistics
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 状态分布卡片 - 扁平化设计
 */
@Composable
fun StatusDistributionCard(
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
                text = "状态分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 状态分布列表
            StatusRow(
                status = "未开始",
                count = statistics.notStartedPlans,
                total = statistics.totalPlans,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusRow(
                status = "进行中",
                count = statistics.inProgressPlans,
                total = statistics.totalPlans,
                color = MaterialTheme.colorScheme.primary
            )
            StatusRow(
                status = "已完成",
                count = statistics.completedPlans,
                total = statistics.totalPlans,
                color = DesignTokens.BrandColors.Success
            )
            StatusRow(
                status = "已取消",
                count = statistics.cancelledPlans,
                total = statistics.totalPlans,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 状态行
 */
@Composable
private fun StatusRow(
    status: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count * 100 / total) else 0
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = color,
                            shape = MaterialTheme.shapes.small
                        )
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$count ($percentage%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        
        // 进度条
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}