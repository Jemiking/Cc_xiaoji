package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.presentation.calendar.components.SelectedDateDetailCard
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle
import java.time.LocalDate

@Composable
fun SplitPanel(
    data: DemoData,
    style: IndicatorStyle,
    emphasizeNight: Boolean
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val scheduleForSelected = remember(selectedDate, data) { data.schedules.find { it.date == selectedDate } }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text("月历", style = MaterialTheme.typography.titleMedium)
            MonthCalendarPanel(
                data = data,
                style = style,
                emphasizeNight = emphasizeNight
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("当日详情", style = MaterialTheme.typography.titleMedium)
            SelectedDateDetailCard(
                date = selectedDate,
                schedule = scheduleForSelected,
                onEdit = {},
                onDelete = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

