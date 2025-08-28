package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.viewmodel.CalendarViewMode
import com.ccxiaoji.feature.schedule.presentation.calendar.CalendarView
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickEntryPanel(
    data: DemoData,
    emphasizeNight: Boolean
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
    var showSheet by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("长按日期或使用按钮打开快捷录入")
            Button(onClick = { showSheet = true }) { Text("打开快捷录入") }
        }

        CalendarView(
            yearMonth = data.yearMonth,
            selectedDate = selectedDate,
            schedules = data.schedules,
            weekStartDay = DayOfWeek.MONDAY,
            viewMode = CalendarViewMode.COMFORTABLE,
            onDateSelected = { selectedDate = it },
            onDateLongClick = { showSheet = true },
            onMonthNavigate = {}
        )
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("快速设置班次", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    data.shifts.forEach { shift ->
                        AssistChip(
                            onClick = { /* demo 中不落地保存，仅演示UI */ },
                            label = { Text(shift.name) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showSheet = false }, modifier = Modifier.align(Alignment.End)) { Text("完成") }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
