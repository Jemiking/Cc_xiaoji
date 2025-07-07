package com.ccxiaoji.feature.todo.presentation.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 任务分组标题组件
 * 用于显示时间分组（今天、明天、本周等）
 */
@Composable
fun TaskGroupHeader(
    title: String,
    taskCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = DesignTokens.Spacing.medium, 
                vertical = DesignTokens.Spacing.small
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = DesignTokens.BrandColors.Todo,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$taskCount 项",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}