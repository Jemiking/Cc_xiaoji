package com.ccxiaoji.feature.schedule.presentation.pattern

import androidx.compose.ui.res.stringResource
import com.ccxiaoji.feature.schedule.R

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.schedule.domain.model.Shift
import com.ccxiaoji.feature.schedule.presentation.components.DatePickerDialog
import com.ccxiaoji.feature.schedule.presentation.components.CustomDatePickerDialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * 批量排班界面
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.schedule_back))
                    }
                },
                actions = {
                    // 创建按钮
                    Button(
                        onClick = { viewModel.createSchedules() },
                        enabled = uiState.canCreate && !uiState.isLoading,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(stringResource(R.string.schedule_pattern_create))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
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
    }
    
    // 显示加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
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
            // 这里可以显示 Snackbar
        }
    }
}

/**
 * 日期范围选择部分
 */
@Composable
private fun DateRangeSection(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_date_range),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateSelector(
                    label = stringResource(R.string.schedule_pattern_start_date),
                    date = startDate,
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                )
                
                Text(stringResource(R.string.schedule_pattern_to), style = MaterialTheme.typography.bodyMedium)
                
                DateSelector(
                    label = stringResource(R.string.schedule_pattern_end_date),
                    date = endDate,
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 日期选择器（简化版，实际项目中应使用日期选择对话框）
 */
@Composable
private fun DateSelector(
    label: String,
    date: LocalDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 排班模式选择部分
 */
@Composable
private fun PatternTypeSection(
    selectedType: PatternType,
    onTypeChange: (PatternType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            PatternType.values().forEach { type ->
                val typeName = when (type) {
                    PatternType.SINGLE -> stringResource(R.string.schedule_pattern_single)
                    PatternType.CYCLE -> stringResource(R.string.schedule_pattern_cycle)
                    PatternType.ROTATION -> stringResource(R.string.schedule_pattern_rotation)
                    PatternType.CUSTOM -> stringResource(R.string.schedule_pattern_custom)
                }
                OutlinedCard(
                    onClick = { onTypeChange(type) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            typeName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (selectedType == type) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.schedule_pattern_selected),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单次排班配置部分
 */
@Composable
private fun SinglePatternSection(
    shifts: List<Shift>,
    selectedShift: Shift?,
    onShiftSelect: (Shift) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_select_shift),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            shifts.forEach { shift ->
                ShiftSelectionItem(
                    shift = shift,
                    isSelected = shift == selectedShift,
                    onSelect = { onShiftSelect(shift) }
                )
            }
        }
    }
}

/**
 * 循环排班配置部分（支持任意天数）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CyclePatternSection(
    shifts: List<Shift>,
    cycleDays: Int,
    cyclePattern: Map<Int, Long?>,
    onCycleDaysChange: (Int) -> Unit,
    onPatternChange: (Int, Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_cycle_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 循环天数选择
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.schedule_pattern_cycle_period),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.schedule_pattern_cycle_days),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 减少按钮
                        OutlinedIconButton(
                            onClick = { 
                                if (cycleDays > 2) onCycleDaysChange(cycleDays - 1) 
                            },
                            enabled = cycleDays > 2,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = stringResource(R.string.schedule_pattern_decrease_days)
                            )
                        }
                        
                        // 显示当前天数
                        Surface(
                            modifier = Modifier
                                .widthIn(min = 60.dp)
                                .height(40.dp),
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${cycleDays}${stringResource(R.string.schedule_pattern_cycle_days_unit)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 增加按钮
                        OutlinedIconButton(
                            onClick = { 
                                if (cycleDays < 365) onCycleDaysChange(cycleDays + 1) 
                            },
                            enabled = cycleDays < 365,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.schedule_pattern_increase_days)
                            )
                        }
                    }
                }
                
                // 支持范围提示
                Text(
                    stringResource(R.string.schedule_pattern_cycle_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            // 每天的班次选择
            (0 until cycleDays).forEach { dayIndex ->
                CycleDayShiftSelector(
                    dayIndex = dayIndex,
                    cycleDays = cycleDays,
                    shifts = shifts,
                    selectedShiftId = cyclePattern[dayIndex],
                    onShiftSelect = { shiftId ->
                        onPatternChange(dayIndex, shiftId)
                    }
                )
            }
        }
    }
}

/**
 * 周循环配置部分（已废弃，保留以兼容）
 */
@Deprecated("使用 CyclePatternSection 代替")
@Composable
private fun WeeklyPatternSection(
    shifts: List<Shift>,
    weekPattern: Map<DayOfWeek, Long?>,
    onPatternChange: (DayOfWeek, Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_cycle),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            DayOfWeek.values().forEach { dayOfWeek ->
                WeekDayShiftSelector(
                    dayOfWeek = dayOfWeek,
                    shifts = shifts,
                    selectedShiftId = weekPattern[dayOfWeek],
                    onShiftSelect = { shiftId ->
                        onPatternChange(dayOfWeek, shiftId)
                    }
                )
            }
        }
    }
}

/**
 * 轮班配置部分
 */
@Composable
private fun RotationPatternSection(
    shifts: List<Shift>,
    selectedShifts: List<Long>,
    restDays: Int,
    onShiftsChange: (List<Long>) -> Unit,
    onRestDaysChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_rotation_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                stringResource(R.string.schedule_pattern_rotation_order_hint),
                style = MaterialTheme.typography.bodyMedium
            )
            
            shifts.forEach { shift ->
                val position = selectedShifts.indexOf(shift.id).takeIf { it >= 0 }?.plus(1)
                ShiftSelectionItem(
                    shift = shift,
                    isSelected = shift.id in selectedShifts,
                    position = position,
                    onSelect = {
                        if (shift.id in selectedShifts) {
                            onShiftsChange(selectedShifts - shift.id)
                        } else {
                            onShiftsChange(selectedShifts + shift.id)
                        }
                    }
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.schedule_pattern_rotation_rest_days))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (restDays > 0) onRestDaysChange(restDays - 1) }
                    ) {
                        Text("-")
                    }
                    Text(restDays.toString())
                    IconButton(
                        onClick = { onRestDaysChange(restDays + 1) }
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}

/**
 * 循环天班次选择器
 */
@Composable
private fun CycleDayShiftSelector(
    dayIndex: Int,
    cycleDays: Int,
    shifts: List<Shift>,
    selectedShiftId: Long?,
    onShiftSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedShift = shifts.find { it.id == selectedShiftId }
    
    OutlinedCard(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.schedule_pattern_day_number, dayIndex + 1),
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (selectedShift != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                Color(selectedShift.color),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        selectedShift.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    stringResource(R.string.schedule_pattern_rest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.schedule_pattern_rest)) },
                onClick = {
                    onShiftSelect(null)
                    expanded = false
                }
            )
            
            shifts.forEach { shift ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(shift.color),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(shift.name)
                        }
                    },
                    onClick = {
                        onShiftSelect(shift.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 班次选择项
 */
@Composable
private fun ShiftSelectionItem(
    shift: Shift,
    isSelected: Boolean,
    position: Int? = null,
    onSelect: () -> Unit
) {
    OutlinedCard(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            Color(shift.color),
                            shape = MaterialTheme.shapes.small
                        )
                )
                
                Column {
                    Text(
                        shift.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${shift.startTime} - ${shift.endTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isSelected) {
                if (position != null) {
                    Badge {
                        Text(position.toString())
                    }
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = stringResource(R.string.schedule_pattern_selected),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 星期几班次选择器
 */
@Composable
private fun WeekDayShiftSelector(
    dayOfWeek: DayOfWeek,
    shifts: List<Shift>,
    selectedShiftId: Long?,
    onShiftSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedShift = shifts.find { it.id == selectedShiftId }
    
    OutlinedCard(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA),
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (selectedShift != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                Color(selectedShift.color),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        selectedShift.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    stringResource(R.string.schedule_pattern_rest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.schedule_pattern_rest)) },
                onClick = {
                    onShiftSelect(null)
                    expanded = false
                }
            )
            
            shifts.forEach { shift ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(shift.color),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(shift.name)
                        }
                    },
                    onClick = {
                        onShiftSelect(shift.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 自定义模式配置部分
 */
@Composable
private fun CustomPatternSection(
    shifts: List<Shift>,
    startDate: LocalDate,
    customPattern: List<Long?>,
    onPatternChange: (Int, Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.schedule_pattern_custom_mode),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (customPattern.isEmpty()) {
                Text(
                    stringResource(R.string.schedule_pattern_custom_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(customPattern) { index, shiftId ->
                        val date = startDate.plusDays(index.toLong())
                        CustomDayShiftSelector(
                            date = date,
                            shifts = shifts,
                            selectedShiftId = shiftId,
                            onShiftSelect = { id -> onPatternChange(index, id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 自定义模式的单日班次选择器
 */
@Composable
private fun CustomDayShiftSelector(
    date: LocalDate,
    shifts: List<Shift>,
    selectedShiftId: Long?,
    onShiftSelect: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedShift = shifts.find { it.id == selectedShiftId }
    
    OutlinedCard(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    date.format(DateTimeFormatter.ofPattern(stringResource(R.string.schedule_pattern_date_format_month_day))),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (selectedShift != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                Color(selectedShift.color),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        selectedShift.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Text(
                    stringResource(R.string.schedule_pattern_rest),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.schedule_pattern_rest)) },
                onClick = {
                    onShiftSelect(null)
                    expanded = false
                }
            )
            
            shifts.forEach { shift ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(shift.color),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(shift.name)
                        }
                    },
                    onClick = {
                        onShiftSelect(shift.id)
                        expanded = false
                    }
                )
            }
        }
    }
}