package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 班次颜色选择器
 *
 * 用于在班次编辑页面选择班次颜色的组件。
 * 支持水平滚动和网格两种布局模式。
 *
 * 设计要点:
 * - 满足 48dp 最小触控热区 (无障碍)
 * - 选中状态显示勾选图标 + 边框
 * - 根据颜色亮度自动调整勾选图标颜色
 * - 提供语义化的无障碍描述
 *
 * @param colors 可选颜色列表 (Int 格式)
 * @param selectedColor 当前选中的颜色
 * @param onColorSelected 颜色选中回调
 * @param modifier Modifier
 * @param layout 布局模式 (Row/Grid)
 * @param itemSize 颜色项尺寸
 * @param label 可选的标题文本
 *
 * @see ShiftColorIndicator 颜色指示器基础组件
 */
@Composable
fun ShiftColorSelector(
    colors: List<Int>,
    selectedColor: Int?,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    layout: ColorSelectorLayout = ColorSelectorLayout.Row,
    itemSize: Dp = 48.dp,
    label: String? = null
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
            )
        }

        when (layout) {
            ColorSelectorLayout.Row -> {
                ColorSelectorRow(
                    colors = colors,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected,
                    itemSize = itemSize
                )
            }
            ColorSelectorLayout.Grid -> {
                ColorSelectorGrid(
                    colors = colors,
                    selectedColor = selectedColor,
                    onColorSelected = onColorSelected,
                    itemSize = itemSize
                )
            }
        }
    }
}

/**
 * 水平滚动布局的颜色选择器
 */
@Composable
private fun ColorSelectorRow(
    colors: List<Int>,
    selectedColor: Int?,
    onColorSelected: (Int) -> Unit,
    itemSize: Dp
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
        contentPadding = PaddingValues(horizontal = DesignTokens.Spacing.xs)
    ) {
        items(colors, key = { it }) { color ->
            ColorSelectorItem(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) },
                size = itemSize
            )
        }
    }
}

/**
 * 网格布局的颜色选择器
 */
@Composable
private fun ColorSelectorGrid(
    colors: List<Int>,
    selectedColor: Int?,
    onColorSelected: (Int) -> Unit,
    itemSize: Dp
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = itemSize + DesignTokens.Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
        contentPadding = PaddingValues(DesignTokens.Spacing.xs),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(colors, key = { it }) { color ->
            ColorSelectorItem(
                color = color,
                isSelected = color == selectedColor,
                onClick = { onColorSelected(color) },
                size = itemSize
            )
        }
    }
}

/**
 * 单个颜色选择项
 */
@Composable
private fun ColorSelectorItem(
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    size: Dp
) {
    val composeColor = Color(color)
    val colorName = getColorName(color)

    // 根据颜色亮度决定勾选图标颜色
    val checkmarkColor = if (composeColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(composeColor)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                selected = isSelected
                contentDescription = if (isSelected) {
                    "已选择 $colorName"
                } else {
                    "选择 $colorName"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = checkmarkColor,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
}

/**
 * 布局模式枚举
 */
@Stable
enum class ColorSelectorLayout {
    /** 水平滚动行 */
    Row,
    /** 网格布局 */
    Grid
}

/**
 * 根据颜色值获取颜色名称 (用于无障碍)
 */
private fun getColorName(color: Int): String {
    return when (color) {
        // 预设颜色名称映射
        0xFF4CAF50.toInt() -> "绿色"
        0xFF81C784.toInt() -> "浅绿色"
        0xFF2196F3.toInt() -> "蓝色"
        0xFF64B5F6.toInt() -> "浅蓝色"
        0xFF9C27B0.toInt() -> "紫色"
        0xFFBA68C8.toInt() -> "浅紫色"
        0xFFFF9800.toInt() -> "橙色"
        0xFFFFB74D.toInt() -> "浅橙色"
        0xFFF44336.toInt() -> "红色"
        0xFFE57373.toInt() -> "浅红色"
        0xFF9E9E9E.toInt() -> "灰色"
        0xFFBDBDBD.toInt() -> "浅灰色"
        0xFFFFEB3B.toInt() -> "黄色"
        0xFFFFF176.toInt() -> "浅黄色"
        0xFF00BCD4.toInt() -> "青色"
        0xFF4DD0E1.toInt() -> "浅青色"
        0xFF795548.toInt() -> "棕色"
        0xFFA1887F.toInt() -> "浅棕色"
        0xFF607D8B.toInt() -> "蓝灰色"
        0xFF90A4AE.toInt() -> "浅蓝灰色"
        0xFFE91E63.toInt() -> "粉红色"
        0xFFF48FB1.toInt() -> "浅粉红色"
        0xFF3F51B5.toInt() -> "靛蓝色"
        0xFF7986CB.toInt() -> "浅靛蓝色"
        0xFF009688.toInt() -> "青绿色"
        0xFF4DB6AC.toInt() -> "浅青绿色"
        0xFF8BC34A.toInt() -> "黄绿色"
        0xFFAED581.toInt() -> "浅黄绿色"
        0xFFCDDC39.toInt() -> "酸橙色"
        0xFFDCE775.toInt() -> "浅酸橙色"
        else -> "自定义颜色"
    }
}
