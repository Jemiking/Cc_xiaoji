package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun AgendaPanel(
    data: DemoData,
    style: IndicatorStyle,
    emphasizeNight: Boolean
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val weekFields = WeekFields.of(Locale.getDefault())
    var currentWeek by remember(selectedDate) { mutableStateOf(selectedDate.get(weekFields.weekOfWeekBasedYear())) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 周条：简单 7 天条，指示所选日
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            val startOfWeek = selectedDate.with(weekFields.dayOfWeek(), 1)
            (0..6).map { startOfWeek.plusDays(it.toLong()) }.forEach { day ->
                val schedule = data.schedules.find { it.date == day }
                WeekDayChip(day, schedule, day == selectedDate, style, emphasizeNight) {
                    selectedDate = day
                    currentWeek = day.get(weekFields.weekOfWeekBasedYear())
                }
            }
        }

        // Agenda 列表：当周或全月内有排班的天
        val items = remember(data, currentWeek) {
            data.schedules.sortedBy { it.date }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { sch ->
                if (sch.date.get(weekFields.weekOfWeekBasedYear()) == currentWeek) {
                    SelectedDateDetailCard(
                        date = sch.date,
                        schedule = sch,
                        onEdit = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekDayChip(
    date: LocalDate,
    schedule: Schedule?,
    selected: Boolean,
    style: IndicatorStyle,
    emphasizeNight: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(44.dp)
            .height(56.dp)
            .clickable { onClick() }
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
            when (style) {
                IndicatorStyle.Dot -> if (schedule != null) Box(
                    modifier = Modifier
                        .size(6.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = if (emphasizeNight && schedule.shift.isNight()) Color(0xFF7C4DFF) else ShiftColorMapper.getColorForShift(schedule.shift.color),
                            shape = RoundedCornerShape(50)
                        )
                )
                IndicatorStyle.Bar -> if (schedule != null) Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            color = if (emphasizeNight && schedule.shift.isNight()) Color(0xFF7C4DFF) else ShiftColorMapper.getColorForShift(schedule.shift.color).copy(alpha = 0.6f)
                        )
                )
                IndicatorStyle.Label -> if (schedule != null) Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(
                            color = ShiftColorMapper.getBackgroundColorForShift(schedule.shift.color, 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (emphasizeNight && schedule.shift.isNight()) "夜" else schedule.shift.name.take(2),
                        fontSize = 10.sp,
                        color = if (emphasizeNight && schedule.shift.isNight()) Color(0xFF7C4DFF) else ShiftColorMapper.getColorForShift(schedule.shift.color)
                    )
                }
            }
        }
    }
}

private fun com.ccxiaoji.feature.schedule.domain.model.Shift.isNight(): Boolean {
    val s = startTime
    val e = endTime
    return if (s != null && e != null) e.isBefore(s) || s.hour >= 21 || e.hour <= 6 else false
}

