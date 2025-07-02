package com.ccxiaoji.app.presentation.ui.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

@Composable
fun HabitModuleCard(
    todayCheckedCount: Int,
    totalHabitCount: Int,
    longestStreak: Int,
    onCardClick: () -> Unit,
    onCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 计算完成率
    val completionRate = if (totalHabitCount > 0) {
        (todayCheckedCount.toFloat() / totalHabitCount * 100).toInt()
    } else 0
    
    // 动画效果
    var animatedCompletion by remember { mutableStateOf(0f) }
    LaunchedEffect(completionRate) {
        animate(
            initialValue = animatedCompletion,
            targetValue = completionRate.toFloat(),
            animationSpec = tween(1000, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedCompletion = value
        }
    }
    
    FlatModuleCard(
        title = "习惯",
        icon = Icons.Default.EventRepeat,
        moduleColor = DesignTokens.BrandColors.Habit,
        onClick = onCardClick,
        modifier = modifier
    ) {
        // 今日习惯统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DesignTokens.Spacing.small),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 今日打卡
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Habit.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Habit,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "已打卡",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$todayCheckedCount/$totalHabitCount",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Habit
                    )
                }
            }
            
            // 分隔线
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // 最长连续
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DesignTokens.BrandColors.Warning.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = DesignTokens.BrandColors.Warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "连续",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$longestStreak 天",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignTokens.BrandColors.Warning
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 完成进度
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "今日进度",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${animatedCompletion.toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.BrandColors.Habit
                )
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
            
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth((animatedCompletion / 100f).coerceIn(0f, 1f))
                        .background(
                            brush = DesignTokens.BrandGradients.ModuleHabit,
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
        
        // 快速操作按钮
        FlatButton(
            onClick = onCheckIn,
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DesignTokens.BrandColors.Habit,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
            Text(
                text = "去打卡",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}