package com.ccxiaoji.feature.schedule.presentation.calendar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 月度统计卡片（可折叠）
 */
@Composable
fun MonthlyStatisticsCard(
    statistics: ScheduleStatistics?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded },
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = DesignTokens.BrandColors.Schedule.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            // 标题行（始终显示）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "月度统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = DesignTokens.BrandColors.Schedule
                )
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            
            // 统计内容（可折叠）
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                statistics?.let { stats: ScheduleStatistics ->
                    Column {
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticItem(
                                label = stringResource(R.string.schedule_calendar_work_days),
                                value = stringResource(R.string.schedule_calendar_days_format, stats.workDays),
                                color = DesignTokens.BrandColors.Success
                            )
                            StatisticItem(
                                label = stringResource(R.string.schedule_calendar_rest_days),
                                value = stringResource(R.string.schedule_calendar_days_format, stats.restDays),
                                color = DesignTokens.BrandColors.Info
                            )
                            StatisticItem(
                                label = stringResource(R.string.schedule_calendar_total_hours),
                                value = stringResource(R.string.schedule_calendar_hours_int_format, stats.totalHours.toInt()),
                                color = DesignTokens.BrandColors.Warning
                            )
                        }
                    }
                } ?: run {
                    // 无数据时的占位
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignTokens.Spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无统计数据",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}