package com.ccxiaoji.feature.plan.presentation.screen.edit.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.SectionHeader
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化状态选择器
 */
@Composable
fun StatusSelector(
    status: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "计划状态",
            modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            StatusItem(
                label = "未开始",
                statusCode = "NOT_STARTED",
                isSelected = status == "NOT_STARTED",
                onClick = { onStatusSelected("NOT_STARTED") }
            )
            
            StatusItem(
                label = "进行中",
                statusCode = "IN_PROGRESS",
                isSelected = status == "IN_PROGRESS",
                onClick = { onStatusSelected("IN_PROGRESS") }
            )
            
            StatusItem(
                label = "已完成",
                statusCode = "COMPLETED",
                isSelected = status == "COMPLETED",
                onClick = { onStatusSelected("COMPLETED") }
            )
            
            StatusItem(
                label = "已取消",
                statusCode = "CANCELLED",
                isSelected = status == "CANCELLED",
                onClick = { onStatusSelected("CANCELLED") }
            )
        }
    }
}

/**
 * 状态选项
 */
@Composable
private fun StatusItem(
    label: String,
    statusCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> getStatusColor(statusCode).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isSelected -> getStatusColor(statusCode)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    FlatChip(
        label = label,
        onClick = onClick,
        selected = isSelected,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = if (isSelected) {
            getStatusColor(statusCode).copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
    )
}

/**
 * 获取状态对应的颜色
 */
@Composable
private fun getStatusColor(status: String) = when (status) {
    "NOT_STARTED" -> MaterialTheme.colorScheme.onSurfaceVariant
    "IN_PROGRESS" -> MaterialTheme.colorScheme.primary
    "COMPLETED" -> MaterialTheme.colorScheme.primary
    "CANCELLED" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.primary
}