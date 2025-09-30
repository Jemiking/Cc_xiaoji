package com.ccxiaoji.feature.ledger.presentation.demo.stylecatalog.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
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
    val Primary = Color(0xFF111827)      // 主文字（深黑）
    val Secondary = Color(0xFF6B7280)    // 次要文字（灰）
    val SelectedBg = Color(0xFF3B82F6)   // 选中背景（蓝）
    val Border = Color(0xFFE5E7EB)       // 边框（淡灰）
    val Disabled = Color(0xFFE5E7EB)     // 禁用（极淡灰）
}

// 日历日期数据
private data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val isEnabled: Boolean = true  // 新增：是否可选
)

@Composable
fun CustomDatePickerDialog(
    initialDate: LocalDate? = null,
    minDate: LocalDate? = null,  // 新增：最小可选日期
    maxDate: LocalDate? = null,  // 新增：最大可选日期
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var showYearMonthPicker by remember { mutableStateOf(false) }  // 新增：年月选择器状态
    val today = LocalDate.now()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 内容区域（带有水平内边距）
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // 顶部日期显示（左对齐，右侧日历图标）
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedDate.year}年",
                                fontSize = 12.sp,
                                color = DatePickerColors.Secondary,
                                fontWeight = FontWeight.Normal
                            )
                            Text(
                                text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日${getDayOfWeekChinese(selectedDate)}",
                                fontSize = 17.sp,
                                color = DatePickerColors.Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = DatePickerColors.Secondary
                        )
                    }

                    // 月份导航栏
                    MonthNavigator(
                        currentMonth = currentMonth,
                        onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                        onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                        onMonthYearClick = { showYearMonthPicker = true }  // 点击显示年月选择器
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 星期标题行
                    WeekdayHeaders()

                    // 可滑动的日历网格
                    SwipeableCalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        today = today,
                        minDate = minDate,
                        maxDate = maxDate,
                        onDateSelected = { selectedDate = it },
                        onMonthChanged = { currentMonth = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 底部按钮栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 快捷按钮（可横向滚动）
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 今天
                        item {
                            QuickDateButton(
                                text = "今",
                                enabled = isDateInRange(today, minDate, maxDate),
                                onClick = {
                                    selectedDate = today
                                    currentMonth = YearMonth.from(today)
                                }
                            )
                        }

                        // 昨天
                        item {
                            val yesterday = today.minusDays(1)
                            QuickDateButton(
                                text = "昨",
                                enabled = isDateInRange(yesterday, minDate, maxDate),
                                onClick = {
                                    selectedDate = yesterday
                                    currentMonth = YearMonth.from(yesterday)
                                }
                            )
                        }

                        // 前天
                        item {
                            val dayBeforeYesterday = today.minusDays(2)
                            QuickDateButton(
                                text = "前",
                                enabled = isDateInRange(dayBeforeYesterday, minDate, maxDate),
                                onClick = {
                                    selectedDate = dayBeforeYesterday
                                    currentMonth = YearMonth.from(dayBeforeYesterday)
                                }
                            )
                        }
                    }

                    // 取消和确定按钮
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("取消", color = DatePickerColors.Secondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onDateSelected(selectedDate)
                                onDismiss()
                            }
                        ) {
                            Text("确定", color = DatePickerColors.SelectedBg)
                        }
                    }
                }
            }
        }
    }

    // 年月选择弹窗
    if (showYearMonthPicker) {
        YearMonthPicker(
            currentYearMonth = currentMonth,
            onYearMonthSelected = { yearMonth ->
                currentMonth = yearMonth
                showYearMonthPicker = false
            },
            onDismiss = { showYearMonthPicker = false }
        )
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
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左箭头
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                modifier = Modifier.size(24.dp),
                tint = DatePickerColors.Primary
            )
        }

        // 年月显示（居中，可点击）
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { onMonthYearClick() }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentMonth.year}年${currentMonth.monthValue}月",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = DatePickerColors.Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = DatePickerColors.Secondary
            )
        }

        // 右箭头
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                modifier = Modifier.size(24.dp),
                tint = DatePickerColors.Primary
            )
        }
    }
}

@Composable
private fun WeekdayHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
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
                    fontSize = 11.sp,
                    color = DatePickerColors.Secondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun SwipeableCalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit
) {
    // 使用今天作为基准月份
    val baseMonth = remember { YearMonth.from(LocalDate.now()) }
    val initialPage = 1000

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 2000 }
    )

    // 计算当前月份对应的页面索引
    val currentPageIndex = remember(currentMonth) {
        val monthDiff = currentMonth.year * 12 + currentMonth.monthValue -
                (baseMonth.year * 12 + baseMonth.monthValue)
        initialPage + monthDiff
    }

    // 监听页面滑动，更新月份
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val monthOffset = pagerState.currentPage - initialPage
            val newMonth = baseMonth.plusMonths(monthOffset.toLong())
            if (newMonth != currentMonth) {
                onMonthChanged(newMonth)
            }
        }
    }

    // 当外部改变月份时（箭头或年月选择器），同步pager
    LaunchedEffect(currentPageIndex) {
        if (pagerState.currentPage != currentPageIndex && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(currentPageIndex)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val monthOffset = page - initialPage
        val displayMonth = baseMonth.plusMonths(monthOffset.toLong())

        CalendarGrid(
            currentMonth = displayMonth,
            selectedDate = selectedDate,
            today = today,
            minDate = minDate,
            maxDate = maxDate,
            onDateSelected = onDateSelected
        )
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val calendarDays = remember(currentMonth, selectedDate) {
        generateCalendarDays(currentMonth, selectedDate, today, minDate, maxDate)
    }

    // 动态计算周数
    val weekCount = remember(calendarDays) {
        (calendarDays.size + 6) / 7  // 向上取整
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height((weekCount * 36).dp), // 动态高度：周数 × 36dp
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
        day.isSelected -> DatePickerColors.SelectedBg
        else -> Color.Transparent
    }

    val textColor = when {
        !day.isEnabled -> DatePickerColors.Disabled  // 禁用状态
        !day.isCurrentMonth -> DatePickerColors.Disabled
        day.isSelected -> Color.White
        else -> DatePickerColors.Primary
    }

    val borderModifier = if (day.isToday && !day.isSelected) {
        Modifier.border(
            width = 1.dp,
            color = DatePickerColors.SelectedBg,
            shape = CircleShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(borderModifier)
            .background(backgroundColor)
            .clickable(
                enabled = day.isCurrentMonth && day.isEnabled,  // 检查是否可选
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (day.isCurrentMonth && day.isEnabled) {
                    onDateSelected(day.date)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            fontSize = 13.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun QuickDateButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(width = 38.dp, height = 34.dp),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(
            1.dp,
            if (enabled) DatePickerColors.Border else DatePickerColors.Disabled
        ),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = if (enabled) DatePickerColors.Secondary else DatePickerColors.Disabled,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = DatePickerColors.Disabled
        )
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (enabled) DatePickerColors.Secondary else DatePickerColors.Disabled
        )
    }
}

// 生成日历数据
private fun generateCalendarDays(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
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
            val date = previousMonth.atDay(previousMonthLastDay - i)
            days.add(CalendarDay(
                date = date,
                isCurrentMonth = false,
                isEnabled = isDateInRange(date, minDate, maxDate)
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
            isSelected = date == selectedDate,
            isEnabled = isDateInRange(date, minDate, maxDate)
        ))
    }

    // 下月填充日（只填充到当周周日）
    val lastDayOfWeek = (lastDay.dayOfWeek.value % 7).let {
        if (it == 0) 6 else it - 1  // 周一=0, 周二=1...周日=6
    }

    // 如果最后一天不是周日（值不是6），填充到周日
    if (lastDayOfWeek != 6) {
        val daysToFill = 6 - lastDayOfWeek
        val nextMonth = yearMonth.plusMonths(1)
        for (day in 1..daysToFill) {
            val date = nextMonth.atDay(day)
            days.add(CalendarDay(
                date = date,
                isCurrentMonth = false,
                isEnabled = isDateInRange(date, minDate, maxDate)
            ))
        }
    }

    return days
}

// 判断日期是否在范围内
private fun isDateInRange(
    date: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?
): Boolean {
    return (minDate == null || date >= minDate) &&
           (maxDate == null || date <= maxDate)
}

// 年月选择器组件
@Composable
private fun YearMonthPicker(
    currentYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentYearMonth.year) }
    var selectedMonth by remember { mutableStateOf(currentYearMonth.monthValue) }
    val currentYear = LocalDate.now().year
    val yearRange = (currentYear - 50)..(currentYear + 50)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 标题
                Text(
                    text = "选择年月",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = DatePickerColors.Primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 年份选择（横向滚动）
                Text(
                    text = "年份",
                    fontSize = 12.sp,
                    color = DatePickerColors.Secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(yearRange.count()) { index ->
                        val year = yearRange.first + index
                        val isSelected = year == selectedYear

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) DatePickerColors.SelectedBg
                                    else Color.Transparent
                                )
                                .clickable { selectedYear = year }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$year",
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else DatePickerColors.Primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份选择（网格）
                Text(
                    text = "月份",
                    fontSize = 12.sp,
                    color = DatePickerColors.Secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { index ->
                        val month = index + 1
                        val isSelected = month == selectedMonth

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) DatePickerColors.SelectedBg
                                    else Color(0xFFF3F4F6)
                                )
                                .clickable { selectedMonth = month },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${month}月",
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else DatePickerColors.Primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = DatePickerColors.Secondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onYearMonthSelected(YearMonth.of(selectedYear, selectedMonth))
                        }
                    ) {
                        Text("确定", color = DatePickerColors.SelectedBg)
                    }
                }
            }
        }
    }
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