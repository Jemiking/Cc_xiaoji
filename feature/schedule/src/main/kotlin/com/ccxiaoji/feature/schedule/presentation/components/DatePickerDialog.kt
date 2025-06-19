package com.ccxiaoji.feature.schedule.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarView
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.time.LocalTime

/**
 * 日期选择器对话框
 * @param showDialog 是否显示对话框
 * @param initialDate 初始日期
 * @param onDateSelected 日期选择回调
 * @param onDismiss 关闭对话框回调
 * @param dateValidator 日期验证器，返回false的日期将被禁用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    dateValidator: ((LocalDate) -> Boolean)? = null
) {
    if (showDialog) {
        // 获取初始日期的毫秒数
        val initialDateMillis = initialDate?.let { date ->
            date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
        
        // 创建日期选择器状态
        val datePickerState = if (dateValidator != null) {
            rememberDatePickerState(
                initialSelectedDateMillis = initialDateMillis,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = java.time.Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        return dateValidator(date)
                    }
                }
            )
        } else {
            rememberDatePickerState(
                initialSelectedDateMillis = initialDateMillis
            )
        }
        
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(selectedDate)
                        }
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier
            )
        }
    }
}

/**
 * 创建日期范围选择器对话框
 * @param showDialog 是否显示对话框
 * @param initialStartDate 初始开始日期
 * @param initialEndDate 初始结束日期
 * @param onDateRangeSelected 日期范围选择回调
 * @param onDismiss 关闭对话框回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    showDialog: Boolean,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        var isSelectingStartDate by remember { mutableStateOf(true) }
        var startDate by remember { mutableStateOf(initialStartDate) }
        var endDate by remember { mutableStateOf(initialEndDate) }
        
        // 根据当前选择状态创建日期选择器
        val currentDate = if (isSelectingStartDate) startDate else endDate
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentDate?.let { date ->
                date.atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        )
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(if (isSelectingStartDate) stringResource(R.string.schedule_dialog_select_start_date) else stringResource(R.string.schedule_dialog_select_end_date))
            },
            text = {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            
                            if (isSelectingStartDate) {
                                startDate = selectedDate
                                isSelectingStartDate = false
                            } else {
                                endDate = selectedDate
                                if (startDate != null && endDate != null) {
                                    // 确保开始日期不晚于结束日期
                                    if (startDate!! <= endDate!!) {
                                        onDateRangeSelected(startDate!!, endDate!!)
                                        onDismiss()
                                    } else {
                                        // 交换日期
                                        onDateRangeSelected(endDate!!, startDate!!)
                                        onDismiss()
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSelectingStartDate) stringResource(R.string.schedule_dialog_next_step) else stringResource(R.string.schedule_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        )
    }
}

/**
 * 基于 CalendarView 的自定义日期选择器对话框（Material You 风格）
 * 使用项目自定义的 CalendarView，星期显示为单字（"日"、"一"等）
 * @param showDialog 是否显示对话框
 * @param initialDate 初始日期
 * @param onDateSelected 日期选择回调
 * @param onDismiss 关闭对话框回调
 * @param dateValidator 日期验证器，返回false的日期将被禁用（暂未实现）
 * @param weekStartDay 一周的开始日期，默认为周一
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    dateValidator: ((LocalDate) -> Boolean)? = null,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY
) {
    if (showDialog) {
        var currentYearMonth by remember { 
            mutableStateOf(
                initialDate?.let { YearMonth.from(it) } ?: YearMonth.now()
            )
        }
        var selectedDate by remember { mutableStateOf(initialDate) }

        Dialog(
            onDismissRequest = onDismiss,
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            // Material You 风格卡片，使用大圆角和柔和阴影
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge, // 使用大圆角
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 标题区域 - Material You 风格
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 上一月按钮 - Tonal 风格
                            FilledTonalIconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = stringResource(R.string.schedule_dialog_previous_month)
                                )
                            }
                            
                            // 年月显示 - 层次分明
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${currentYearMonth.year}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = stringResource(R.string.schedule_format_month_only, currentYearMonth.monthValue),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            // 下一月按钮 - Tonal 风格
                            FilledTonalIconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.schedule_dialog_next_month)
                                )
                            }
                        }
                    }
                    
                    // 日历主体 - 带 Surface Tint
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        // 使用自定义的日期选择器日历视图
                        PickerCalendarView(
                            yearMonth = currentYearMonth,
                            selectedDate = selectedDate,
                            onDateSelected = { date ->
                                if (dateValidator == null || dateValidator(date)) {
                                    selectedDate = date
                                }
                            },
                            weekStartDay = weekStartDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                    
                    // 操作按钮区 - Material You 风格
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 今天按钮 - Tonal 风格
                        FilledTonalButton(
                            onClick = {
                                val today = LocalDate.now()
                                currentYearMonth = YearMonth.from(today)
                                selectedDate = today
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.schedule_calendar_today))
                        }
                        
                        // 操作按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 取消按钮 - Text 风格
                            TextButton(
                                onClick = onDismiss,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_cancel))
                            }
                            
                            // 确定按钮 - Filled 风格
                            Button(
                                onClick = {
                                    selectedDate?.let {
                                        onDateSelected(it)
                                        onDismiss()
                                    }
                                },
                                enabled = selectedDate != null,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 使用自定义日期选择器的包装函数
 * 可以通过这个函数统一替换应用中的所有日期选择器
 * 如果要切换回原生 DatePickerDialog，只需修改这个函数的实现
 */
@Composable
fun AppDatePickerDialog(
    showDialog: Boolean,
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    dateValidator: ((LocalDate) -> Boolean)? = null,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY
) {
    // 使用自定义日期选择器，星期显示为单字
    CustomDatePickerDialog(
        showDialog = showDialog,
        initialDate = initialDate,
        onDateSelected = onDateSelected,
        onDismiss = onDismiss,
        dateValidator = dateValidator,
        weekStartDay = weekStartDay
    )
    
    // 如果要切换回原生日期选择器，取消注释下面的代码
    // DatePickerDialog(
    //     showDialog = showDialog,
    //     initialDate = initialDate,
    //     onDateSelected = onDateSelected,
    //     onDismiss = onDismiss,
    //     dateValidator = dateValidator
    // )
}

/**
 * 专为日期选择器优化的日历视图
 * 具有固定高度的格子和更大的点击区域
 */
@Composable
private fun PickerCalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    modifier: Modifier = Modifier
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    
    // 计算第一天相对于一周起始日的偏移量
    val firstDayOffset = when (weekStartDay) {
        DayOfWeek.SUNDAY -> firstDayOfMonth.dayOfWeek.value % 7
        DayOfWeek.MONDAY -> (firstDayOfMonth.dayOfWeek.value - 1) % 7
        else -> (firstDayOfMonth.dayOfWeek.value - 1) % 7
    }
    
    // 创建日历网格数据
    val calendarDays = remember(yearMonth, weekStartDay) {
        val days = mutableListOf<LocalDate?>()
        // 添加月初的空白天数
        repeat(firstDayOffset) {
            days.add(null)
        }
        // 添加当月所有天数
        for (day in 1..daysInMonth) {
            days.add(yearMonth.atDay(day))
        }
        days
    }
    
    Column(modifier = modifier) {
        // 星期标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDays = when (weekStartDay) {
                DayOfWeek.SUNDAY -> listOf(stringResource(R.string.schedule_weekday_short_sunday), stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday))
                DayOfWeek.MONDAY -> listOf(stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday), stringResource(R.string.schedule_weekday_short_sunday))
                else -> listOf(stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday), stringResource(R.string.schedule_weekday_short_sunday))
            }
            
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (day) {
                        stringResource(R.string.schedule_weekday_short_sunday), stringResource(R.string.schedule_weekday_short_saturday) -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // 日历网格 - 固定高度格子
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(calendarDays) { date ->
                if (date != null) {
                    PickerDateCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == LocalDate.now(),
                        onClick = { onDateSelected(date) }
                    )
                } else {
                    // 空白格子
                    Box(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

/**
 * 日期选择器专用的日期单元格
 * 固定高度 48dp，优化的点击区域
 */
@Composable
private fun PickerDateCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp) // 固定高度
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp, // 更大的字体
            fontWeight = when {
                isSelected || isToday -> FontWeight.Bold
                else -> FontWeight.Normal
            },
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> 
                    MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * 基于 CustomDatePickerDialog 的日期范围选择器（Material You 风格）
 * 采用两步选择方式：先选择开始日期，再选择结束日期
 * @param showDialog 是否显示对话框
 * @param initialStartDate 初始开始日期
 * @param initialEndDate 初始结束日期
 * @param onDateRangeSelected 日期范围选择回调
 * @param onDismiss 关闭对话框回调
 * @param weekStartDay 一周的开始日期，默认为周一
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangePickerDialog(
    showDialog: Boolean,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY
) {
    if (showDialog) {
        var isSelectingStartDate by remember { mutableStateOf(true) }
        var startDate by remember(initialStartDate) { mutableStateOf(initialStartDate) }
        var endDate by remember(initialEndDate) { mutableStateOf(initialEndDate) }
        
        // 当前选择的年月
        var currentYearMonth by remember { 
            mutableStateOf(
                if (isSelectingStartDate) {
                    startDate?.let { YearMonth.from(it) } ?: YearMonth.now()
                } else {
                    endDate?.let { YearMonth.from(it) } ?: startDate?.let { YearMonth.from(it) } ?: YearMonth.now()
                }
            )
        }
        
        // 当前选中的日期
        var selectedDate by remember { 
            mutableStateOf(if (isSelectingStartDate) startDate else endDate) 
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            // Material You 风格卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 步骤指示器和标题
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 步骤指示器
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 步骤1
                            StepIndicator(
                                step = 1,
                                label = stringResource(R.string.schedule_dialog_start_date_label),
                                isActive = isSelectingStartDate,
                                isCompleted = startDate != null && !isSelectingStartDate
                            )
                            
                            // 连接线
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .height(2.dp)
                                    .background(
                                        if (startDate != null && !isSelectingStartDate)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                            
                            // 步骤2
                            StepIndicator(
                                step = 2,
                                label = stringResource(R.string.schedule_dialog_end_date_label),
                                isActive = !isSelectingStartDate,
                                isCompleted = false
                            )
                        }
                        
                        // 已选择的日期显示
                        if (!isSelectingStartDate && startDate != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.schedule_dialog_start_prefix) + startDate!!.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_format_date_pattern_year_month_day))),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    TextButton(
                                        onClick = {
                                            isSelectingStartDate = true
                                            currentYearMonth = startDate?.let { YearMonth.from(it) } ?: YearMonth.now()
                                            selectedDate = startDate
                                        }
                                    ) {
                                        Text(stringResource(R.string.schedule_dialog_modify))
                                    }
                                }
                            }
                        }
                    }
                    
                    // 标题区域 - Material You 风格
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 上一月按钮
                            FilledTonalIconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = stringResource(R.string.schedule_dialog_previous_month)
                                )
                            }
                            
                            // 年月显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${currentYearMonth.year}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = stringResource(R.string.schedule_format_month_only, currentYearMonth.monthValue),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            // 下一月按钮
                            FilledTonalIconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.schedule_dialog_next_month)
                                )
                            }
                        }
                    }
                    
                    // 日历主体
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        // 使用范围选择器日历视图
                        RangePickerCalendarView(
                            yearMonth = currentYearMonth,
                            selectedDate = selectedDate,
                            startDate = startDate,
                            endDate = if (!isSelectingStartDate) endDate else null,
                            isSelectingStartDate = isSelectingStartDate,
                            onDateSelected = { date ->
                                // 验证日期有效性
                                if (!isSelectingStartDate && startDate != null) {
                                    // 选择结束日期时，不能早于开始日期
                                    if (date >= startDate!!) {
                                        selectedDate = date
                                    }
                                } else {
                                    selectedDate = date
                                }
                            },
                            weekStartDay = weekStartDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                    
                    // 操作按钮区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 今天按钮
                        FilledTonalButton(
                            onClick = {
                                val today = LocalDate.now()
                                currentYearMonth = YearMonth.from(today)
                                selectedDate = today
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.schedule_calendar_today))
                        }
                        
                        // 操作按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 取消按钮
                            TextButton(
                                onClick = onDismiss,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_cancel))
                            }
                            
                            // 下一步/确定按钮
                            Button(
                                onClick = {
                                    selectedDate?.let { date ->
                                        if (isSelectingStartDate) {
                                            // 保存开始日期，进入第二步
                                            startDate = date
                                            isSelectingStartDate = false
                                            // 重置选中日期为之前的结束日期或开始日期
                                            selectedDate = endDate ?: date
                                            currentYearMonth = endDate?.let { YearMonth.from(it) } 
                                                ?: YearMonth.from(date)
                                        } else {
                                            // 保存结束日期并返回结果
                                            endDate = date
                                            if (startDate != null) {
                                                onDateRangeSelected(startDate!!, endDate!!)
                                                onDismiss()
                                            }
                                        }
                                    }
                                },
                                enabled = selectedDate != null,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(if (isSelectingStartDate) stringResource(R.string.schedule_dialog_next_step) else stringResource(R.string.schedule_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 步骤指示器组件
 */
@Composable
private fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 圆形指示器
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    when {
                        isActive -> MaterialTheme.colorScheme.primary
                        isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 标签
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 日期范围选择器专用的日历视图
 * 支持显示选择范围高亮
 */
@Composable
private fun RangePickerCalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    isSelectingStartDate: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    modifier: Modifier = Modifier
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    
    // 计算第一天相对于一周起始日的偏移量
    val firstDayOffset = when (weekStartDay) {
        DayOfWeek.SUNDAY -> firstDayOfMonth.dayOfWeek.value % 7
        DayOfWeek.MONDAY -> (firstDayOfMonth.dayOfWeek.value - 1) % 7
        else -> (firstDayOfMonth.dayOfWeek.value - 1) % 7
    }
    
    // 创建日历网格数据
    val calendarDays = remember(yearMonth, weekStartDay) {
        val days = mutableListOf<LocalDate?>()
        // 添加月初的空白天数
        repeat(firstDayOffset) {
            days.add(null)
        }
        // 添加当月所有天数
        for (day in 1..daysInMonth) {
            days.add(yearMonth.atDay(day))
        }
        days
    }
    
    Column(modifier = modifier) {
        // 星期标题行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val weekDays = when (weekStartDay) {
                DayOfWeek.SUNDAY -> listOf(stringResource(R.string.schedule_weekday_short_sunday), stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday))
                DayOfWeek.MONDAY -> listOf(stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday), stringResource(R.string.schedule_weekday_short_sunday))
                else -> listOf(stringResource(R.string.schedule_weekday_short_monday), stringResource(R.string.schedule_weekday_short_tuesday), stringResource(R.string.schedule_weekday_short_wednesday), stringResource(R.string.schedule_weekday_short_thursday), stringResource(R.string.schedule_weekday_short_friday), stringResource(R.string.schedule_weekday_short_saturday), stringResource(R.string.schedule_weekday_short_sunday))
            }
            
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (day) {
                        stringResource(R.string.schedule_weekday_short_sunday), stringResource(R.string.schedule_weekday_short_saturday) -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // 日历网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(calendarDays) { date ->
                if (date != null) {
                    RangeDateCell(
                        date = date,
                        isSelected = date == selectedDate,
                        isStartDate = date == startDate,
                        isEndDate = date == endDate,
                        isInRange = startDate != null && endDate != null && 
                                   date > startDate!! && date < endDate!!,
                        isToday = date == LocalDate.now(),
                        isDisabled = !isSelectingStartDate && startDate != null && date < startDate!!,
                        onClick = { 
                            if (isSelectingStartDate || (startDate != null && date >= startDate!!)) {
                                onDateSelected(date)
                            }
                        }
                    )
                } else {
                    // 空白格子
                    Box(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

/**
 * 日期范围选择器的日期单元格
 */
@Composable
private fun RangeDateCell(
    date: LocalDate,
    isSelected: Boolean,
    isStartDate: Boolean,
    isEndDate: Boolean,
    isInRange: Boolean,
    isToday: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(
                when {
                    isStartDate || isEndDate -> MaterialTheme.colorScheme.primary
                    isInRange -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    isSelected && !isStartDate && !isEndDate -> 
                        MaterialTheme.colorScheme.secondaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = !isDisabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            fontWeight = when {
                isStartDate || isEndDate || isSelected -> FontWeight.Bold
                isToday -> FontWeight.Medium
                else -> FontWeight.Normal
            },
            color = when {
                isDisabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                isStartDate || isEndDate -> MaterialTheme.colorScheme.onPrimary
                isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
                isToday -> MaterialTheme.colorScheme.primary
                date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> 
                    MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        
        // 开始/结束标记
        if (isStartDate || isEndDate) {
            Text(
                text = if (isStartDate) stringResource(R.string.schedule_dialog_date_start_mark) else stringResource(R.string.schedule_dialog_date_end_mark),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
            )
        }
    }
}

/**
 * 基于网格布局的年月选择器（Material You 风格）
 * @param showDialog 是否显示对话框
 * @param currentYearMonth 当前年月
 * @param onYearMonthSelected 年月选择回调
 * @param onDismiss 关闭对话框回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomYearMonthPickerDialog(
    showDialog: Boolean,
    currentYearMonth: YearMonth,
    onYearMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        var selectedYear by remember(currentYearMonth) { mutableIntStateOf(currentYearMonth.year) }
        var selectedMonth by remember(currentYearMonth) { mutableIntStateOf(currentYearMonth.monthValue) }
        
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            // Material You 风格卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 标题
                    Text(
                        text = stringResource(R.string.schedule_dialog_select_year_month),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    
                    // 年份选择区域
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 上一年按钮
                            FilledTonalIconButton(
                                onClick = { selectedYear-- },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = stringResource(R.string.schedule_dialog_previous_year)
                                )
                            }
                            
                            // 年份显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.schedule_dialog_year),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = stringResource(R.string.schedule_format_year_only, selectedYear),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            
                            // 下一年按钮
                            FilledTonalIconButton(
                                onClick = { selectedYear++ },
                                modifier = Modifier.size(40.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.schedule_dialog_next_year)
                                )
                            }
                        }
                    }
                    
                    // 月份网格选择区域
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.schedule_dialog_month),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            
                            // 月份网格 - 3行4列
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(12) { index ->
                                    val month = index + 1
                                    MonthGridItem(
                                        month = month,
                                        isSelected = month == selectedMonth,
                                        onClick = { selectedMonth = month }
                                    )
                                }
                            }
                        }
                    }
                    
                    // 操作按钮区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 今天按钮
                        FilledTonalButton(
                            onClick = {
                                val today = YearMonth.now()
                                selectedYear = today.year
                                selectedMonth = today.monthValue
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.schedule_calendar_today))
                        }
                        
                        // 操作按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 取消按钮
                            TextButton(
                                onClick = onDismiss,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_cancel))
                            }
                            
                            // 确定按钮
                            Button(
                                onClick = {
                                    val yearMonth = YearMonth.of(selectedYear, selectedMonth)
                                    onYearMonthSelected(yearMonth)
                                    onDismiss()
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 月份网格项
 */
@Composable
private fun MonthGridItem(
    month: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val monthNames = listOf(
        stringResource(R.string.schedule_month_january), stringResource(R.string.schedule_month_february), stringResource(R.string.schedule_month_march), stringResource(R.string.schedule_month_april),
        stringResource(R.string.schedule_month_may), stringResource(R.string.schedule_month_june), stringResource(R.string.schedule_month_july), stringResource(R.string.schedule_month_august),
        stringResource(R.string.schedule_month_september), stringResource(R.string.schedule_month_october), stringResource(R.string.schedule_month_november), stringResource(R.string.schedule_month_december)
    )
    
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = month.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = monthNames[month - 1],
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 基于 Material3 时钟的时间选择器（Material You 风格）
 * @param showDialog 是否显示对话框
 * @param initialTime 初始时间
 * @param onTimeSelected 时间选择回调
 * @param onDismiss 关闭对话框回调
 * @param is24Hour 是否使用24小时制，默认为true
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    showDialog: Boolean,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    is24Hour: Boolean = true
) {
    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = is24Hour
        )
        
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            // Material You 风格卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = stringResource(R.string.schedule_dialog_select_time),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // 当前选择的时间显示
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 小时显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%02d", timePickerState.hour),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.schedule_dialog_hour),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            // 分钟显示
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = String.format("%02d", timePickerState.minute),
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = stringResource(R.string.schedule_dialog_minute),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    
                    // Material3 时间选择器
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = MaterialTheme.colorScheme.surface,
                                    clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    selectorColor = MaterialTheme.colorScheme.primary,
                                    containerColor = Color.Transparent,
                                    periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
                                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    periodSelectorUnselectedContainerColor = Color.Transparent,
                                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                    
                    // 操作按钮区
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 当前时间按钮
                        FilledTonalButton(
                            onClick = {
                                val now = LocalTime.now()
                                // 由于 timePickerState 的属性是只读的，
                                // 我们需要重新创建对话框来更新时间
                                onTimeSelected(now)
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.schedule_dialog_current_time))
                        }
                        
                        // 操作按钮组
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 取消按钮
                            TextButton(
                                onClick = onDismiss,
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_cancel))
                            }
                            
                            // 确定按钮
                            Button(
                                onClick = {
                                    onTimeSelected(
                                        LocalTime.of(timePickerState.hour, timePickerState.minute)
                                    )
                                    onDismiss()
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.schedule_confirm))
                            }
                        }
                    }
                }
            }
        }
    }
}