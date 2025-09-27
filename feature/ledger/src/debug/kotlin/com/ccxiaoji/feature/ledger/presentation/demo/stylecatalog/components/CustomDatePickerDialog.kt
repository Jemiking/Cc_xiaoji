package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// 颜色定义
private object DatePickerColors {
    val TopBackground = Color(0xFFF5F3FF)
    val SelectedDate = Color(0xFF3B82F6)
    val TodayBorder = Color(0xFF3B82F6)
    val TextPrimary = Color(0xFF111827)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val TextDisabled = Color(0xFFD1D5DB)
    val ButtonBorder = Color(0xFFE5E7EB)
    val Divider = Color(0xFFE5E7EB)
}

// 日历日期数据
private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)

@Composable
fun CustomDatePickerDialog(
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val today = LocalDate.now()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column {
                // 顶部淡紫色区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DatePickerColors.TopBackground)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "选择日期",
                            fontSize = 12.sp,
                            color = DatePickerColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "选定的日期",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = DatePickerColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = DatePickerColors.TextSecondary
                            )
                        }
                    }
                }

                // 白色主体区域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // 日期显示部分
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${selectedDate.year}年",
                            fontSize = 14.sp,
                            color = DatePickerColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日${getDayOfWeekChinese(selectedDate)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = DatePickerColors.TextPrimary
                        )
                    }

                    // 月份导航栏
                    MonthNavigator(
                        currentMonth = currentMonth,
                        onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                        onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                        onMonthYearClick = { /* TODO: 实现月份选择器 */ }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 星期标题行
                    WeekdayHeaders()

                    // 日历网格
                    CalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        today = today,
                        onDateSelected = { selectedDate = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 底部分割线
                Divider(
                    color = DatePickerColors.Divider,
                    thickness = 1.dp
                )

                // 底部按钮栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 快捷按钮
                    Row {
                        QuickDateButton("今") {
                            selectedDate = today
                            currentMonth = YearMonth.from(today)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        QuickDateButton("昨") {
                            val yesterday = today.minusDays(1)
                            selectedDate = yesterday
                            currentMonth = YearMonth.from(yesterday)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        QuickDateButton("前") {
                            val dayBeforeYesterday = today.minusDays(2)
                            selectedDate = dayBeforeYesterday
                            currentMonth = YearMonth.from(dayBeforeYesterday)
                        }
                    }

                    // 取消和确定按钮
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("取消", color = DatePickerColors.TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onDateSelected(selectedDate)
                                onDismiss()
                            }
                        ) {
                            Text("确定", color = DatePickerColors.SelectedDate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthYearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左箭头
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = DatePickerColors.TextPrimary
            )
        }

        // 年月选择器
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { onMonthYearClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentMonth.year}年${currentMonth.monthValue}月",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = DatePickerColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = DatePickerColors.TextSecondary
            )
        }

        // 右箭头
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = DatePickerColors.TextPrimary
            )
        }
    }
}

@Composable
private fun WeekdayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
        weekdays.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    fontSize = 12.sp,
                    color = DatePickerColors.TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val calendarDays = remember(currentMonth) {
        generateCalendarDays(currentMonth, selectedDate, today)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp), // 6 rows × 40dp
        userScrollEnabled = false
    ) {
        items(calendarDays) { day ->
            DayCell(
                day = day,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    onDateSelected: (LocalDate) -> Unit
) {
    val backgroundColor = when {
        day.isSelected -> DatePickerColors.SelectedDate
        else -> Color.Transparent
    }

    val textColor = when {
        !day.isCurrentMonth -> DatePickerColors.TextDisabled
        day.isSelected -> Color.White
        else -> DatePickerColors.TextPrimary
    }

    val borderModifier = if (day.isToday && !day.isSelected) {
        Modifier.border(
            width = 1.dp,
            color = DatePickerColors.TodayBorder,
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(
                enabled = day.isCurrentMonth,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (day.isCurrentMonth) {
                    onDateSelected(day.date)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 14.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickDateButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(width = 40.dp, height = 36.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, DatePickerColors.ButtonBorder),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = DatePickerColors.TextSecondary
        )
    ) {
        Text(text, fontSize = 14.sp)
    }
}

// 生成日历数据
private fun generateCalendarDays(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate
): List<CalendarDay> {
    val firstDay = yearMonth.atDay(1)
    val lastDay = yearMonth.atEndOfMonth()

    // 获取第一天是星期几（调整为周一=0）
    val firstDayOfWeek = (firstDay.dayOfWeek.value % 7).let {
        if (it == 0) 6 else it - 1
    }

    val days = mutableListOf<CalendarDay>()

    // 添加上月填充日
    if (firstDayOfWeek > 0) {
        val previousMonth = yearMonth.minusMonths(1)
        val previousMonthLastDay = previousMonth.atEndOfMonth().dayOfMonth
        for (i in firstDayOfWeek - 1 downTo 0) {
            days.add(CalendarDay(
                date = previousMonth.atDay(previousMonthLastDay - i),
                isCurrentMonth = false
            ))
        }
    }

    // 当月日期
    for (day in 1..lastDay.dayOfMonth) {
        val date = yearMonth.atDay(day)
        days.add(CalendarDay(
            date = date,
            isCurrentMonth = true,
            isToday = date == today,
            isSelected = date == selectedDate
        ))
    }

    // 下月填充日（确保总共42个格子）
    val remainingDays = 42 - days.size
    if (remainingDays > 0) {
        val nextMonth = yearMonth.plusMonths(1)
        for (day in 1..remainingDays) {
            days.add(CalendarDay(
                date = nextMonth.atDay(day),
                isCurrentMonth = false
            ))
        }
    }

    return days
}

// 获取中文星期
private fun getDayOfWeekChinese(date: LocalDate): String {
    return when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "周一"
        DayOfWeek.TUESDAY -> "周二"
        DayOfWeek.WEDNESDAY -> "周三"
        DayOfWeek.THURSDAY -> "周四"
        DayOfWeek.FRIDAY -> "周五"
        DayOfWeek.SATURDAY -> "周六"
        DayOfWeek.SUNDAY -> "周日"
    }
}