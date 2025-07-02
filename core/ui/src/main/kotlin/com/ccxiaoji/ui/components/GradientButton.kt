package com.ccxiaoji.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 渐变按钮组件
 * 提供现代化的渐变背景按钮
 */
@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = DesignTokens.BrandGradients.Primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    elevation: ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = DesignTokens.Elevation.small,
        pressedElevation = DesignTokens.Elevation.none,
        disabledElevation = DesignTokens.Elevation.none
    ),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
    
    Button(
        onClick = onClick,
        modifier = modifier
            .clip(shape)
            .background(
                brush = if (enabled) gradient else Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.colorScheme.outline
                    )
                ),
                shape = shape
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = elevation,
        shape = shape
    ) {
        content()
    }
}