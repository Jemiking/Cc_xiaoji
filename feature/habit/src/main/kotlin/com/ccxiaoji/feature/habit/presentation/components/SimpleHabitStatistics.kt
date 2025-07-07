package com.ccxiaoji.feature.habit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 简化的习惯统计组件
 * 只显示核心统计数据
 */
@Composable
fun SimpleHabitStatistics(
    habits: List<HabitWithStreak>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
    ) {
        // 总体统计卡片
        OverallStatisticsCard(habits)
        
        // 活跃习惯卡片（最多显示3个）
        ActiveHabitsCard(habits.take(3))
    }
}

@Composable
private fun OverallStatisticsCard(
    habits: List<HabitWithStreak>
) {
    val totalHabits = habits.size
    val completedToday = habits.count { it.completedCount > 0 }  // 使用 completedCount 作为今日完成的指标
    val completionRate = if (totalHabits > 0) completedToday.toFloat() / totalHabits else 0f
    val totalStreak = habits.sumOf { it.currentStreak }
    
    ModernCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = DesignTokens.BrandColors.Habit.copy(alpha = 0.2f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.large)
        ) {
            Text(
                text = "今日进度",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.BrandColors.Habit
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = DesignTokens.BrandColors.Habit,
                    trackColor = DesignTokens.BrandColors.Habit.copy(alpha = 0.1f)
                )
                
                Text(
                    text = "$completedToday/$totalHabits",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Habit
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))
            
            // 统计数据网格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 总习惯数
                StatisticItem(
                    label = "总习惯",
                    value = totalHabits.toString(),
                    color = DesignTokens.BrandColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                
                // 今日完成
                StatisticItem(
                    label = "今日完成",
                    value = completedToday.toString(),
                    color = DesignTokens.BrandColors.Success,
                    modifier = Modifier.weight(1f)
                )
                
                // 总连续天数
                StatisticItem(
                    label = "总连续天数",
                    value = totalStreak.toString(),
                    color = DesignTokens.BrandColors.Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.xs))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActiveHabitsCard(
    topHabits: List<HabitWithStreak>
) {
    if (topHabits.isEmpty()) return
    
    ModernCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.large)
        ) {
            Text(
                text = "活跃习惯",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            topHabits.forEach { habitWithStreak ->
                HabitProgressItem(habitWithStreak)
                if (habitWithStreak != topHabits.last()) {
                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                }
            }
        }
    }
}

@Composable
private fun HabitProgressItem(
    habitWithStreak: HabitWithStreak
) {
    val habit = habitWithStreak.habit
    val progress = habitWithStreak.completedCount.toFloat() / habit.target.coerceAtLeast(1)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
    ) {
        // 图标
        habit.icon?.let { icon ->
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon)
            }
        }
        
        // 标题和进度
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = DesignTokens.BrandColors.Habit,
                trackColor = DesignTokens.BrandColors.Habit.copy(alpha = 0.1f)
            )
        }
        
        // 完成次数
        Text(
            text = "${habitWithStreak.completedCount}/${habit.target}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}