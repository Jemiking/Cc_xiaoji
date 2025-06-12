package com.ccxiaoji.feature.habit.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

/**
 * 习惯完成率图表
 */
@Composable
fun HabitCompletionRateChart(
    habits: List<HabitStatistic>,
    modifier: Modifier = Modifier
) {
    // TODO: 实现实际的图表
    Box(
        modifier = modifier
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "习惯完成率图表",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 习惯趋势图表
 */
@Composable
fun HabitTrendChart(
    dailyData: List<HabitDailyData>,
    modifier: Modifier = Modifier
) {
    // TODO: 实现实际的图表
    Box(
        modifier = modifier
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "习惯趋势图表",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 习惯日历热力图
 */
@Composable
fun HabitCalendarHeatmap(
    habitRecords: Map<LocalDate, Int>,
    totalHabits: Int,
    modifier: Modifier = Modifier
) {
    // TODO: 实现实际的热力图
    Box(
        modifier = modifier
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "习惯热力图",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}