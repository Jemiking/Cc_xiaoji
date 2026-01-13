package com.ccxiaoji.feature.schedule.presentation.aurora

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Aurora 风格日历单元格
 *
 * 设计规格：
 * - 尺寸: 48x64dp (稍高以容纳班次胶囊)
 * - 圆角: 14dp
 * - 间距: 6dp (网格间)
 * - 选中态: Aurora渐变描边2dp + 10%填充 + 缩放动画
 * - 今日: 日期数字外圈2dp环形描边
 * - 周末: 浅蓝背景 #F1F5FF
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AuroraCalendarDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    shift: ShiftType?,
    modifier: Modifier = Modifier,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 点击缩放动画: 使用统一动画规格
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.96f
            isSelected -> 1.02f
            else -> 1.0f
        },
        animationSpec = AuroraAnimations.Springs.tapBounceBack(),
        label = "cell_scale"
    )

    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    // 背景颜色
    val backgroundColor = when {
        isSelected -> ScheduleAuroraColors.PrimaryBlue.copy(alpha = 0.1f)
        isWeekend && !isDarkMode -> ScheduleAuroraColors.WeekendBackground
        isDarkMode -> ScheduleAuroraColors.CardSurfaceDark
        else -> ScheduleAuroraColors.CardSurfaceLight
    }

    // 边框
    val borderModifier = when {
        isSelected -> Modifier.border(
            width = 2.dp,
            brush = ScheduleAuroraColors.AuroraGradient,
            shape = RoundedCornerShape(14.dp)
        )
        else -> Modifier.border(
            width = 1.dp,
            color = if (isDarkMode) ScheduleAuroraColors.BorderDark else ScheduleAuroraColors.BorderLight,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // 文字颜色
    val dateTextColor = when {
        !isCurrentMonth -> if (isDarkMode) ScheduleAuroraColors.TextTertiaryDark else ScheduleAuroraColors.TextTertiaryLight
        isSelected -> ScheduleAuroraColors.PrimaryBlue
        isToday -> ScheduleAuroraColors.PrimaryBlue
        isWeekend -> if (isDarkMode) ScheduleAuroraColors.TextSecondaryDark else ScheduleAuroraColors.TextSecondaryLight
        else -> if (isDarkMode) ScheduleAuroraColors.TextPrimaryDark else ScheduleAuroraColors.TextPrimaryLight
    }

    // 构建无障碍语义描述
    val cellDescription = buildString {
        append("${date.monthValue}月${date.dayOfMonth}日")
        if (isToday) append(", 今天")
        if (isSelected) append(", 已选中")
        if (shift != null) append(", ${shift.displayName}")
    }

    Box(
        modifier = modifier
            .size(48.dp, 64.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .then(borderModifier)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = cellDescription
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = if (onLongClick != null) {
                    {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                } else null
            )
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // 日期数字
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (isToday) {
                            Modifier.border(
                                width = 2.dp,
                                color = ScheduleAuroraColors.PrimaryBlue,
                                shape = CircleShape
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 14.sp,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = dateTextColor,
                    textAlign = TextAlign.Center
                )
            }

            // 班次指示器
            if (shift != null) {
                ShiftPillMini(
                    shiftType = shift,
                    showIcon = false, // 日历格内只显示文字以节省空间
                    isDarkMode = isDarkMode
                )
            } else {
                // 占位空间保持布局一致
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

/**
 * 紧凑型日历单元格 - 只显示颜色点
 * 用于信息密度更高的紧凑模式
 */
@Composable
fun AuroraCalendarDayCellCompact(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    shift: ShiftType?,
    modifier: Modifier = Modifier,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit = {},
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "compact_cell_scale"
    )

    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY

    val backgroundColor = when {
        isSelected -> ScheduleAuroraColors.PrimaryBlue.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        !isCurrentMonth -> if (isDarkMode) ScheduleAuroraColors.TextTertiaryDark else ScheduleAuroraColors.TextTertiaryLight
        isToday -> ScheduleAuroraColors.PrimaryBlue
        isWeekend -> if (isDarkMode) ScheduleAuroraColors.TextSecondaryDark else ScheduleAuroraColors.TextSecondaryLight
        else -> if (isDarkMode) ScheduleAuroraColors.TextPrimaryDark else ScheduleAuroraColors.TextPrimaryLight
    }

    Column(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 日期数字
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 13.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 班次颜色点
        if (shift != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(ScheduleAuroraColors.getShiftPrimaryColor(shift.code, isDarkMode))
            )
        } else {
            Spacer(modifier = Modifier.size(6.dp))
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewAuroraCalendarDayCell() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        // 今日+选中+早班
        AuroraCalendarDayCell(
            date = LocalDate.now(),
            isSelected = true,
            isToday = true,
            shift = ShiftType.MORNING
        )

        // 普通日+晚班
        AuroraCalendarDayCell(
            date = LocalDate.now().plusDays(1),
            isSelected = false,
            isToday = false,
            shift = ShiftType.NIGHT
        )

        // 周末+休息
        AuroraCalendarDayCell(
            date = LocalDate.of(2025, 12, 27), // 周六
            isSelected = false,
            isToday = false,
            shift = ShiftType.REST
        )

        // 无班次
        AuroraCalendarDayCell(
            date = LocalDate.now().plusDays(3),
            isSelected = false,
            isToday = false,
            shift = null
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F7FB)
@Composable
private fun PreviewAuroraCalendarDayCellCompact() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        AuroraCalendarDayCellCompact(
            date = LocalDate.now(),
            isSelected = true,
            isToday = true,
            shift = ShiftType.MORNING
        )
        AuroraCalendarDayCellCompact(
            date = LocalDate.now().plusDays(1),
            isSelected = false,
            isToday = false,
            shift = ShiftType.NIGHT
        )
        AuroraCalendarDayCellCompact(
            date = LocalDate.now().plusDays(2),
            isSelected = false,
            isToday = false,
            shift = null
        )
    }
}
