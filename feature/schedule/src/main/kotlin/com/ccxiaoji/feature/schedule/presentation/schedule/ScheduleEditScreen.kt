package com.ccxiaoji.feature.schedule.presentation.schedule

import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.viewmodel.ScheduleEditViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 排班编辑界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditScreen(
    date: String?,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleEditViewModel = hiltViewModel()
) {
    val selectedDate = remember(date) {
        date?.let { LocalDate.parse(it) } ?: LocalDate.now()
    }
    
    val shifts by viewModel.shifts.collectAsState()
    val currentSchedule by viewModel.currentSchedule.collectAsState()
    val selectedShift by viewModel.selectedShift.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // 初始化加载当前日期的排班
    LaunchedEffect(selectedDate) {
        viewModel.loadScheduleForDate(selectedDate)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.schedule_edit_title_with_date, selectedDate.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_calendar_date_format_full))))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.schedule_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSchedule(selectedDate)
                            onNavigateBack()
                        },
                        enabled = selectedShift != null
                    ) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.schedule_save))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 当前排班信息
            currentSchedule?.let { schedule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = Color(schedule.shift.color)
                            ) {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.schedule_edit_current_shift, schedule.shift.name),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${schedule.shift.startTime} - ${schedule.shift.endTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 班次选择提示
            Text(
                text = if (currentSchedule == null) stringResource(R.string.schedule_edit_select_shift) else stringResource(R.string.schedule_edit_change_shift),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 班次列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 添加"休息"选项
                item {
                    ShiftCard(
                        shift = null,
                        isSelected = selectedShift == null && currentSchedule == null,
                        onClick = { viewModel.selectShift(null) }
                    )
                }
                
                // 班次列表
                items(shifts) { shift ->
                    ShiftCard(
                        shift = shift,
                        isSelected = selectedShift?.id == shift.id,
                        onClick = { viewModel.selectShift(shift) }
                    )
                }
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // TODO: 显示Snackbar
            viewModel.clearError()
        }
    }
}

/**
 * 班次卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftCard(
    shift: Shift?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 颜色指示器
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (shift != null) Color(shift.color) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            
            // 班次信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shift?.name ?: stringResource(R.string.schedule_edit_rest),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (shift != null) {
                    Text(
                        text = "${shift.startTime} - ${shift.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    shift.description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            
            // 选中标记
            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}