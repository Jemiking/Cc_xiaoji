package com.ccxiaoji.feature.schedule.presentation.demo

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 改用基础布局，避免额外依赖
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * 架构简化版日历日期单元格
 * 
 * 简化要点：
 * 1. 移除ModernCard，直接使用基础背景和边框
 * 2. 使用ConstraintLayout替代Box+Column的嵌套
 * 3. 让内容决定高度，而不是固定容器高度
 * 4. 减少样式计算复杂度
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimplifiedCalendarDayCell(
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
    // 简化的字体大小计算
    val computedDateFontSize = debugParams?.dateNumberTextSize ?: when (viewMode) {
        CalendarViewMode.COMFORTABLE -> 20.sp
        CalendarViewMode.COMPACT -> 14.sp
    }
    
    // 直接使用简单的lineHeight计算
    val directLineHeight = computedDateFontSize * 1.3f
    
    // 简化的背景色计算
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    // 简化的边框色
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        schedule != null -> ShiftColorMapper.getColorForShift(schedule.shift.color).copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
    }

    // 直接使用Column，避免ModernCard的复杂嵌套
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // 关键：让内容决定高度而不是固定高度
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .background(
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(1.dp) // 边框效果
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(7.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 6.dp), // 充足的内边距
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 日期数字 - 直接放置，不需要额外容器
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = computedDateFontSize,
                lineHeight = directLineHeight  // 简化的lineHeight计算
            ),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.secondary
                date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) ->
                    DesignTokens.BrandColors.Error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        // 班次标记 - 如果存在的话
        schedule?.let { sch ->
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = ShiftColorMapper.getBackgroundColorForShift(sch.shift.color, 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sch.shift.name.take(2),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp
                    ),
                    color = ShiftColorMapper.getColorForShift(sch.shift.color),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}