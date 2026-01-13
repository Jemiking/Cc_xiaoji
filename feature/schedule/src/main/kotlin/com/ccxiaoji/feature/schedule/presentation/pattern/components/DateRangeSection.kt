package com.ccxiaoji.feature.schedule.presentation.pattern.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSectionCard
import androidx.compose.ui.unit.dp
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 日期范围选择部分 - 扁平化设计
 */
@Composable
fun DateRangeSection(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ScheduleSectionCard(
        title = stringResource(R.string.schedule_pattern_date_range),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateSelector(
                label = stringResource(R.string.schedule_pattern_start_date),
                date = startDate,
                onClick = onStartDateClick,
                modifier = Modifier.weight(1f)
            )

            Text(
                stringResource(R.string.schedule_pattern_to),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DateSelector(
                label = stringResource(R.string.schedule_pattern_end_date),
                date = endDate,
                onClick = onEndDateClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 日期选择器 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DesignTokens.Spacing.medium)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}