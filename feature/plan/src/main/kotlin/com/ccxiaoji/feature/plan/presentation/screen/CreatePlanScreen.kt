package com.ccxiaoji.feature.plan.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.presentation.components.ParentPlanSelector
import kotlinx.datetime.*
import java.util.Locale

/**
 * 创建计划页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onBackClick: () -> Unit,
    onPlanCreated: (String) -> Unit,
    parentPlanId: String? = null,
    viewModel: CreatePlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 初始化父计划ID
    LaunchedEffect(parentPlanId) {
        parentPlanId?.let {
            viewModel.setParentPlan(it)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建计划") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createPlan()
                        },
                        enabled = uiState.isValid && !uiState.isLoading
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题输入
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("计划标题 *") },
                    placeholder = { Text("输入计划标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    isError = uiState.titleError != null
                )
                
                // 描述输入
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("计划描述") },
                    placeholder = { Text("输入计划描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // 日期选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 开始日期
                    DatePickerField(
                        label = "开始日期 *",
                        date = uiState.startDate,
                        onDateSelected = viewModel::updateStartDate,
                        isError = uiState.startDateError != null,
                        errorMessage = uiState.startDateError,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // 结束日期
                    DatePickerField(
                        label = "结束日期 *",
                        date = uiState.endDate,
                        onDateSelected = viewModel::updateEndDate,
                        isError = uiState.endDateError != null,
                        errorMessage = uiState.endDateError,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 父计划选择
                if (parentPlanId == null) {
                    ParentPlanSelector(
                        selectedParentId = uiState.parentPlan?.id,
                        onParentSelected = { id, title ->
                            if (id != null && title != null) {
                                viewModel.setParentPlanDetails(id, title)
                            } else {
                                viewModel.setParentPlanDetails("", "")
                            }
                        }
                    )
                }
                
                // 颜色选择
                ColorSelector(
                    selectedColor = uiState.color,
                    onColorSelected = viewModel::updateColor
                )
                
                // 标签输入
                TagInput(
                    tags = uiState.tags,
                    onTagsChanged = viewModel::updateTags,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 优先级选择
                PrioritySelector(
                    priority = uiState.priority,
                    onPrioritySelected = viewModel::updatePriority
                )
            }
        }
    }
    
    // 处理创建成功
    LaunchedEffect(uiState.createdPlanId) {
        uiState.createdPlanId?.let { planId ->
            onPlanCreated(planId)
        }
    }
    
    // 显示错误
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 日期选择器字段
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = date?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text("选择日期") },
        modifier = modifier,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
            }
        },
        isError = isError
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = date,
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 日期选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.atStartOfDayIn(TimeZone.currentSystemDefault())
            ?.toEpochMilliseconds()
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        onDateSelected(localDate)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


/**
 * 颜色选择器
 */
@Composable
private fun ColorSelector(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF5252", "#E91E63", "#9C27B0", "#673AB7",
        "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
        "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
        "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
        "#795548", "#9E9E9E", "#607D8B"
    )
    
    Column {
        Text(
            text = "选择颜色",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(colors) { color ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(color)))
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (color == selectedColor) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "已选择",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 标签输入
 */
@Composable
private fun TagInput(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            label = { Text("添加标签") },
            placeholder = { Text("输入标签后按回车") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                if (tagInput.isNotBlank()) {
                    TextButton(
                        onClick = {
                            if (tagInput.isNotBlank() && !tags.contains(tagInput.trim())) {
                                onTagsChanged(tags + tagInput.trim())
                                tagInput = ""
                            }
                        }
                    ) {
                        Text("添加")
                    }
                }
            }
        )
        
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            TagChipGroup(
                tags = tags,
                onTagRemoved = { tag ->
                    onTagsChanged(tags - tag)
                }
            )
        }
    }
}

/**
 * 标签芯片组
 */
@Composable
private fun TagChipGroup(
    tags: List<String>,
    onTagRemoved: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = false,
                onClick = { onTagRemoved(tag) },
                label = { Text(tag) },
                trailingIcon = {
                    Text("×", style = MaterialTheme.typography.titleMedium)
                }
            )
        }
    }
}

/**
 * 优先级选择器
 */
@Composable
private fun PrioritySelector(
    priority: Int,
    onPrioritySelected: (Int) -> Unit
) {
    Column {
        Text(
            text = "优先级",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) { index ->
                FilterChip(
                    selected = priority == index,
                    onClick = { onPrioritySelected(index) },
                    label = {
                        Text(
                            when (index) {
                                0 -> "低"
                                1 -> "较低"
                                2 -> "中"
                                3 -> "较高"
                                4 -> "高"
                                else -> ""
                            }
                        )
                    }
                )
            }
        }
    }
}