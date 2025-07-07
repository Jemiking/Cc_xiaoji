package com.ccxiaoji.feature.plan.presentation.screen.detail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.feature.plan.domain.model.PlanStatus
import com.ccxiaoji.ui.components.FlatChip

/**
 * 状态芯片 - 扁平化设计
 */
@Composable
fun StatusChip(
    status: PlanStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (text, containerColor, contentColor) = when (status) {
        PlanStatus.NOT_STARTED -> Triple(
            "未开始",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
        PlanStatus.IN_PROGRESS -> Triple(
            "进行中",
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary
        )
        PlanStatus.COMPLETED -> Triple(
            "已完成",
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary
        )
        PlanStatus.CANCELLED -> Triple(
            "已取消",
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.error
        )
    }
    
    FlatChip(
        label = text,
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = contentColor.copy(alpha = 0.2f),
        modifier = modifier
    )
}