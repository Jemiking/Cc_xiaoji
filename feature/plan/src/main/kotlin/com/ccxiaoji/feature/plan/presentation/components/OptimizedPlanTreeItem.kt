package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.collections.immutable.ImmutableList

/**
 * 优化的计划树形列表项 - 扁平化设计
 * 包含清晰的树形结构线条
 */
@Composable
fun OptimizedPlanTreeItem(
    plan: Plan,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPlanClick: () -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onDeleteClick: () -> Unit,
    onCreateSubPlan: () -> Unit,
    level: Int = 0,
    isLastChild: Boolean = false,
    modifier: Modifier = Modifier
) {
    val indentWidth = 24.dp
    val indentPadding = remember(level) { indentWidth * level }
    
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // 树形结构线条
        if (level > 0) {
            Box(
                modifier = Modifier
                    .width(indentPadding)
                    .fillMaxHeight()
            ) {
                TreeLines(
                    level = level,
                    isLastChild = isLastChild,
                    hasChildren = plan.hasChildren,
                    isExpanded = isExpanded
                )
            }
        }
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // 计划卡片
            ModernCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DesignTokens.Spacing.medium, vertical = DesignTokens.Spacing.xs)
                    .clickable { onPlanClick() },
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = when (plan.status) {
                    PlanStatus.NOT_STARTED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    PlanStatus.IN_PROGRESS -> DesignTokens.BrandColors.Plan.copy(alpha = 0.2f)
                    PlanStatus.COMPLETED -> DesignTokens.BrandColors.Success.copy(alpha = 0.2f)
                    PlanStatus.CANCELLED -> DesignTokens.BrandColors.Error.copy(alpha = 0.2f)
                }
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                ) {
                    // 标题行
                    PlanHeader(
                        plan = plan,
                        isExpanded = isExpanded,
                        onToggleExpand = if (plan.hasChildren) onToggleExpand else null,
                        onCreateSubPlan = onCreateSubPlan,
                        onDeleteClick = onDeleteClick
                    )
                    
                    // 日期和进度
                    PlanDetails(plan = plan)
                    
                    // 标签
                    if (plan.tags.isNotEmpty()) {
                        PlanTagsRow(tags = plan.tags)
                    }
                }
            }
            
            // 子计划
            AnimatedVisibility(
                visible = isExpanded && plan.hasChildren,
                enter = expandVertically(animationSpec = spring()),
                exit = shrinkVertically(animationSpec = spring())
            ) {
                Column {
                    plan.children.forEachIndexed { index, childPlan ->
                        OptimizedPlanTreeItem(
                            plan = childPlan,
                            isExpanded = false,
                            onToggleExpand = { /* TODO */ },
                            onPlanClick = onPlanClick,
                            onProgressUpdate = onProgressUpdate,
                            onDeleteClick = onDeleteClick,
                            onCreateSubPlan = onCreateSubPlan,
                            level = level + 1,
                            isLastChild = index == plan.children.size - 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * 树形结构线条绘制
 */
@Composable
private fun TreeLines(
    level: Int,
    isLastChild: Boolean,
    hasChildren: Boolean,
    isExpanded: Boolean
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val lineColor = Color.Gray.copy(alpha = 0.3f)
        val strokeWidth = 1.dp.toPx()
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        
        // 垂直线
        val verticalLineEndY = if (isLastChild) size.height / 2 else size.height
        drawLine(
            color = lineColor,
            start = Offset(size.width - 12.dp.toPx(), 0f),
            end = Offset(size.width - 12.dp.toPx(), verticalLineEndY),
            strokeWidth = strokeWidth,
            pathEffect = dashPathEffect
        )
        
        // 水平线
        drawLine(
            color = lineColor,
            start = Offset(size.width - 12.dp.toPx(), size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth,
            pathEffect = dashPathEffect
        )
    }
}

/**
 * 计划头部
 */
@Composable
private fun PlanHeader(
    plan: Plan,
    isExpanded: Boolean,
    onToggleExpand: (() -> Unit)?,
    onCreateSubPlan: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 展开/折叠按钮
        if (onToggleExpand != null) {
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 0f else -90f,
                animationSpec = spring(),
                label = "expand_arrow"
            )
            
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "折叠" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.rotate(rotation)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
        
        // 状态指示器
        val statusColor = when (plan.status) {
            PlanStatus.NOT_STARTED -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            PlanStatus.IN_PROGRESS -> DesignTokens.BrandColors.Plan
            PlanStatus.COMPLETED -> DesignTokens.BrandColors.Success
            PlanStatus.CANCELLED -> DesignTokens.BrandColors.Error
        }
        
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        // 标题
        Text(
            text = plan.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        
        // 操作按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
        ) {
            IconButton(
                onClick = onCreateSubPlan,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加子计划",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    modifier = Modifier.size(18.dp),
                    tint = DesignTokens.BrandColors.Error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 计划详情（日期和进度）
 */
@Composable
private fun PlanDetails(plan: Plan) {
    // 日期
    val dateText = remember(plan.startDate, plan.endDate) {
        "${plan.startDate.monthNumber.toString().padStart(2, '0')}.${plan.startDate.dayOfMonth.toString().padStart(2, '0')} - " +
        "${plan.endDate.monthNumber.toString().padStart(2, '0')}.${plan.endDate.dayOfMonth.toString().padStart(2, '0')}"
    }
    
    Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
    
    Text(
        text = dateText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
    
    // 进度条
    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = { plan.progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = DesignTokens.BrandColors.Plan,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
        
        Text(
            text = "${plan.progress.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 标签行
 */
@Composable
private fun PlanTagsRow(tags: List<String>) {
    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
    ) {
        tags.take(3).forEach { tag ->
            PlanTagChip(tag = tag)
        }
        
        if (tags.size > 3) {
            Text(
                text = "+${tags.size - 3}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = DesignTokens.Spacing.xs)
            )
        }
    }
}

/**
 * 标签芯片 - 扁平化设计
 */
@Composable
private fun PlanTagChip(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(DesignTokens.BorderRadius.small))
            .background(DesignTokens.BrandColors.Plan.copy(alpha = 0.1f))
            .padding(horizontal = DesignTokens.Spacing.small, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = DesignTokens.BrandColors.Plan
        )
    }
}