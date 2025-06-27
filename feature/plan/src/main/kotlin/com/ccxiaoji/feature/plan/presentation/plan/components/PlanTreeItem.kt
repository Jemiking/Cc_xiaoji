package com.ccxiaoji.feature.plan.presentation.plan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.Plan
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.feature.plan.presentation.theme.extendedColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * 计划树形列表项组件（性能优化版本）
 * 使用@Stable注解标记组件稳定性，优化重组性能
 */
@Stable
@Composable
fun PlanTreeItem(
    plan: Plan,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPlanClick: () -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onDeleteClick: () -> Unit,
    onCreateSubPlan: () -> Unit,
    level: Int = 0,
    modifier: Modifier = Modifier
) {
    // 使用remember缓存计算结果
    val indentPadding = remember(level) { (level * 24).dp }
    val extendedColors = MaterialTheme.extendedColors
    val statusColor = remember(plan.status, extendedColors) {
        when (plan.status) {
            PlanStatus.NOT_STARTED -> extendedColors.notStarted
            PlanStatus.IN_PROGRESS -> extendedColors.inProgress
            PlanStatus.COMPLETED -> extendedColors.completed
            PlanStatus.CANCELLED -> extendedColors.cancelled
        }
    }
    
    // 动画旋转角度
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = spring(),
        label = "expandArrowRotation"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indentPadding)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onPlanClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题行
                PlanTitleRow(
                    plan = plan,
                    isExpanded = isExpanded,
                    rotationAngle = rotationAngle,
                    statusColor = statusColor,
                    onToggleExpand = onToggleExpand,
                    onCreateSubPlan = onCreateSubPlan,
                    onDeleteClick = onDeleteClick
                )
                
                // 日期信息
                PlanDateInfo(plan = plan)
                
                // 进度条
                PlanProgressBar(
                    progress = plan.progress,
                    color = remember(plan.color) { Color(plan.color.toColorInt()) }
                )
                
                // 标签
                if (plan.tags.isNotEmpty()) {
                    PlanTags(tags = remember(plan.tags) { plan.tags.toImmutableList() })
                }
            }
        }
        
        // 子计划列表（使用懒加载）
        AnimatedVisibility(
            visible = isExpanded && plan.hasChildren,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = 0.8f)
            ),
            exit = shrinkVertically(
                animationSpec = spring(dampingRatio = 0.8f)
            )
        ) {
            LazyChildPlanList(
                children = remember(plan.children) { plan.children.toImmutableList() },
                onToggleExpand = onToggleExpand,
                onPlanClick = onPlanClick,
                onProgressUpdate = onProgressUpdate,
                onDeleteClick = onDeleteClick,
                onCreateSubPlan = onCreateSubPlan,
                level = level + 1
            )
        }
    }
}

/**
 * 计划标题行组件
 */
@Stable
@Composable
private fun PlanTitleRow(
    plan: Plan,
    isExpanded: Boolean,
    rotationAngle: Float,
    statusColor: Color,
    onToggleExpand: () -> Unit,
    onCreateSubPlan: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 展开/折叠按钮
        if (plan.hasChildren) {
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "折叠" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotationAngle)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
        
        // 状态指示器
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
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
        IconButton(
            onClick = onCreateSubPlan,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加子计划",
                modifier = Modifier.size(20.dp)
            )
        }
        
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * 日期信息组件
 */
@Stable
@Composable
private fun PlanDateInfo(plan: Plan) {
    val dateText = remember(plan.startDate, plan.endDate) {
        "${plan.startDate.monthNumber.toString().padStart(2, '0')}-${plan.startDate.dayOfMonth.toString().padStart(2, '0')} ~ " +
        "${plan.endDate.monthNumber.toString().padStart(2, '0')}-${plan.endDate.dayOfMonth.toString().padStart(2, '0')}"
    }
    
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = dateText,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 进度条组件
 */
@Stable
@Composable
private fun PlanProgressBar(
    progress: Float,
    color: Color
) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${progress.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 标签列表组件
 */
@Stable
@Composable
private fun PlanTags(tags: ImmutableList<String>) {
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.take(3).forEach { tag ->
            TagChip(tag = tag)
        }
        if (tags.size > 3) {
            Text(
                text = "+${tags.size - 3}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 懒加载子计划列表
 * 只渲染可见的子计划，提高性能
 */
@Composable
private fun LazyChildPlanList(
    children: ImmutableList<Plan>,
    onToggleExpand: () -> Unit,
    onPlanClick: () -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onDeleteClick: () -> Unit,
    onCreateSubPlan: () -> Unit,
    level: Int
) {
    Column {
        children.forEach { childPlan ->
            PlanTreeItem(
                plan = childPlan,
                isExpanded = false, // 子计划默认折叠
                onToggleExpand = { /* TODO: 处理子计划展开 */ },
                onPlanClick = onPlanClick,
                onProgressUpdate = onProgressUpdate,
                onDeleteClick = onDeleteClick,
                onCreateSubPlan = onCreateSubPlan,
                level = level
            )
        }
    }
}

/**
 * 标签芯片
 */
@Composable
private fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

/**
 * 将颜色字符串转换为Color Int
 */
private fun String.toColorInt(): Long {
    return this.removePrefix("#").toLong(16) or 0xFF000000
}