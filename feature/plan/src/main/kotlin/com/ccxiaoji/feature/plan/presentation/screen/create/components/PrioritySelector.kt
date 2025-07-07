package com.ccxiaoji.feature.plan.presentation.screen.create.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatChip
import com.ccxiaoji.ui.components.SectionHeader
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化优先级选择器
 */
@Composable
fun PrioritySelector(
    priority: Int,
    onPrioritySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "优先级",
            modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            PriorityItem(
                label = "低",
                priority = 0,
                isSelected = priority == 0,
                onClick = { onPrioritySelected(0) }
            )
            
            PriorityItem(
                label = "较低",
                priority = 1,
                isSelected = priority == 1,
                onClick = { onPrioritySelected(1) }
            )
            
            PriorityItem(
                label = "中",
                priority = 2,
                isSelected = priority == 2,
                onClick = { onPrioritySelected(2) }
            )
            
            PriorityItem(
                label = "较高",
                priority = 3,
                isSelected = priority == 3,
                onClick = { onPrioritySelected(3) }
            )
            
            PriorityItem(
                label = "高",
                priority = 4,
                isSelected = priority == 4,
                onClick = { onPrioritySelected(4) }
            )
        }
    }
}

/**
 * 优先级选项
 */
@Composable
private fun PriorityItem(
    label: String,
    priority: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> getPriorityColor(priority).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        isSelected -> getPriorityColor(priority)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    FlatChip(
        label = label,
        onClick = onClick,
        selected = isSelected,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = if (isSelected) {
            getPriorityColor(priority).copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
    )
}

/**
 * 获取优先级对应的颜色
 */
@Composable
private fun getPriorityColor(priority: Int) = when (priority) {
    0 -> MaterialTheme.colorScheme.onSurfaceVariant // 低
    1 -> MaterialTheme.colorScheme.tertiary // 较低
    2 -> MaterialTheme.colorScheme.primary // 中
    3 -> MaterialTheme.colorScheme.secondary // 较高
    4 -> MaterialTheme.colorScheme.error // 高
    else -> MaterialTheme.colorScheme.primary
}