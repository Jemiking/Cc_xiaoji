package com.ccxiaoji.feature.schedule.presentation.export.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.statistics.TimeRange
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 时间范围选择部分 - 扁平化设计
 */
@Composable
fun TimeRangeSection(
    selectedRange: TimeRange,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    onRangeChange: (TimeRange) -> Unit,
    onCustomDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
        ) {
            Text(
                stringResource(R.string.schedule_export_time_range),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 预设时间范围
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                TimeRange.values().filter { it != TimeRange.CUSTOM }.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeChange(range) },
                        label = { 
                            Text(
                                when (range) {
                                    TimeRange.THIS_WEEK -> stringResource(R.string.schedule_statistics_time_range_this_week)
                                    TimeRange.THIS_MONTH -> stringResource(R.string.schedule_statistics_time_range_this_month)
                                    TimeRange.LAST_MONTH -> stringResource(R.string.schedule_statistics_time_range_last_month)
                                    TimeRange.CUSTOM -> stringResource(R.string.schedule_statistics_time_range_custom)
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedRange == range,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
            
            // 自定义时间范围
            FilterChip(
                selected = selectedRange == TimeRange.CUSTOM,
                onClick = { onRangeChange(TimeRange.CUSTOM) },
                label = { Text(stringResource(R.string.schedule_statistics_time_range_custom)) },
                modifier = Modifier.fillMaxWidth(),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedRange == TimeRange.CUSTOM,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
            
            // 日期选择器
            if (selectedRange == TimeRange.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick,
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.Spacing.small),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.schedule_export_start_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    Text(
                        stringResource(R.string.schedule_export_to),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick,
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignTokens.Spacing.small),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.schedule_export_end_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                customEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}