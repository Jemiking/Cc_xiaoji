package com.ccxiaoji.feature.habit.presentation.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class HabitStatistic(
    val habitId: String,
    val habitName: String,
    val totalDays: Int,
    val completedDays: Int,
    val completionRate: Float,
    val currentStreak: Int,
    val longestStreak: Int,
    val color: Color
)

data class HabitDailyData(
    val date: LocalDate,
    val completedHabits: Int,
    val totalHabits: Int,
    val completionRate: Float
)

@Composable
fun HabitCompletionRateChart(
    habits: List<HabitStatistic>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall completion rate
        val overallRate = if (habits.isNotEmpty()) {
            habits.sumOf { it.completedDays } / habits.sumOf { it.totalDays }.toFloat()
        } else 0f
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatisticCard(
                title = "总体完成率",
                value = "%.1f%%".format(overallRate * 100),
                color = MaterialTheme.colorScheme.primary
            )
            StatisticCard(
                title = "活跃习惯",
                value = habits.size.toString(),
                color = MaterialTheme.colorScheme.secondary
            )
            StatisticCard(
                title = "最长连续",
                value = "${habits.maxOfOrNull { it.longestStreak } ?: 0}天",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        
        // Individual habit completion rates
        habits.forEach { habit ->
            HabitProgressBar(
                habit = habit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HabitTrendChart(
    dailyData: List<HabitDailyData>,
    modifier: Modifier = Modifier
) {
    if (dailyData.isEmpty()) {
        Box(
            modifier = modifier.height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        // Draw axes
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = 1.dp.toPx()
        )
        
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = 1.dp.toPx()
        )
        
        // Draw grid lines
        for (i in 0..4) {
            val y = padding + chartHeight * i / 4
            drawLine(
                color = Color.Gray.copy(alpha = 0.1f),
                start = Offset(padding, y),
                end = Offset(size.width - padding, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }
        
        // Draw trend line
        val points = dailyData.mapIndexed { index, data ->
            val x = padding + chartWidth * index / (dailyData.size - 1)
            val y = padding + chartHeight * (1 - data.completionRate)
            Offset(x, y)
        }
        
        for (i in 1 until points.size) {
            drawLine(
                color = primaryColor,
                start = points[i - 1],
                end = points[i],
                strokeWidth = 2.dp.toPx()
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
fun HabitCalendarHeatmap(
    habitRecords: Map<LocalDate, Int>, // Date to completion count
    totalHabits: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val cellSize = 15.dp.toPx()
        val cellSpacing = 2.dp.toPx()
        val startX = 30.dp.toPx()
        val startY = 20.dp.toPx()
        
        val sortedDates = habitRecords.keys.sorted()
        if (sortedDates.isEmpty()) return@Canvas
        
        val firstDate = sortedDates.first()
        val lastDate = sortedDates.last()
        
        var currentDate = firstDate
        var col = 0
        var row = currentDate.dayOfWeek.ordinal
        
        while (currentDate <= lastDate) {
            val completedCount = habitRecords[currentDate] ?: 0
            val completionRate = if (totalHabits > 0) completedCount / totalHabits.toFloat() else 0f
            
            val x = startX + col * (cellSize + cellSpacing)
            val y = startY + row * (cellSize + cellSpacing)
            
            drawRect(
                color = getHeatmapColor(completionRate),
                topLeft = Offset(x, y),
                size = Size(cellSize, cellSize)
            )
            
            // Move to next date
            // Convert kotlinx.datetime.LocalDate to java.time.LocalDate
            val javaDate = java.time.LocalDate.of(
                currentDate.year,
                currentDate.monthNumber,
                currentDate.dayOfMonth
            )
            val nextJavaDate = javaDate.plusDays(1)
            // Convert back to kotlinx.datetime.LocalDate
            currentDate = LocalDate(
                nextJavaDate.year,
                nextJavaDate.monthValue,
                nextJavaDate.dayOfMonth
            )
            row++
            if (row > 6) {
                row = 0
                col++
            }
        }
        
        // Draw day labels
        val days = listOf("一", "二", "三", "四", "五", "六", "日")
        days.forEachIndexed { index, day ->
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = Color.Gray.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(
                    day,
                    startX - 5.dp.toPx(),
                    startY + index * (cellSize + cellSpacing) + cellSize / 2,
                    paint
                )
            }
        }
    }
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun HabitProgressBar(
    habit: HabitStatistic,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = habit.habitName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "%.0f%%".format(habit.completionRate * 100),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = habit.color
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.1f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                
                // Progress
                if (habit.completionRate > 0) {
                    drawRoundRect(
                        color = habit.color,
                        size = Size(size.width * habit.completionRate, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${habit.completedDays}/${habit.totalDays} 天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "连续 ${habit.currentStreak} 天",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getHeatmapColor(completionRate: Float): Color {
    return when {
        completionRate == 0f -> Color(0xFFEEEEEE)
        completionRate < 0.25f -> Color(0xFFC6E48B)
        completionRate < 0.5f -> Color(0xFF7BC96F)
        completionRate < 0.75f -> Color(0xFF239A3B)
        else -> Color(0xFF196127)
    }
}