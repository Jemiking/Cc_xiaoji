package com.ccxiaoji.feature.schedule.presentation.shift

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.viewmodel.QuickShiftSelectionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 快速班次选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickShiftSelectionScreen(
    navController: NavController,
    selectedDate: String,
    viewModel: QuickShiftSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val date = LocalDate.parse(selectedDate)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = stringResource(R.string.schedule_dialog_quick_select_shift),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_format_date_pattern_month_day))),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.schedule_dialog_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 清除排班选项
            item {
                QuickShiftOption(
                    shift = null,
                    isSelected = uiState.currentShiftId == null,
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_shift_id", 0L)
                        navController.popBackStack()
                    }
                )
            }
            
            // 常用班次列表
            items(uiState.quickShifts) { shift ->
                QuickShiftOption(
                    shift = shift,
                    isSelected = uiState.currentShiftId == shift.id,
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("selected_shift_id", shift.id)
                        navController.popBackStack()
                    }
                )
            }
            
            // 更多选项按钮
            item {
                OutlinedButton(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("navigate_to_full_selector", true)
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.schedule_dialog_more_shifts))
                }
            }
        }
    }
}

/**
 * 快速班次选项项
 */
@Composable
private fun QuickShiftOption(
    shift: Shift?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (shift != null) {
                // 班次信息
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 班次颜色标识
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(shift.color),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shift.name.take(1),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 班次详情
                    Column {
                        Text(
                            text = shift.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${shift.startTime} - ${shift.endTime}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 清除排班选项
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.schedule_dialog_clear_schedule),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // 选中标记
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.schedule_dialog_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}