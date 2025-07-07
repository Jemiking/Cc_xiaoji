package com.ccxiaoji.feature.schedule.presentation.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Schedule
import com.ccxiaoji.feature.schedule.presentation.utils.ShiftColorMapper
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 选中日期详情卡片
 */
@Composable
fun SelectedDateDetailCard(
    date: LocalDate,
    schedule: Schedule?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = if (schedule != null) {
            ShiftColorMapper.getColorForShift(schedule.shift.color).copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium)
        ) {
            // 日期标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_full))),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                // 操作按钮
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    if (schedule != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.schedule_calendar_delete_schedule),
                                tint = DesignTokens.BrandColors.Error
                            )
                        }
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.schedule_calendar_edit_schedule),
                            tint = DesignTokens.BrandColors.Schedule
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))
            
            // 班次信息
            if (schedule != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                ) {
                    // 班次颜色标识（扁平化设计）
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = ShiftColorMapper.getBackgroundColorForShift(schedule.shift.color, 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = schedule.shift.name.take(2),
                            style = MaterialTheme.typography.titleMedium,
                            color = ShiftColorMapper.getColorForShift(schedule.shift.color),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 班次详情
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.shift.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (schedule.shift.startTime != null && schedule.shift.endTime != null) {
                            Text(
                                text = "${schedule.shift.startTime} - ${schedule.shift.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = stringResource(R.string.schedule_calendar_work_hours) + stringResource(R.string.schedule_calendar_hours_format, schedule.shift.duration),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                // 无排班时的提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.Spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.schedule_calendar_no_schedule),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))
                        Text(
                            text = "点击编辑按钮添加排班",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}