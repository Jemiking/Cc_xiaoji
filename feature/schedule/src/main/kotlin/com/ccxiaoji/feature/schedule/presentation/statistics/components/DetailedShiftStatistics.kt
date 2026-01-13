package com.ccxiaoji.feature.schedule.presentation.statistics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSectionCard
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 详细班次统计 - 使用 UI Kit 组件
 */
@Composable
fun DetailedShiftStatistics(
    distribution: Map<String, Int>,
    totalHours: Double,
    shifts: List<Shift>,
    modifier: Modifier = Modifier
) {
    ScheduleSectionCard(
        title = stringResource(R.string.schedule_statistics_shift_details),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            distribution.forEach { (shiftName, days) ->
                val shift = shifts.find { it.name == shiftName }
                if (shift != null) {
                    ShiftDetailRow(
                        shift = shift,
                        days = days,
                        totalHours = days * shift.duration
                    )
                    if (shiftName != distribution.keys.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = DesignTokens.Spacing.xs),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}