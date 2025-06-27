package com.ccxiaoji.app.presentation.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.ledger.domain.model.SavingsGoal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalDialog(
    goal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, targetAmount: Double, targetDate: LocalDate?, description: String?, color: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var selectedDate by remember { mutableStateOf(goal?.targetDate) }
    var selectedColor by remember { mutableStateOf(goal?.color ?: "#4CAF50") }
    var selectedIcon by remember { mutableStateOf(goal?.iconName ?: "savings") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val colors = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#9C27B0",
        "#F44336", "#00BCD4", "#FFC107", "#E91E63",
        "#3F51B5", "#009688", "#795548", "#607D8B"
    )
    
    val icons = listOf(
        "savings" to Icons.Default.Savings,
        "house" to Icons.Default.Home,
        "car" to Icons.Default.DirectionsCar,
        "vacation" to Icons.Default.BeachAccess,
        "education" to Icons.Default.School,
        "emergency" to Icons.Default.LocalHospital,
        "shopping" to Icons.Default.ShoppingCart,
        "gift" to Icons.Default.CardGiftcard,
        "phone" to Icons.Default.PhoneAndroid,
        "computer" to Icons.Default.Computer,
        "camera" to Icons.Default.CameraAlt
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = if (goal == null) "创建储蓄目标" else "编辑储蓄目标",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Name input
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
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Target amount input
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
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Target date
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
                        .clickable { showDatePicker = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述 (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Icon selection
                Text(
                    text = "选择图标",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(icons) { (iconName, icon) ->
                        IconOption(
                            icon = icon,
                            isSelected = selectedIcon == iconName,
                            color = Color(android.graphics.Color.parseColor(selectedColor)),
                            onClick = { selectedIcon = iconName }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Color selection
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colors) { color ->
                        ColorOption(
                            color = Color(android.graphics.Color.parseColor(color)),
                            isSelected = selectedColor == color,
                            onClick = { selectedColor = color }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
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
                                        selectedColor,
                                        selectedIcon
                                    )
                                }
                            }
                        }
                    ) {
                        Text(if (goal == null) "创建" else "更新")
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

@Composable
private fun IconOption(
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
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

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

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