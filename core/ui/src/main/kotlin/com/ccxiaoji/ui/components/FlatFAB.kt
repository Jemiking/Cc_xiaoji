package com.ccxiaoji.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化悬浮操作按钮
 * 遵循极简扁平化设计（方案A）
 * 
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param containerColor 容器颜色
 * @param contentColor 内容颜色
 * @param content 内容（通常是图标）
 */
@Composable
fun FlatFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.large)
    ) {
        content()
    }
}

/**
 * 扁平化小型FAB
 * 用于次要操作
 */
@Composable
fun FlatSmallFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
    ) {
        content()
    }
}

/**
 * 扁平化扩展FAB
 * 带文字的FAB
 */
@Composable
fun FlatExtendedFAB(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        icon = icon,
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(DesignTokens.BorderRadius.large)
    )
}