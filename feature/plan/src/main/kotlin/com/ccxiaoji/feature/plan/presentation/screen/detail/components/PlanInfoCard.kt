package com.ccxiaoji.feature.plan.presentation.screen.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * 计划基本信息卡片 - 扁平化设计
 */
@Composable
fun PlanInfoCard(
    plan: Plan,
    onStatusChange: (PlanStatus) -> Unit,
    onProgressChange: (Float) -> Unit,
    onEditProgress: () -> Unit = {},
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
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (plan.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 状态指示器
                StatusChip(
                    status = plan.status,
                    onClick = { /* TODO: 显示状态选择菜单 */ }
                )
            }
            
            // 日期范围
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(DesignTokens.Spacing.xs))
                Text(
                    text = "${formatDate(plan.startDate)} - ${formatDate(plan.endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 进度条
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "进度",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${plan.progress.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        if (!plan.hasChildren) {
                            IconButton(
                                onClick = onEditProgress,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "编辑进度",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                LinearProgressIndicator(
                    progress = { plan.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getPlanColor(plan.priority),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                if (plan.hasChildren) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
                    Text(
                        text = "进度由子计划自动计算",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            
            // 标签
            if (plan.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    plan.tags.forEach { tag ->
                        FlatChip(
                            label = tag,
                            onClick = { },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 获取计划优先级对应的颜色
 */
@Composable
private fun getPlanColor(priority: Int) = when (priority) {
    1 -> MaterialTheme.colorScheme.error // 高优先级
    2 -> MaterialTheme.colorScheme.primary // 中优先级
    3 -> DesignTokens.BrandColors.Success // 低优先级
    else -> MaterialTheme.colorScheme.primary
}

/**
 * 格式化日期
 */
private fun formatDate(date: LocalDate): String {
    return date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}