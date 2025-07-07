package com.ccxiaoji.feature.schedule.presentation.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.statistics.TimeRange
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate

/**
 * 时间范围选择器 - 扁平化设计
 */
@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    customStartDate: LocalDate,
    customEndDate: LocalDate,
    onRangeChange: (TimeRange) -> Unit,
    onCustomDateChange: (LocalDate, LocalDate) -> Unit,
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
                stringResource(R.string.schedule_statistics_time_range),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // 预设时间范围
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.xs)
            ) {
                TimeRange.values().filter { it != TimeRange.CUSTOM }.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onRangeChange(range) },
                        label = { 
                            Text(when (range) {
                                TimeRange.THIS_WEEK -> stringResource(R.string.schedule_statistics_time_range_this_week)
                                TimeRange.THIS_MONTH -> stringResource(R.string.schedule_statistics_time_range_this_month)
                                TimeRange.LAST_MONTH -> stringResource(R.string.schedule_statistics_time_range_last_month)
                                TimeRange.CUSTOM -> stringResource(R.string.schedule_statistics_time_range_custom)
                            })
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedRange == TimeRange.CUSTOM,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
            
            // 自定义日期选择
            if (selectedRange == TimeRange.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    DateCard(
                        label = stringResource(R.string.schedule_export_start_date),
                        date = customStartDate,
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    )
                    
                    Text(
                        stringResource(R.string.schedule_export_to), 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    DateCard(
                        label = stringResource(R.string.schedule_export_end_date),
                        date = customEndDate,
                        modifier = Modifier.weight(1f),
                        onClick = onCustomDateClick
                    )
                }
            }
        }
    }
}