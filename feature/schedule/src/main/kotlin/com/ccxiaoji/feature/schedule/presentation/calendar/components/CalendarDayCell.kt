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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.ui.components.ModernCard
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
    modifier: Modifier = Modifier
) {
    // 根据视图模式动态调整文本样式和尺寸
    val dateTextStyle = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> MaterialTheme.typography.headlineMedium
        CalendarViewMode.COMPACT -> MaterialTheme.typography.titleMedium
    }
    
    val shiftLabelSize = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> Pair(60.dp, 28.dp)
        CalendarViewMode.COMPACT -> Pair(45.dp, 18.dp)
    }
    
    val shiftLabelFontSize = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 16.sp
        CalendarViewMode.COMPACT -> 11.sp
    }
    
    val spacingBetween = when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 8.dp
        CalendarViewMode.COMPACT -> 3.dp
    }
    
    ModernCard(
        modifier = modifier
            .aspectRatio(
                when (viewMode) {
                    CalendarViewMode.COMFORTABLE -> 0.5f
                    CalendarViewMode.COMPACT -> 1f
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        backgroundColor = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.05f)
            else -> MaterialTheme.colorScheme.surface
        },
        borderColor = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            schedule != null -> ShiftColorMapper.getColorForShift(schedule.shift.color).copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
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
                    style = dateTextStyle,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.secondary
                        date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> 
                            DesignTokens.BrandColors.Error
                        else -> MaterialTheme.colorScheme.onSurface
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