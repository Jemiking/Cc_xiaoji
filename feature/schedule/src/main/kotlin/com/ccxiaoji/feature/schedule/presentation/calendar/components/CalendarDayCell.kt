package com.ccxiaoji.feature.schedule.presentation.calendar.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.ui.components.ModernCard
import androidx.compose.material3.CardDefaults
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 日历日期单元格
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarDayCell(
    date: LocalDate,
    schedule: Schedule?,
    isSelected: Boolean,
    isToday: Boolean,
    viewMode: CalendarViewMode,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    debugParams: com.ccxiaoji.feature.schedule.presentation.debug.CalendarViewParams? = null,
    modifier: Modifier = Modifier
) {
    // 根据视图模式动态调整文本样式和尺寸（可被调试参数覆盖字号）
    val baseDateTextStyle = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> MaterialTheme.typography.headlineMedium
        CalendarViewMode.COMPACT -> MaterialTheme.typography.titleMedium
    }
    val computedDateFontSize = debugParams?.dateNumberTextSize ?: when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 20.sp
        CalendarViewMode.COMPACT -> 14.sp
    }
    
    // 计算安全的lineHeight，避免文字被裁剪（支持调试参数）
    val lineHeightMultiplier = debugParams?.dateNumberLineHeightMultiplier ?: 1.25f
    val boldExtraSpace = debugParams?.fontWeightBoldExtraSpace ?: 0.1f
    val characterAdjustment = debugParams?.characterSpaceAdjustment ?: 0.0f
    
    val finalMultiplier = lineHeightMultiplier + 
                         (if (isToday) boldExtraSpace else 0f) + 
                         characterAdjustment
    val safeLineHeight = computedDateFontSize * finalMultiplier
    
    // 计算所需的最小容器高度（支持调试参数）
    val containerHeightMultiplier = debugParams?.minContainerHeightMultiplier ?: 2.5f
    val minRequiredHeight = (computedDateFontSize.value * containerHeightMultiplier).dp
    
    // 调试日志 - 帮助排查问题
    if (date.dayOfMonth == 1) { // 只在1号输出，减少日志量
        android.util.Log.d("CalendarDayCell", """
            调试参数检查:
            - debugParams存在: ${debugParams != null}
            - fontSize: ${computedDateFontSize.value}sp
            - lineHeightMultiplier: $lineHeightMultiplier
            - finalMultiplier: $finalMultiplier  
            - safeLineHeight: ${safeLineHeight.value}sp
            - containerHeightMultiplier: $containerHeightMultiplier
            - minRequiredHeight: ${minRequiredHeight.value}dp
        """.trimIndent())
    }
    
    val shiftLabelSize = debugParams?.let { 
        Pair(it.cellSize, it.cellSize * 0.5f) 
    } ?: when (viewMode) {
        CalendarViewMode.COMFORTABLE -> Pair(60.dp, 28.dp)
        CalendarViewMode.COMPACT -> Pair(45.dp, 18.dp)
    }
    
    val shiftLabelFontSize = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 16.sp
        CalendarViewMode.COMPACT -> 11.sp
    }
    
    val spacingBetween = debugParams?.cellSpacing ?: when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 8.dp
        CalendarViewMode.COMPACT -> 3.dp
    }
    
    // 动态计算容器高度，确保有足够空间显示内容
    val dynamicRowHeight = debugParams?.rowHeight?.let { debugHeight ->
        // 如果debug参数指定了高度，确保不小于最小需求
        maxOf(debugHeight, minRequiredHeight)
    } ?: when (viewMode) {
        CalendarViewMode.COMFORTABLE -> maxOf(56.dp, minRequiredHeight)
        CalendarViewMode.COMPACT -> maxOf(48.dp, minRequiredHeight)
    }
    
    ModernCard(
        modifier = modifier
            .height(dynamicRowHeight)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        backgroundColor = when {
            isSelected -> if (debugParams?.selectedDateBackgroundColor != null &&
                debugParams.selectedDateBackgroundColor != androidx.compose.ui.graphics.Color.Unspecified
            ) debugParams.selectedDateBackgroundColor else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            isToday -> if (debugParams?.todayDateBackgroundColor != null &&
                debugParams.todayDateBackgroundColor != androidx.compose.ui.graphics.Color.Unspecified
            ) debugParams.todayDateBackgroundColor else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
            else -> MaterialTheme.colorScheme.surface
        },
        borderColor = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            schedule != null -> ShiftColorMapper.getColorForShift(schedule.shift.color).copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        },
        cornerRadius = debugParams?.cornerRadius
        ,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 日期数字
                Text(
                    text = date.dayOfMonth.toString(),
                    style = TextStyle(
                        fontSize = computedDateFontSize,
                        lineHeight = safeLineHeight,
                        fontFamily = baseDateTextStyle.fontFamily,
                        letterSpacing = baseDateTextStyle.letterSpacing
                    ),
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    color = when {
                        isSelected -> if (debugParams?.selectedDateTextColor != null &&
                            debugParams.selectedDateTextColor != androidx.compose.ui.graphics.Color.Unspecified
                        ) debugParams.selectedDateTextColor else MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.secondary
                        date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) ->
                            DesignTokens.BrandColors.Error
                        else -> if (debugParams?.normalDateTextColor != null &&
                            debugParams.normalDateTextColor != androidx.compose.ui.graphics.Color.Unspecified
                        ) debugParams.normalDateTextColor else MaterialTheme.colorScheme.onSurface
                    }
                )
                
                // 班次信息（扁平化设计）
                schedule?.let { sch ->
                    Spacer(modifier = Modifier.height(spacingBetween))
                    Box(
                        modifier = Modifier
                            .size(width = shiftLabelSize.first, height = shiftLabelSize.second)
                            .background(
                                color = ShiftColorMapper.getBackgroundColorForShift(sch.shift.color, 0.1f),
                                shape = RoundedCornerShape(DesignTokens.BorderRadius.small)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sch.shift.name.take(2),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = shiftLabelFontSize
                            ),
                            color = ShiftColorMapper.getColorForShift(sch.shift.color),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
