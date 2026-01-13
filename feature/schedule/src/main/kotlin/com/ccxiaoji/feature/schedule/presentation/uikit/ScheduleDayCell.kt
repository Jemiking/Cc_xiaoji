package com.ccxiaoji.feature.schedule.presentation.uikit

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
// border removed - using pill-in-pill background selection style
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Schedule 模块日历日格子
 *
 * 用于月视图日历中显示单个日期及其班次信息。
 * 特点：
 * - 扁平化设计，无阴影
 * - 支持多种尺寸模式（紧凑/舒适）
 * - 清晰的状态视觉反馈（今天/选中/周末）
 * - 使用 ShiftPill 显示班次信息
 *
 * @param date 日期
 * @param schedule 该日期的排班信息，null 表示无班次
 * @param isSelected 是否被选中
 * @param isToday 是否是今天
 * @param modifier Modifier
 * @param size 格子尺寸模式
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 *
 * @see ShiftPill 班次胶囊标签
 * @see ScheduleDesignSpecs.CalendarCellSizes 格子尺寸规范
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ScheduleDayCell(
    date: LocalDate,
    schedule: Schedule?,
    isSelected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    size: DayCellSize = DayCellSize.Medium,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    // 交互状态
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 共享元素转场 Scope
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    // 计算日期状态
    val dateState = resolveDateState(
        isSelected = isSelected,
        isToday = isToday,
        isWeekend = date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
        hasShift = schedule != null
    )

    // 背景色动画 - UIDemo pill-in-pill 风格
    val animatedBackgroundColor by animateColorAsState(
        targetValue = dateState.backgroundColor,
        animationSpec = ScheduleDesignSpecs.Motion.flatSwitchSpec(),
        label = "backgroundColor"
    )

    // 文字颜色动画（对齐 UIDemo 的切换手感）
    val animatedTextColor by animateColorAsState(
        targetValue = dateState.textColor,
        animationSpec = ScheduleDesignSpecs.Motion.flatSwitchSpec(),
        label = "textColor"
    )

    // 按压缩放动画
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = ScheduleDesignSpecs.Motion.cellPressSpec(),
        label = "pressScale"
    )

    val sizeSpec = size.toSizeSpec()

    // 共享元素修饰符
    val sharedModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = SharedElementKeys.dateContainer(date.toEpochDay())
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    ScheduleDesignSpecs.Motion.sharedElementSpec()
                }
            )
        }
    } else {
        Modifier
    }

    val cellShape = RoundedCornerShape(ScheduleDesignSpecs.Corners.DayCell)
    val baseBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .then(sharedModifier)
            .height(sizeSpec.cellHeight)
            .fillMaxWidth()
            .scale(pressScale)
            .clip(cellShape)
            .background(baseBackgroundColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // 使用自定义缩放效果替代涟漪
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(ScheduleDesignSpecs.Spacing.CellPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        // 内层选中背景：pill-in-pill（外层 surfaceVariant，内层 surface）
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(cellShape)
                .background(animatedBackgroundColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = sizeSpec.verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sizeSpec.spacing)
        ) {
            // 日期数字（今天用圆形背景突出显示）
            DateNumber(
                day = date.dayOfMonth,
                isToday = isToday,
                textColor = animatedTextColor,
                fontSize = sizeSpec.dateFontSize
            )

            // 班次标签 - 使用 UIDemo 风格的 ShiftPill
            if (schedule != null) {
                ShiftPill(
                    name = schedule.shift.name.take(2),
                    color = schedule.shift.color,
                    icon = schedule.shift.inferShiftIcon(),
                    size = sizeSpec.shiftPillSize,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // 占位符，保持布局一致性
                EmptyShiftPill(
                    size = sizeSpec.shiftPillSize,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 空日期格子
 *
 * 用于日历月视图中非当月日期的占位。
 */
@Composable
fun EmptyDayCell(
    modifier: Modifier = Modifier,
    size: DayCellSize = DayCellSize.Medium
) {
    val sizeSpec = size.toSizeSpec()

    Box(
        modifier = modifier
            .height(sizeSpec.cellHeight)
            .fillMaxWidth()
    )
}

/**
 * 日期数字组件
 *
 * 内部组件，用于渲染日期数字。
 * 今天的日期使用圆形背景突出显示。
 */
@Composable
private fun DateNumber(
    day: Int,
    isToday: Boolean,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    if (isToday) {
        // 今天使用圆形背景
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(ScheduleDesignSpecs.ModuleColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 1
            )
        }
    } else {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

/**
 * 解析日期状态 - UIDemo pill-in-pill 风格
 *
 * 选中态使用 surface 背景（220ms 动画），非选中透明
 */
@Composable
private fun resolveDateState(
    isSelected: Boolean,
    isToday: Boolean,
    isWeekend: Boolean,
    @Suppress("UNUSED_PARAMETER") hasShift: Boolean // 预留：未来可用于有班次日期的特殊样式
): DateCellState {
    return when {
        isSelected -> DateCellState(
            backgroundColor = MaterialTheme.colorScheme.surface,
            textColor = ScheduleDesignSpecs.ModuleColors.Primary
        )
        isToday -> DateCellState(
            backgroundColor = Color.Transparent,
            textColor = MaterialTheme.colorScheme.onSurface // 文字颜色由圆形背景处理
        )
        isWeekend -> DateCellState(
            backgroundColor = Color.Transparent,
            textColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )
        else -> DateCellState(
            backgroundColor = Color.Transparent,
            textColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 日期格子状态数据 - UIDemo 风格（无边框）
 */
@Stable
private data class DateCellState(
    val backgroundColor: Color,
    val textColor: Color
)

/**
 * 日格子尺寸规格数据
 */
@Stable
internal data class DayCellSizeSpec(
    val cellHeight: Dp,
    val dateFontSize: androidx.compose.ui.unit.TextUnit,
    val shiftPillSize: ShiftPillSize,
    val verticalPadding: Dp,
    val spacing: Dp
)

/**
 * 日格子尺寸枚举
 */
@Stable
enum class DayCellSize {
    /** 紧凑模式 - 适用于小屏幕或信息密集视图 */
    Compact,

    /** 中等模式 - 默认大小 */
    Medium,

    /** 舒适模式 - 适用于大屏幕或注重可读性 */
    Comfortable;

    internal fun toSizeSpec(): DayCellSizeSpec = when (this) {
        Compact -> DayCellSizeSpec(
            cellHeight = ScheduleDesignSpecs.CalendarCellSizes.CompactCellHeight,
            dateFontSize = 12.sp,
            shiftPillSize = ShiftPillSize.Small,
            verticalPadding = 4.dp,
            spacing = 2.dp
        )
        Medium -> DayCellSizeSpec(
            cellHeight = ScheduleDesignSpecs.CalendarCellSizes.MediumCellHeight,
            dateFontSize = 14.sp,
            shiftPillSize = ShiftPillSize.Medium,
            verticalPadding = 6.dp,
            spacing = 4.dp
        )
        Comfortable -> DayCellSizeSpec(
            cellHeight = ScheduleDesignSpecs.CalendarCellSizes.ComfortableCellHeight,
            dateFontSize = 16.sp,
            shiftPillSize = ShiftPillSize.Large,
            verticalPadding = 8.dp,
            spacing = 6.dp
        )
    }
}
