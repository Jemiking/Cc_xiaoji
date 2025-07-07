package com.ccxiaoji.feature.schedule.presentation.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.ScheduleStatistics
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 统计概览 - 扁平化设计
 */
@Composable
fun StatisticsOverview(
    statistics: ScheduleStatistics,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            Text(
                stringResource(R.string.schedule_statistics_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_total_days),
                    value = statistics.totalDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_work_days),
                    value = statistics.workDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_rest_days),
                    value = statistics.restDays.toString(),
                    unit = stringResource(R.string.schedule_statistics_day_unit)
                )
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                thickness = 1.dp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_total_hours),
                    value = "%.1f".format(statistics.totalHours),
                    unit = stringResource(R.string.schedule_statistics_hour_unit)
                )
                StatisticCard(
                    title = stringResource(R.string.schedule_statistics_average_hours),
                    value = if (statistics.workDays > 0) 
                        "%.1f".format(statistics.totalHours / statistics.workDays)
                    else "0",
                    unit = stringResource(R.string.schedule_statistics_hour_per_day_unit)
                )
            }
        }
    }
}