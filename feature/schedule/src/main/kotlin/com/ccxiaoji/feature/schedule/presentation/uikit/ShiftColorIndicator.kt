package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 班次颜色指示器
 *
 * 用于显示班次的颜色标识，支持多种尺寸和形状。
 * 是 Schedule UI Kit 的基础组件，被 ShiftPill、ShiftRow 等组件依赖。
 *
 * @param color 班次颜色（Int 格式，与 Shift.color 兼容）
 * @param modifier Modifier
 * @param size 指示器尺寸
 * @param shape 指示器形状
 * @param icon 可选的图标（用于无障碍双重识别）
 * @param showBorder 是否显示边框
 *
 * @see ScheduleDesignSpecs.ShiftColors 班次语义色定义
 */
@Composable
fun ShiftColorIndicator(
    color: Int,
    modifier: Modifier = Modifier,
    size: ShiftIndicatorSize = ShiftIndicatorSize.Medium,
    shape: ShiftIndicatorShape = ShiftIndicatorShape.Rounded,
    icon: ImageVector? = null,
    showBorder: Boolean = false
) {
    ShiftColorIndicatorImpl(
        color = Color(color),
        modifier = modifier,
        sizeDp = size.dp,
        shape = shape.toComposeShape(),
        icon = icon,
        showBorder = showBorder
    )
}

/**
 * 班次颜色指示器（使用 Compose Color）
 *
 * 重载版本，直接接受 Compose Color 参数。
 */
@Composable
fun ShiftColorIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    size: ShiftIndicatorSize = ShiftIndicatorSize.Medium,
    shape: ShiftIndicatorShape = ShiftIndicatorShape.Rounded,
    icon: ImageVector? = null,
    showBorder: Boolean = false
) {
    ShiftColorIndicatorImpl(
        color = color,
        modifier = modifier,
        sizeDp = size.dp,
        shape = shape.toComposeShape(),
        icon = icon,
        showBorder = showBorder
    )
}

/**
 * 班次颜色指示器（自定义尺寸）
 *
 * 重载版本，允许自定义尺寸值。
 */
@Composable
fun ShiftColorIndicator(
    color: Int,
    sizeDp: Dp,
    modifier: Modifier = Modifier,
    shape: ShiftIndicatorShape = ShiftIndicatorShape.Rounded,
    icon: ImageVector? = null,
    showBorder: Boolean = false
) {
    ShiftColorIndicatorImpl(
        color = Color(color),
        modifier = modifier,
        sizeDp = sizeDp,
        shape = shape.toComposeShape(),
        icon = icon,
        showBorder = showBorder
    )
}

/**
 * 内部实现
 */
@Composable
private fun ShiftColorIndicatorImpl(
    color: Color,
    modifier: Modifier,
    sizeDp: Dp,
    shape: Shape,
    icon: ImageVector?,
    showBorder: Boolean
) {
    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(shape)
            .background(color)
            .then(
                if (showBorder) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = shape
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            val iconSize = (sizeDp.value * 0.6f).dp
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = Color.White
            )
        }
    }
}

/**
 * 指示器尺寸枚举
 */
@Stable
enum class ShiftIndicatorSize(val dp: Dp) {
    /** 极小尺寸 - 用于紧凑列表 */
    ExtraSmall(16.dp),

    /** 小尺寸 - 用于日历格子内 */
    Small(24.dp),

    /** 中等尺寸 - 默认大小，用于列表行 */
    Medium(32.dp),

    /** 大尺寸 - 用于详情页或编辑界面 */
    Large(40.dp),

    /** 超大尺寸 - 用于颜色选择器 */
    ExtraLarge(48.dp)
}

/**
 * 指示器形状枚举
 */
@Stable
enum class ShiftIndicatorShape {
    /** 圆形 */
    Circle,

    /** 圆角矩形 */
    Rounded,

    /** 正方形（小圆角） */
    Square;

    fun toComposeShape(): Shape = when (this) {
        Circle -> CircleShape
        Rounded -> RoundedCornerShape(DesignTokens.BorderRadius.small)
        Square -> RoundedCornerShape(2.dp)
    }
}

/**
 * 休息状态的颜色指示器
 *
 * 特殊版本，使用虚线或特殊样式表示休息/无班次。
 */
@Composable
fun RestIndicator(
    modifier: Modifier = Modifier,
    size: ShiftIndicatorSize = ShiftIndicatorSize.Medium
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(ShiftIndicatorShape.Rounded.toComposeShape())
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = ShiftIndicatorShape.Rounded.toComposeShape()
            ),
        contentAlignment = Alignment.Center
    ) {
        // 可以添加休息图标
    }
}
