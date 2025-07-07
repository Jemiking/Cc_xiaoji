package com.ccxiaoji.feature.plan.presentation.screen.template.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ccxiaoji.ui.components.FlatChip

/**
 * 标签Chip组件 - 扁平化设计
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    FlatChip(
        label = tag,
        onClick = { },
        selected = false,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        modifier = modifier
    )
}