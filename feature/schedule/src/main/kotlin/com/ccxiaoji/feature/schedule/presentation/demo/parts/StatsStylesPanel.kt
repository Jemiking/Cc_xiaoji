package com.ccxiaoji.feature.schedule.presentation.demo.parts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.presentation.demo.DemoData
import com.ccxiaoji.feature.schedule.presentation.demo.IndicatorStyle

@Composable
fun StatsStylesPanel(
    data: DemoData,
    emphasizeNight: Boolean
) {
    // 顶部两种统计样式对比
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("卡片统计", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val work = data.schedules.size
                    val nights = data.schedules.count { it.shift.startTime != null && it.shift.endTime != null && (it.shift.endTime!!.isBefore(it.shift.startTime) || it.shift.startTime!!.hour >= 21 || it.shift.endTime!!.hour <= 6) }
                    val rest = 30 - work
                    StatItem("工作日", "$work 天")
                    StatItem("夜班", "$nights 天")
                    StatItem("休息", "$rest 天")
                }
            }
        }
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("极简统计", style = MaterialTheme.typography.titleMedium)
            MinimalStatsBar(data)
        }
    }

    Spacer(Modifier.height(12.dp))
    // 下方采用标签风格的月历
    MonthCalendarPanel(data = data, style = IndicatorStyle.Label, emphasizeNight = emphasizeNight)
}

@Composable
private fun MinimalStatsBar(data: DemoData) {
    val work = data.schedules.size
    val nights = data.schedules.count { it.shift.startTime != null && it.shift.endTime != null && (it.shift.endTime!!.isBefore(it.shift.startTime) || it.shift.startTime!!.hour >= 21 || it.shift.endTime!!.hour <= 6) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("工作 $work 天", style = MaterialTheme.typography.bodyLarge)
        Text("夜班 $nights 天", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
