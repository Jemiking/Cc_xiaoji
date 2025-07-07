package com.ccxiaoji.feature.plan.presentation.screen.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 子计划项 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubPlanItem(
    plan: Plan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(getStatusColor(plan.status))
            )
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 标题和进度
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                
                // 进度条
                LinearProgressIndicator(
                    progress = { plan.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = getPlanProgressColor(plan.progress),
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
            
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
            
            // 进度百分比
            Text(
                text = "${plan.progress.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            // 导航箭头
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 获取状态对应的颜色
 */
@Composable
private fun getStatusColor(status: PlanStatus) = when (status) {
    PlanStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant
    PlanStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
    PlanStatus.COMPLETED -> DesignTokens.BrandColors.Success
    PlanStatus.CANCELLED -> MaterialTheme.colorScheme.error
}

/**
 * 获取进度对应的颜色
 */
@Composable
private fun getPlanProgressColor(progress: Float) = when {
    progress >= 100f -> DesignTokens.BrandColors.Success
    progress >= 50f -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
}