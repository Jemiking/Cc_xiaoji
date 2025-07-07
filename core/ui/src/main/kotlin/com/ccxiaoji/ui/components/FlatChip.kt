package com.ccxiaoji.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 扁平化标签组件
 * 遵循极简扁平化设计（方案A）
 * 
 * @param label 标签文本
 * @param onClick 点击事件
 * @param modifier 修饰符
 * @param selected 是否选中
 * @param enabled 是否启用
 * @param leadingIcon 前置图标
 * @param trailingIcon 后置图标
 * @param containerColor 容器颜色
 * @param contentColor 内容颜色
 * @param borderColor 边框颜色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    },
    contentColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    },
    borderColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }
) {
    FilterChip(
        onClick = onClick,
        label = { Text(label) },
        selected = selected,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        ),
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            pressedElevation = 0.dp
        )
    )
}

/**
 * 扁平化输入标签
 * 用于可删除的标签
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatInputChip(
    label: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    InputChip(
        onClick = { },
        label = { Text(label) },
        selected = selected,
        enabled = enabled,
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        trailingIcon = {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "删除",
                    modifier = Modifier.size(14.dp),
                    tint = contentColor.copy(alpha = 0.6f)
                )
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        colors = InputChipDefaults.inputChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        ),
        elevation = InputChipDefaults.inputChipElevation(
            elevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            pressedElevation = 0.dp
        )
    )
}

/**
 * 扁平化选择标签
 * 用于单选或多选场景
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatSelectChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedIcon: ImageVector = Icons.Default.Check,
    containerColor: Color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surface
    },
    contentColor: Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
) {
    FilterChip(
        onClick = { onSelectedChange(!selected) },
        label = { Text(label) },
        selected = selected,
        enabled = enabled,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        modifier = modifier,
        shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            selectedContainerColor = containerColor,
            selectedLabelColor = contentColor,
            iconColor = contentColor,
            selectedLeadingIconColor = contentColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            }
        ),
        elevation = FilterChipDefaults.filterChipElevation(
            elevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            pressedElevation = 0.dp
        )
    )
}