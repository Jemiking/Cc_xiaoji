package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 统一选项行组件
 *
 * 用于设置页面、选择器等场景的选项行。
 * 支持带图标、标题、副标题、尾部内容的灵活配置。
 *
 * 设计要点:
 * - 满足 48dp 最小触控高度 (无障碍)
 * - 统一的视觉风格
 * - 支持禁用状态
 *
 * @param title 主标题
 * @param onClick 点击回调
 * @param modifier Modifier
 * @param subtitle 副标题 (可选)
 * @param leadingIcon 前置图标 (可选)
 * @param trailing 尾部内容 (可选)
 * @param enabled 是否启用
 */
@Composable
fun ScheduleOptionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true
) {
    val contentAlpha = if (enabled) 1f else ScheduleDesignSpecs.DateStateStyles.DisabledAlpha

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small + DesignTokens.Spacing.xs
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 前置图标
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
        }

        // 标题区域
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }

        // 尾部内容
        if (trailing != null) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            ) {
                trailing()
            }
        }
    }
}

/**
 * 带导航箭头的选项行
 *
 * 常用于跳转到下一级页面的设置项。
 */
@Composable
fun ScheduleNavigationRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    value: String? = null,
    enabled: Boolean = true
) {
    ScheduleOptionRow(
        title = title,
        onClick = onClick,
        modifier = modifier,
        subtitle = subtitle,
        leadingIcon = leadingIcon,
        enabled = enabled,
        trailing = {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}

/**
 * 单选项行
 *
 * 用于单选列表中的选项。
 */
@Composable
fun ScheduleRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small + DesignTokens.Spacing.xs
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        val contentAlpha = if (enabled) 1f else ScheduleDesignSpecs.DateStateStyles.DisabledAlpha

        // 前置图标
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
            )
        }

        // 标题区域
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }

        // 单选按钮
        RadioButton(
            selected = selected,
            onClick = null, // 由 Row 的 selectable 处理
            enabled = enabled
        )
    }
}

/**
 * 单选组容器
 *
 * 为一组单选项提供语义化分组。
 */
@Composable
fun ScheduleRadioGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.selectableGroup()
    ) {
        content()
    }
}

/**
 * 危险操作行
 *
 * 用于删除、清除等危险操作的选项行。
 */
@Composable
fun ScheduleDangerRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    val dangerColor = MaterialTheme.colorScheme.error
    val contentAlpha = if (enabled) 1f else ScheduleDesignSpecs.DateStateStyles.DisabledAlpha

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { }
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small + DesignTokens.Spacing.xs
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 前置图标
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = dangerColor.copy(alpha = contentAlpha)
            )
        }

        // 标题区域
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = dangerColor.copy(alpha = contentAlpha)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
    }
}
