package com.ccxiaoji.feature.schedule.presentation.pattern

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.R
import com.ccxiaoji.feature.schedule.presentation.components.CustomDatePickerDialog
import com.ccxiaoji.feature.schedule.presentation.pattern.components.*
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 批量排班界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulePatternScreen(
    onBack: () -> Unit,
    viewModel: SchedulePatternViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val shifts by viewModel.shifts.collectAsState()
    val weekStartDay by viewModel.weekStartDay.collectAsState()
    
    // 日期选择器状态
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_pattern_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.schedule_back)
                        )
                    }
                },
                actions = {
                    // 创建按钮
                    Button(
                        onClick = { viewModel.createSchedules() },
                        enabled = uiState.canCreate && !uiState.isLoading,
                        modifier = Modifier.padding(end = DesignTokens.Spacing.medium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.schedule_pattern_create))
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
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium),
                contentPadding = PaddingValues(vertical = DesignTokens.Spacing.medium)
            ) {
                // 日期范围选择
                item {
                    DateRangeSection(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        onStartDateClick = { showStartDatePicker = true },
                        onEndDateClick = { showEndDatePicker = true }
                    )
                }
                
                // 排班模式选择
                item {
                    PatternTypeSection(
                        selectedType = uiState.patternType,
                        onTypeChange = viewModel::updatePatternType
                    )
                }
                
                // 根据不同模式显示不同的配置界面
                when (uiState.patternType) {
                    PatternType.SINGLE -> {
                        item {
                            SinglePatternSection(
                                shifts = shifts,
                                selectedShift = uiState.selectedShift,
                                onShiftSelect = viewModel::selectShift
                            )
                        }
                    }
                    PatternType.CYCLE -> {
                        item {
                            CyclePatternSection(
                                shifts = shifts,
                                cycleDays = uiState.cycleDays,
                                cyclePattern = uiState.cyclePattern,
                                onCycleDaysChange = viewModel::updateCycleDays,
                                onPatternChange = viewModel::updateCyclePattern
                            )
                        }
                    }
                    PatternType.ROTATION -> {
                        item {
                            RotationPatternSection(
                                shifts = shifts,
                                selectedShifts = uiState.rotationShifts,
                                restDays = uiState.restDays,
                                onShiftsChange = viewModel::updateRotationShifts,
                                onRestDaysChange = viewModel::updateRestDays
                            )
                        }
                    }
                    PatternType.CUSTOM -> {
                        item {
                            CustomPatternSection(
                                shifts = shifts,
                                startDate = uiState.startDate,
                                customPattern = uiState.customPattern,
                                onPatternChange = viewModel::updateCustomPattern
                            )
                        }
                    }
                }
            }
            
            // 显示加载状态
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    // 开始日期选择器 - 使用自定义日期选择器
    CustomDatePickerDialog(
        showDialog = showStartDatePicker,
        initialDate = uiState.startDate,
        onDateSelected = {
            viewModel.updateStartDate(it)
            showStartDatePicker = false
        },
        onDismiss = { showStartDatePicker = false },
        weekStartDay = weekStartDay
    )
    
    // 结束日期选择器 - 使用自定义日期选择器
    CustomDatePickerDialog(
        showDialog = showEndDatePicker,
        initialDate = uiState.endDate,
        onDateSelected = {
            viewModel.updateEndDate(it)
            showEndDatePicker = false
        },
        onDismiss = { showEndDatePicker = false },
        weekStartDay = weekStartDay
    )
    
    // 显示成功状态
    if (uiState.isSuccess) {
        LaunchedEffect(Unit) {
            onBack()
        }
    }
}