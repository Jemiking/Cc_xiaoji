package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 班次列表行
 *
 * 用于班次选择列表、班次管理列表等场景。
 * 特点：
 * - 左侧颜色指示器
 * - 中间班次信息（名称、时间）
 * - 右侧可选操作/选中状态
 *
 * @param shift 班次数据，null 表示休息/无班次
 * @param selected 是否选中
 * @param onClick 点击回调，null 时不可点击
 * @param modifier Modifier
 * @param enabled 是否启用
 * @param showTimeRange 是否显示时间范围
 * @param trailing 尾部内容（如操作按钮、选中图标）
 *
 * @see ShiftColorIndicator 颜色指示器
 */
@Composable
fun ShiftRow(
    shift: Shift?,
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showTimeRange: Boolean = true,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val backgroundColor = when {
        selected -> ScheduleDesignSpecs.ModuleColors.PrimaryContainer
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
            .background(backgroundColor)
            .then(
                if (onClick != null && enabled) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 颜色指示器
        if (shift != null) {
            ShiftColorIndicator(
                color = shift.color,
                size = ShiftIndicatorSize.Medium,
                shape = ShiftIndicatorShape.Rounded
            )
        } else {
            RestIndicator(size = ShiftIndicatorSize.Medium)
        }

        // 班次信息
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = shift?.name ?: "休息",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            if (showTimeRange && shift != null) {
                Text(
                    text = shift.timeRangeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }
        }

        // 尾部内容
        if (trailing != null) {
            Row(content = trailing)
        } else if (selected) {
            // 默认选中图标
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "已选中",
                tint = ScheduleDesignSpecs.ModuleColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 可点击的班次行（带箭头）
 *
 * 用于导航到详情页的场景。
 */
@Composable
fun ShiftNavigationRow(
    shift: Shift,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showTimeRange: Boolean = true
) {
    ShiftRow(
        shift = shift,
        selected = false,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        showTimeRange = showTimeRange,
        trailing = {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    )
}

/**
 * 休息行
 *
 * 专用于显示休息/无班次状态。
 */
@Composable
fun RestRow(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ShiftRow(
        shift = null,
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        showTimeRange = false
    )
}

/**
 * 班次选择列表项
 *
 * 用于单选场景，自带 RadioButton。
 */
@Composable
fun ShiftRadioRow(
    shift: Shift?,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignTokens.BorderRadius.medium))
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(
                horizontal = DesignTokens.Spacing.medium,
                vertical = DesignTokens.Spacing.small
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // 由 Row 的 clickable 处理
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = ScheduleDesignSpecs.ModuleColors.Primary
            )
        )

        // 颜色指示器
        if (shift != null) {
            ShiftColorIndicator(
                color = shift.color,
                size = ShiftIndicatorSize.Small,
                shape = ShiftIndicatorShape.Rounded
            )
        } else {
            RestIndicator(size = ShiftIndicatorSize.Small)
        }

        // 班次名称
        Text(
            text = shift?.name ?: "休息",
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.weight(1f)
        )
    }
}
