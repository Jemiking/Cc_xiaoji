package com.ccxiaoji.feature.schedule.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.ZoneId

/**
 * 日期选择页面 - 替代原DatePickerDialog
 * 使用Material 3的DatePicker组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerScreen(
    initialDate: String?, // 格式: "yyyy-MM-dd"
    navController: NavController
) {
    // 解析初始日期
    val parsedDate = initialDate?.let {
        try {
            LocalDate.parse(it)
        } catch (e: Exception) {
            null
        }
    }
    
    // 创建日期选择器状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = parsedDate?.let { date ->
            date.atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("选择日期")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.large)
        ) {
            // 说明卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Text(
                        text = "选择日期",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "请从日历中选择所需的日期",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Material3 日期选择器
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            currentYearContentColor = MaterialTheme.colorScheme.primary,
                            selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                            dayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 取消按钮
                FlatButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(stringResource(R.string.schedule_cancel))
                }
                
                // 确定按钮
                FlatButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_date", selectedDate.toString())
                        }
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