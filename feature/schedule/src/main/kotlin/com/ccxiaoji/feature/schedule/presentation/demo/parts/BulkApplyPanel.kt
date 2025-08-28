package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun BulkApplyPanel(
    data: DemoData,
    emphasizeNight: Boolean
) {
    var selectedDates by remember { mutableStateOf(setOf<LocalDate>()) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("多选日期后批量套用班次/清空（演示UI）", style = MaterialTheme.typography.bodyMedium)
        BulkCalendarGrid(
            yearMonth = data.yearMonth,
            schedules = data.schedules,
            selectedDates = selectedDates,
            onToggle = { date -> selectedDates = selectedDates.toggle(date) }
        )

        // 操作条
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* demo: 不落地，仅演示 */ },
                enabled = selectedDates.isNotEmpty()
            ) { Text("套用日班") }
            OutlinedButton(
                onClick = { /* demo: 不落地，仅演示 */ },
                enabled = selectedDates.isNotEmpty()
            ) { Text("清空") }
        }
    }
}

private fun <T> Set<T>.toggle(item: T): Set<T> =
    if (contains(item)) this - item else this + item

@Composable
private fun BulkCalendarGrid(
    yearMonth: java.time.YearMonth,
    schedules: List<com.ccxiaoji.feature.schedule.domain.model.Schedule>,
    selectedDates: Set<LocalDate>,
    onToggle: (LocalDate) -> Unit
) {
    val days = yearMonth.lengthOfMonth()
    val first = yearMonth.atDay(1)
    val offset = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val cells = remember(yearMonth) {
        val list = mutableListOf<LocalDate?>()
        repeat(offset) { list.add(null) }
        for (d in 1..days) list.add(yearMonth.atDay(d))
        val pad = (7 - (list.size % 7)) % 7
        repeat(pad) { list.add(null) }
        list.toList()
    }
    val map = remember(schedules) { schedules.associateBy { it.date } }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        items(cells) { date ->
            if (date == null) {
                Spacer(Modifier.height(56.dp))
            } else {
                val sch = map[date]
                val selected = selectedDates.contains(date)
                Surface(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                        .clickable { onToggle(date) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Text(text = date.dayOfMonth.toString(), modifier = Modifier.align(Alignment.TopStart).padding(6.dp), style = MaterialTheme.typography.titleSmall)
                        if (sch != null) Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                                .background(
                                    color = ShiftColorMapper.getBackgroundColorForShift(sch.shift.color, 0.1f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(sch.shift.name.take(2), color = ShiftColorMapper.getColorForShift(sch.shift.color))
                        }
                    }
                }
            }
        }
    }
}

