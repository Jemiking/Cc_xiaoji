package com.ccxiaoji.feature.habit.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak

@Composable
fun HabitStatisticsView(
    habits: List<HabitWithStreak>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 习惯完成率统计
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "习惯完成率",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val habitStatistics = habits.map { habitWithStreak ->
                        val habit = habitWithStreak.habit
                        val totalDays = when (habit.period) {
                            "daily" -> 30
                            "weekly" -> 4
                            "monthly" -> 1
                            else -> 30
                        }
                        HabitStatistic(
                            habitId = habit.id,
                            habitName = habit.title,
                            totalDays = totalDays,
                            completedDays = habitWithStreak.completedCount,
                            completionRate = if (totalDays > 0) habitWithStreak.completedCount.toFloat() / totalDays else 0f,
                            currentStreak = habitWithStreak.currentStreak,
                            longestStreak = habitWithStreak.longestStreak,
                            color = Color(habit.color.removePrefix("#").toLong(16) or 0xFF000000)
                        )
                    }
                    
                    HabitCompletionRateChart(
                        habits = habitStatistics,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // 习惯趋势图
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "习惯趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // TODO: 从ViewModel获取每日数据
                    val dailyData = listOf<HabitDailyData>() // 暂时空数据
                    
                    HabitTrendChart(
                        dailyData = dailyData,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // 习惯热力图
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "习惯热力图",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // TODO: 从ViewModel获取记录数据
                    val habitRecords = mapOf<kotlinx.datetime.LocalDate, Int>() // 暂时空数据
                    
                    HabitCalendarHeatmap(
                        habitRecords = habitRecords,
                        totalHabits = habits.size,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

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
    val date: kotlinx.datetime.LocalDate,
    val completedCount: Int,
    val totalCount: Int
)