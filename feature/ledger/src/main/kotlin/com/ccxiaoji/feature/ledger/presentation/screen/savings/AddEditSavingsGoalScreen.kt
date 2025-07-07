package com.ccxiaoji.feature.ledger.presentation.screen.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.feature.ledger.presentation.viewmodel.AddEditSavingsGoalViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSavingsGoalScreen(
    navController: NavController,
    goalId: String? = null,
    viewModel: AddEditSavingsGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.previousBackStackEntry?.savedStateHandle?.set("savings_goal_updated", true)
            navController.popBackStack()
        }
    }
    
    // 处理错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (goalId == null) "创建储蓄目标" else "编辑储蓄目标",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 名称输入
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = { Text("目标名称") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
                
                // 目标金额输入
                OutlinedTextField(
                    value = uiState.targetAmount,
                    onValueChange = viewModel::updateTargetAmount,
                    label = { Text("目标金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = uiState.amountError != null,
                    supportingText = uiState.amountError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
                
                // 目标日期
                OutlinedTextField(
                    value = uiState.targetDate?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) ?: "",
                    onValueChange = { },
                    label = { Text("目标日期 (可选)") },
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (uiState.targetDate != null) {
                                IconButton(onClick = { viewModel.clearTargetDate() }) {
                                    Icon(Icons.Default.Clear, contentDescription = "清除日期")
                                }
                            }
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
                
                // 描述输入
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("描述 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                )
                
                // 图标选择
                Column {
                    Text(
                        text = "选择图标",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
                    )
                    
                    val icons = listOf(
                        "savings" to Icons.Default.Savings,
                        "house" to Icons.Default.Home,
                        "car" to Icons.Default.DirectionsCar,
                        "education" to Icons.Default.School,
                        "travel" to Icons.Default.Flight,
                        "phone" to Icons.Default.PhoneAndroid,
                        "medical" to Icons.Default.MedicalServices,
                        "shopping" to Icons.Default.ShoppingCart,
                        "emergency" to Icons.Default.Warning
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(icons) { (iconName, icon) ->
                            IconOption(
                                icon = icon,
                                isSelected = uiState.selectedIcon == iconName,
                                color = uiState.selectedColor,
                                onClick = { viewModel.updateIcon(iconName) }
                            )
                        }
                    }
                }
                
                // 颜色选择
                Column {
                    Text(
                        text = "选择颜色",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = DesignTokens.Spacing.small)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.colorOptions.indices.toList()) { index ->
                            ColorOption(
                                color = uiState.colorOptions[index],
                                isSelected = uiState.selectedColorIndex == index,
                                onClick = { viewModel.updateColorIndex(index) }
                            )
                        }
                    }
                }
                
                // 保存按钮
                Button(
                    onClick = viewModel::saveSavingsGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = DesignTokens.Spacing.medium),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (goalId == null) "创建" else "更新")
                    }
                }
            }
        }
    }
    
    // 日期选择器对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateTargetDate(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 图标选项组件
 */
@Composable
private fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) color.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 颜色选项组件
 */
@Composable
private fun ColorOption(
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}