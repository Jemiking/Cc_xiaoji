package com.ccxiaoji.feature.schedule.presentation.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek

/**
 * 一周起始日选择对话框 - 扁平化设计
 */
@Composable
fun WeekStartDayDialog(
    showDialog: Boolean,
    currentWeekStartDay: DayOfWeek,
    onDaySelected: (DayOfWeek) -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        FlatDialog(
            onDismissRequest = onDismiss,
            title = stringResource(R.string.schedule_settings_week_start_dialog_title),
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.schedule_cancel))
                }
            }
        ) {
            Column {
                val weekDays = listOf(
                    DayOfWeek.MONDAY to stringResource(R.string.schedule_settings_week_start_monday),
                    DayOfWeek.SUNDAY to stringResource(R.string.schedule_settings_week_start_sunday)
                )
                
                weekDays.forEach { (dayOfWeek, displayName) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDaySelected(dayOfWeek)
                                onDismiss()
                            }
                            .padding(vertical = DesignTokens.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentWeekStartDay == dayOfWeek,
                            onClick = {
                                onDaySelected(dayOfWeek)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.small))
                        Text(
                            displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}