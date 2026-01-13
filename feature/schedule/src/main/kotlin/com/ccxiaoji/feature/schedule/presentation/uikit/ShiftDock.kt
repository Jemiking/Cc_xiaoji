package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.domain.model.Shift

/**
 * ShiftDock：底部浮动班次选择坞（TabSelector 风格）
 *
 * 设计要点（对齐 UIDemo TabSelector）：
 * - 外层容器：surfaceVariant + 4dp padding + 16dp 圆角
 * - 内部 pills：选中态 surface 背景，非选中透明；220ms 颜色动画
 * - 进入/退出动画：slideIn/slideOut + fade
 */
@Composable
fun ShiftDock(
    visible: Boolean,
    shifts: List<Shift>,
    selectedShiftId: Long?,
    onSelectedShiftIdChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    includeRest: Boolean = true,
    restLabel: String = "休息",
    horizontalPadding: Dp = 16.dp,
    bottomMargin: Dp = 16.dp
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val enterOffsetSpec = tween<IntOffset>(
        durationMillis = ScheduleDesignSpecs.Motion.Duration.DockEnter,
        easing = ScheduleDesignSpecs.Motion.Easing.Emphasized
    )
    val exitOffsetSpec = tween<IntOffset>(
        durationMillis = ScheduleDesignSpecs.Motion.Duration.DockExit,
        easing = ScheduleDesignSpecs.Motion.Easing.Accelerate
    )
    val enterAlphaSpec = tween<Float>(
        durationMillis = ScheduleDesignSpecs.Motion.Duration.DockEnter,
        easing = ScheduleDesignSpecs.Motion.Easing.Emphasized
    )
    val exitAlphaSpec = tween<Float>(
        durationMillis = ScheduleDesignSpecs.Motion.Duration.DockExit,
        easing = ScheduleDesignSpecs.Motion.Easing.Accelerate
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = enterOffsetSpec,
            initialOffsetY = { fullHeight -> fullHeight }
        ) + fadeIn(animationSpec = enterAlphaSpec),
        exit = slideOutVertically(
            animationSpec = exitOffsetSpec,
            targetOffsetY = { fullHeight -> fullHeight }
        ) + fadeOut(animationSpec = exitAlphaSpec),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(bottom = bottomInset + bottomMargin)
    ) {
        LazyRow(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            if (includeRest) {
                item(key = "rest") {
                    ShiftPill(
                        name = restLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        icon = Icons.Filled.Coffee,
                        size = ShiftPillSize.Dock,
                        selected = selectedShiftId == null,
                        style = ShiftPillStyle.DockTab,
                        onClick = { onSelectedShiftIdChange(null) }
                    )
                }
            }

            items(shifts, key = { it.id }) { shift ->
                ShiftPill(
                    name = shift.name,
                    color = Color(shift.color),
                    icon = shift.inferShiftIcon(),
                    size = ShiftPillSize.Dock,
                    selected = shift.id == selectedShiftId,
                    style = ShiftPillStyle.DockTab,
                    onClick = { onSelectedShiftIdChange(shift.id) }
                )
            }
        }
    }
}
