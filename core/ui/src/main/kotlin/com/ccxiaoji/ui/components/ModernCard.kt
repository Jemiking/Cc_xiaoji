package com.ccxiaoji.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 现代化卡片组件 - 极简扁平设计
 * 统一的卡片设计，支持语义化颜色和设计令牌
 */
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = backgroundColor ?: MaterialTheme.colorScheme.surface
    ),
    border: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = borderColor ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    ),
    elevation: CardElevation = CardDefaults.cardElevation(
        defaultElevation = DesignTokens.Elevation.small,
        pressedElevation = DesignTokens.Elevation.none
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
        elevation = elevation,
        colors = colors,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium),
            content = content
        )
    }
}