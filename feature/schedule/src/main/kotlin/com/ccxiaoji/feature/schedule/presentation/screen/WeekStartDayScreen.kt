package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.DayOfWeek

/**
 * 周起始日选择页面 - 替代原WeekStartDayDialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekStartDayScreen(
    currentWeekStartDay: DayOfWeek,
    navController: NavController
) {
    var selectedDay by remember { mutableStateOf(currentWeekStartDay) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(stringResource(R.string.schedule_settings_week_start_dialog_title))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(DesignTokens.Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 说明文字
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(DesignTokens.Spacing.medium),
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedDay == DayOfWeek.MONDAY,
                        onClick = { selectedDay = DayOfWeek.MONDAY },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedDay == DayOfWeek.MONDAY) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedDay == DayOfWeek.SUNDAY,
                        onClick = { selectedDay = DayOfWeek.SUNDAY },
                        role = Role.RadioButton
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedDay == DayOfWeek.SUNDAY) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
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
            
            // 按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                FlatButton(
                    onClick = { 
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(stringResource(R.string.schedule_cancel))
                }
                
                FlatButton(
                    onClick = { 
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("week_start_day", selectedDay)
                        navController.popBackStack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.schedule_confirm))
                }
            }
        }
    }
}