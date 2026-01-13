package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleBottomActions
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSelectableCard
import com.ccxiaoji.feature.schedule.presentation.uikit.ScheduleSimpleScaffold
import com.ccxiaoji.feature.schedule.presentation.uikit.confirmCancelActions
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek

/**
 * 周起始日选择页面 - 使用 Schedule UI Kit 组件
 */
@Composable
fun WeekStartDayScreen(
    currentWeekStartDay: DayOfWeek,
    navController: NavController
) {
    var selectedDay by remember { mutableStateOf(currentWeekStartDay) }

    ScheduleSimpleScaffold(
        title = stringResource(R.string.schedule_settings_week_start_dialog_title),
        onNavigateBack = { navController.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 说明文字
            ScheduleCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(DesignTokens.Spacing.small))

                    Text(
                        text = "选择一周的起始日",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "这会影响日历和统计的显示方式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.large))

            // 周一选项
            ScheduleSelectableCard(
                selected = selectedDay == DayOfWeek.MONDAY,
                onClick = { selectedDay = DayOfWeek.MONDAY },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.RadioButton
                        selected = selectedDay == DayOfWeek.MONDAY
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.schedule_settings_week_start_monday),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedDay == DayOfWeek.MONDAY)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "符合工作日习惯",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedDay == DayOfWeek.MONDAY)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RadioButton(
                        selected = selectedDay == DayOfWeek.MONDAY,
                        onClick = { selectedDay = DayOfWeek.MONDAY }
                    )
                }
            }

            Spacer(modifier = Modifier.height(DesignTokens.Spacing.medium))

            // 周日选项
            ScheduleSelectableCard(
                selected = selectedDay == DayOfWeek.SUNDAY,
                onClick = { selectedDay = DayOfWeek.SUNDAY },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        role = Role.RadioButton
                        selected = selectedDay == DayOfWeek.SUNDAY
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.schedule_settings_week_start_sunday),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedDay == DayOfWeek.SUNDAY)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "传统周末开始",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedDay == DayOfWeek.SUNDAY)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RadioButton(
                        selected = selectedDay == DayOfWeek.SUNDAY,
                        onClick = { selectedDay = DayOfWeek.SUNDAY }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 底部操作栏
            val actions = confirmCancelActions(
                onConfirm = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("week_start_day", selectedDay)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
            ScheduleBottomActions(
                primaryAction = actions.first,
                secondaryAction = actions.second,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
