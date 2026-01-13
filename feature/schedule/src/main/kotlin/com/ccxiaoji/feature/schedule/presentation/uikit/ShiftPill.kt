package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Shift

/**
 * 班次胶囊标签（ShiftPill）- UIDemo 风格
 *
 * 设计要点：
 * - 以 surface/surfaceVariant 为基础色（无阴影）
 * - 使用「颜色点 + 图标 + 粗体文本」表达语义
 * - 选中态使用 220ms 颜色过渡动画
 */
@Stable
enum class ShiftPillStyle {
    /** 日历格子内的紧凑标签（自带 surface 背景与细边框） */
    CalendarTag,
    /** Dock/筛选条中的可选标签（背景由父容器承担，本组件仅做选中态高亮） */
    DockTab
}

@Composable
fun ShiftPill(
    shift: Shift,
    modifier: Modifier = Modifier,
    size: ShiftPillSize = ShiftPillSize.Medium,
    showIcon: Boolean = true,
    selected: Boolean = false,
    style: ShiftPillStyle = ShiftPillStyle.CalendarTag,
    onClick: (() -> Unit)? = null
) {
    ShiftPillImpl(
        name = shift.name,
        shiftColor = Color(shift.color),
        modifier = modifier,
        height = size.height,
        icon = if (showIcon) shift.inferShiftIcon() else null,
        selected = selected,
        style = style,
        onClick = onClick
    )
}

@Composable
fun ShiftPill(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: ShiftPillSize = ShiftPillSize.Medium,
    icon: ImageVector? = null,
    selected: Boolean = false,
    style: ShiftPillStyle = ShiftPillStyle.CalendarTag,
    onClick: (() -> Unit)? = null
) {
    ShiftPillImpl(
        name = name,
        shiftColor = color,
        modifier = modifier,
        height = size.height,
        icon = icon,
        selected = selected,
        style = style,
        onClick = onClick
    )
}

@Composable
fun ShiftPill(
    name: String,
    color: Int,
    modifier: Modifier = Modifier,
    size: ShiftPillSize = ShiftPillSize.Medium,
    icon: ImageVector? = null,
    selected: Boolean = false,
    style: ShiftPillStyle = ShiftPillStyle.CalendarTag,
    onClick: (() -> Unit)? = null
) {
    ShiftPillImpl(
        name = name,
        shiftColor = Color(color),
        modifier = modifier,
        height = size.height,
        icon = icon,
        selected = selected,
        style = style,
        onClick = onClick
    )
}

/**
 * 内部实现 - UIDemo 风格 Pill-in-Pill
 */
@Composable
private fun ShiftPillImpl(
    name: String,
    modifier: Modifier,
    height: Dp,
    shiftColor: Color,
    icon: ImageVector?,
    selected: Boolean,
    style: ShiftPillStyle,
    onClick: (() -> Unit)?
) {
    val shape = ScheduleDesignSpecs.ShiftTagSpecs.Shape

    val targetBackgroundColor = when (style) {
        ShiftPillStyle.CalendarTag -> MaterialTheme.colorScheme.surface
        ShiftPillStyle.DockTab -> if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
    }

    val backgroundColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = ScheduleDesignSpecs.Motion.flatSwitchSpec(),
        label = "ShiftPillBackground"
    )

    val targetContentColor = when (style) {
        ShiftPillStyle.CalendarTag -> MaterialTheme.colorScheme.onSurface
        ShiftPillStyle.DockTab -> if (selected) shiftColor else MaterialTheme.colorScheme.onSurface
    }

    val contentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = ScheduleDesignSpecs.Motion.flatSwitchSpec(),
        label = "ShiftPillContentColor"
    )

    val dotSize = when {
        height >= 44.dp -> 10.dp
        height <= 18.dp -> 6.dp
        else -> 8.dp
    }

    val iconSize = when {
        height >= 44.dp -> 18.dp
        height <= 18.dp -> 12.dp
        else -> 14.dp
    }

    val textStyle = if (height >= 44.dp) {
        MaterialTheme.typography.labelLarge.copy(
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    } else {
        MaterialTheme.typography.labelSmall.copy(
            fontSize = if (height <= 18.dp) 9.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = if (height <= 18.dp) 12.sp else 16.sp
        )
    }

    val horizontalPadding = when {
        height >= 44.dp -> 12.dp
        else -> 8.dp
    }

    Row(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(backgroundColor)
            .then(
                if (style == ShiftPillStyle.CalendarTag) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        shape = shape
                    )
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        // Dot indicator - 颜色语义点（对齐 UIDemo TransactionItem）
        Box(
            modifier = Modifier
                .size(dotSize)
                .clip(CircleShape)
                .background(shiftColor)
        )

        val shouldShowIcon = icon != null && (style == ShiftPillStyle.DockTab || height >= 20.dp)
        if (shouldShowIcon) {
            Icon(
                imageVector = icon!!,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = when (style) {
                    ShiftPillStyle.CalendarTag -> shiftColor
                    ShiftPillStyle.DockTab -> contentColor
                }
            )
        }

        Text(
            text = name,
            style = textStyle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * 休息状态胶囊 - UIDemo 风格
 */
@Composable
fun RestPill(
    modifier: Modifier = Modifier,
    size: ShiftPillSize = ShiftPillSize.Medium,
    text: String = "休息"
) {
    val shape = ScheduleDesignSpecs.ShiftTagSpecs.Shape
    Box(
        modifier = modifier
            .height(size.height)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = shape
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = if (size.height <= 18.dp) 9.sp else 11.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * 空状态胶囊
 */
@Composable
fun EmptyShiftPill(
    modifier: Modifier = Modifier,
    size: ShiftPillSize = ShiftPillSize.Small
) {
    Box(modifier = modifier.height(size.height))
}

/**
 * 胶囊标签尺寸枚举
 */
@Stable
enum class ShiftPillSize(val height: Dp) {
    /** 小尺寸 - 用于紧凑模式日历 */
    Small(16.dp),

    /** 中等尺寸 - 默认大小 */
    Medium(20.dp),

    /** 大尺寸 - 用于舒适模式日历 */
    Large(24.dp),

    /** Dock 尺寸 - 用于底部浮动条/筛选条（对齐 UIDemo TabSelector 高度） */
    Dock(44.dp)
}

/**
 * 推断班次图标（太阳/月亮）
 *
 * 规则：
 * - 跨天（夜班）→ 月亮
 * - 非跨天 → 太阳
 */
fun Shift.inferShiftIcon(): ImageVector {
    return if (isOvernight) Icons.Filled.Bedtime else Icons.Filled.WbSunny
}
