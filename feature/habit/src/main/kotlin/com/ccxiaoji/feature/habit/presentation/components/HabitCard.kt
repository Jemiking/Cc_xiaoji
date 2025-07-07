package com.ccxiaoji.feature.habit.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.habit.R
import com.ccxiaoji.feature.habit.domain.model.HabitWithStreak
import com.ccxiaoji.feature.habit.presentation.utils.HabitColorMapper
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 习惯卡片组件
 * 使用扁平化设计展示单个习惯信息
 */
@Composable
fun HabitCard(
    habitWithStreak: HabitWithStreak,
    isCheckedToday: Boolean,
    onCheckIn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithStreak.habit
    val habitColor = HabitColorMapper.getHabitColor(habit.color, habit.title)
    
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (isCheckedToday) habitColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 习惯信息
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        // 习惯图标
                        habit.icon?.let { icon ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(habitColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = icon,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        
                        Column {
                            // 习惯标题
                            Text(
                                text = habit.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            // 周期和目标
                            Text(
                                text = buildString {
                                    append(when (habit.period) {
                                        "daily" -> stringResource(R.string.period_daily)
                                        "weekly" -> stringResource(R.string.period_weekly)
                                        "monthly" -> stringResource(R.string.period_monthly)
                                        else -> habit.period
                                    })
                                    append(" · 目标 ${habit.target} 次")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // 习惯描述
                    habit.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = DesignTokens.Spacing.small)
                        )
                    }
                    
                    // 连续天数显示
                    if (habitWithStreak.currentStreak > 0) {
                        Row(
                            modifier = Modifier.padding(top = DesignTokens.Spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                        ) {
                            // 火焰图标
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(DesignTokens.BrandColors.Warning.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔥",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            Text(
                                text = stringResource(R.string.streak_days, habitWithStreak.currentStreak),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = DesignTokens.BrandColors.Warning
                            )
                            
                            // 最长连续记录
                            if (habitWithStreak.longestStreak > habitWithStreak.currentStreak) {
                                Text(
                                    text = "最高 ${habitWithStreak.longestStreak} 天",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
                ) {
                    // 打卡按钮
                    IconButton(
                        onClick = onCheckIn,
                        enabled = !isCheckedToday
                    ) {
                        Icon(
                            imageVector = if (isCheckedToday) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Outlined.CheckCircleOutline
                            },
                            contentDescription = stringResource(R.string.check_in),
                            tint = if (isCheckedToday) {
                                habitColor
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    // 编辑按钮
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // 删除按钮
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // 进度条（如果今天已打卡）
            if (isCheckedToday) {
                LinearProgressIndicator(
                    progress = { 
                        val progress = habitWithStreak.completedCount.toFloat() / habit.target.coerceAtLeast(1)
                        progress.coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignTokens.Spacing.small)
                        .height(4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                    color = habitColor,
                    trackColor = habitColor.copy(alpha = 0.1f)
                )
            }
        }
    }
}