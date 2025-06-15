package com.ccxiaoji.feature.schedule.presentation.ui.pattern

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
import com.ccxiaoji.feature.schedule.presentation.ui.components.DatePickerDialog
import com.ccxiaoji.feature.schedule.presentation.ui.components.CustomDatePickerDialog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.ccxiaoji.feature.schedule.presentation.viewmodel.SchedulePatternViewModel

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
                title = { Text("批量排班") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 创建按钮
                    Button(
                        onClick = { viewModel.createSchedules() },
                        enabled = uiState.canCreate && !uiState.isLoading,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text("创建")
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
        selectedDate = uiState.startDate,
        onDateSelected = {
            viewModel.updateStartDate(it)
            showStartDatePicker = false
        },
        onDismiss = { showStartDatePicker = false }
    )
    
    // 结束日期选择器 - 使用自定义日期选择器
    CustomDatePickerDialog(
        showDialog = showEndDatePicker,
        selectedDate = uiState.endDate,
        onDateSelected = {
            viewModel.updateEndDate(it)
            showEndDatePicker = false
        },
        onDismiss = { showEndDatePicker = false }
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
                "日期范围",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateSelector(
                    label = "开始日期",
                    date = startDate,
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                )
                
                Text("至", style = MaterialTheme.typography.bodyMedium)
                
                DateSelector(
                    label = "结束日期",
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
                "排班模式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            PatternType.values().forEach { type ->
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
                            type.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (selectedType == type) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "已选择",
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
                "选择班次",
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
                "循环排班设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 循环天数选择
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "循环周期设置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "循环天数",
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
                                contentDescription = "减少天数"
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
                                    text = "${cycleDays}天",
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
                                contentDescription = "增加天数"
                            )
                        }
                    }
                }
                
                // 支持范围提示
                Text(
                    "支持 2-365 天的任意循环周期",
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
                "周循环排班",
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
                "轮班设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "请按顺序选择班次：",
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
                Text("轮班后休息天数")
                
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
                "第 ${dayIndex + 1} 天",
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
                    "休息",
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
                text = { Text("休息") },
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
                        contentDescription = "已选择",
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
                    "休息",
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
                text = { Text("休息") },
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
                "自定义排班模式",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (customPattern.isEmpty()) {
                Text(
                    "请选择日期范围后自动生成配置",
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
                    date.format(DateTimeFormatter.ofPattern("MM月dd日")),
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
                    "休息",
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
                text = { Text("休息") },
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