package com.ccxiaoji.feature.schedule.presentation.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarView
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.debug.DefaultDebugParams
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

/**
 * 扁平化排班 Demo 界面（无阴影版本）
 * - 去除 TopBar/Card/FAB 的阴影
 * - 使用细边框与留白替代层级阴影
 * - 放入示例排班数据
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatScheduleDemoScreen(
    onNavigateBack: () -> Unit = {}
) {
    // 示例数据：今日与明日的排班
    val today = LocalDate.now()
    val sampleShift = remember {
        Shift(
            id = 1L,
            name = "日班",
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(18, 0),
            color = 0xFF2196F3.toInt(),
            description = "演示用"
        )
    }
    val schedules = remember {
        listOf(
            Schedule(id = 1L, date = today, shift = sampleShift),
            Schedule(id = 2L, date = today.plusDays(1), shift = sampleShift)
        )
    }

    val yearMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    // 采用调试参数作为基础，并移除不必要的视觉重量
    val params = remember { DefaultDebugParams.default }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "扁平化排班 Demo", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // Demo：不使用 FAB，保持界面更“扁平”；如需可改为 0dp 阴影的 FAB
        snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 无阴影的统计卡片（用细边框替代）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Stat("工作日", "2天")
                    Stat("休息日", "0天")
                    Stat("总工时", "18小时")
                }
            }

            // 日历视图（扁平风格，细边框格子）
            CalendarView(
                yearMonth = yearMonth,
                selectedDate = selectedDate,
                schedules = schedules,
                weekStartDay = java.time.DayOfWeek.MONDAY,
                viewMode = CalendarViewMode.COMFORTABLE,
                debugParams = params.calendarView.copy(
                    cellSpacing = 0.dp,
                    rowHeight = params.calendarView.rowHeight,
                    cornerRadius = 10.dp,
                    dateNumberTextSize = params.calendarView.dateNumberTextSize
                ),
                onDateSelected = { selectedDate = it },
                onDateLongClick = {},
                onMonthNavigate = {},
                modifier = Modifier.padding(top = 8.dp)
            )

            // 下方信息卡片（无阴影）
            selectedDate?.let { date ->
                val selected = schedules.find { it.date == date }
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    // 复用模块现有的详情卡片内容
                    com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard(
                        date = date,
                        schedule = selected,
                        onEdit = { /* demo 中不跳转 */ },
                        onDelete = { /* demo 中不删除 */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
    }
}
