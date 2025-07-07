package com.ccxiaoji.feature.ledger.presentation.screen.savings.dialogs

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.theme.DesignTokens
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 扁平化储蓄目标对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatSavingsGoalDialog(
    goal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Double, targetDate: LocalDate?, description: String?, color: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var selectedDate by remember { mutableStateOf(goal?.targetDate) }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var selectedIcon by remember { mutableStateOf(goal?.iconName ?: "savings") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // 使用Material主题颜色而非硬编码颜色
    val colorOptions = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
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
    
    FlatDialog(
        title = if (goal == null) "创建储蓄目标" else "编辑储蓄目标",
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                when {
                    name.isBlank() -> nameError = "请输入目标名称"
                    targetAmount.toDoubleOrNull() == null || targetAmount.toDouble() <= 0 -> {
                        amountError = "请输入有效金额"
                    }
                    else -> {
                        onConfirm(
                            name,
                            targetAmount.toDouble(),
                            selectedDate,
                            description.ifBlank { null },
                            "#${colorOptions[selectedColorIndex].value.toString(16).uppercase()}",
                            selectedIcon
                        )
                    }
                }
            }) {
                Text(if (goal == null) "创建" else "更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 名称输入
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    nameError = null
                },
                label = { Text("目标名称") },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )
            
            // 目标金额输入
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { 
                    targetAmount = it.filter { char -> char.isDigit() || char == '.' }
                    amountError = null
                },
                label = { Text("目标金额") },
                prefix = { Text("¥") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                isError = amountError != null,
                supportingText = amountError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                )
            )
            
            // 目标日期
            OutlinedTextField(
                value = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) ?: "",
                onValueChange = { },
                label = { Text("目标日期 (可选)") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
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
                value = description,
                onValueChange = { description = it },
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
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(icons) { (iconName, icon) ->
                        IconOption(
                            icon = icon,
                            isSelected = selectedIcon == iconName,
                            color = colorOptions[selectedColorIndex],
                            onClick = { selectedIcon = iconName }
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
                    items(colorOptions.indices.toList()) { index ->
                        ColorOption(
                            color = colorOptions[index],
                            isSelected = selectedColorIndex == index,
                            onClick = { selectedColorIndex = index }
                        )
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerModal(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * 图标选项
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
 * 颜色选项
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

/**
 * 日期选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(date)
                    } ?: onDateSelected(null)
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