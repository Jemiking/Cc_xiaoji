package com.ccxiaoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 毛玻璃效果卡片
 * 提供现代化的半透明卡片效果
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    backgroundBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(DesignTokens.BorderRadius.large)
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = backgroundBrush ?: Brush.verticalGradient(
                    colors = listOf(
                        containerColor,
                        containerColor.copy(alpha = 0.6f)
                    )
                ),
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .padding(DesignTokens.Spacing.medium)
    ) {
        content()
    }
}
