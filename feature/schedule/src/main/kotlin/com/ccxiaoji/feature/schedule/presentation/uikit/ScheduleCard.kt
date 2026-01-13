package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * Schedule 模块统一扁平卡片容器
 *
 * 基于 Material 3 Card，针对 Schedule 模块的视觉规范进行定制。
 * 特点：
 * - 极简扁平设计，无阴影
 * - 统一的圆角和边框
 * - 支持可选的点击交互
 *
 * @param modifier Modifier
 * @param onClick 点击回调，为 null 时卡片不可点击
 * @param enabled 是否启用交互
 * @param containerColor 背景色
 * @param contentPadding 内容内边距
 * @param content 卡片内容
 *
 * @see ScheduleSectionCard 带标题的区块卡片
 */
@Composable
fun ScheduleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(DesignTokens.Spacing.medium),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(ScheduleDesignSpecs.Corners.SectionCard)
    val border = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    )
    val colors = CardDefaults.cardColors(
        containerColor = containerColor,
        disabledContainerColor = containerColor.copy(alpha = 0.6f)
    )
    val elevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
        draggedElevation = 0.dp,
        disabledElevation = 0.dp
    )

    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * 带标题的区块卡片
 *
 * 用于将相关内容分组显示，带有标题和可选的尾部操作。
 *
 * @param title 区块标题
 * @param modifier Modifier
 * @param subtitle 可选的副标题
 * @param headerTrailing 标题行尾部内容（如操作按钮）
 * @param contentPadding 内容区域内边距
 * @param content 卡片内容
 */
@Composable
fun ScheduleSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerTrailing: (@Composable RowScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(DesignTokens.Spacing.medium),
    content: @Composable ColumnScope.() -> Unit
) {
    ScheduleCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        // 标题区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = DesignTokens.Spacing.medium,
                    vertical = DesignTokens.Spacing.small
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (headerTrailing != null) {
                Row(content = headerTrailing)
            }
        }

        // 分隔线
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
        )

        // 内容区域
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * 可选中的卡片
 *
 * 用于单选/多选场景，如班次选择。
 *
 * @param selected 是否选中
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param enabled 是否启用
 * @param content 卡片内容
 */
@Composable
fun ScheduleSelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = if (selected) {
        ScheduleDesignSpecs.ModuleColors.PrimaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (selected) {
        ScheduleDesignSpecs.ModuleColors.Primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }
    val shape = RoundedCornerShape(ScheduleDesignSpecs.Corners.SectionCard)

    Card(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium),
            content = content
        )
    }
}
