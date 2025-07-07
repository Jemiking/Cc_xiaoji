package com.ccxiaoji.feature.schedule.presentation.calendar.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek

/**
 * 日历星期标题行
 */
@Composable
fun CalendarWeekHeader(
    weekStartDay: DayOfWeek = DayOfWeek.MONDAY,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Spacing.small, vertical = DesignTokens.Spacing.small)
    ) {
        val weekDays = when (weekStartDay) {
            DayOfWeek.SUNDAY -> listOf(
                stringResource(R.string.schedule_weekday_short_sunday),
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday)
            )
            DayOfWeek.MONDAY -> listOf(
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday),
                stringResource(R.string.schedule_weekday_short_sunday)
            )
            else -> listOf(
                stringResource(R.string.schedule_weekday_short_monday),
                stringResource(R.string.schedule_weekday_short_tuesday),
                stringResource(R.string.schedule_weekday_short_wednesday),
                stringResource(R.string.schedule_weekday_short_thursday),
                stringResource(R.string.schedule_weekday_short_friday),
                stringResource(R.string.schedule_weekday_short_saturday),
                stringResource(R.string.schedule_weekday_short_sunday)
            )
        }
        
        weekDays.forEachIndexed { index, day ->
            val isWeekend = when (weekStartDay) {
                DayOfWeek.SUNDAY -> index == 0 || index == 6
                DayOfWeek.MONDAY -> index == 5 || index == 6
                else -> index == 5 || index == 6
            }
            
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isWeekend) {
                    DesignTokens.BrandColors.Error.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}