package com.ccxiaoji.feature.ledger.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.api.SavingsGoalItem
import com.ccxiaoji.feature.ledger.presentation.ui.components.color.ColorPicker
import com.ccxiaoji.feature.ledger.presentation.ui.components.icon.IconPicker
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalDialog(
    goal: SavingsGoalItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        targetAmount: Double,
        targetDate: LocalDate?,
        description: String?,
        color: String,
        iconName: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmountYuan?.toString() ?: "") }
    var targetDate by remember { mutableStateOf(goal?.targetDate) }
    var description by remember { mutableStateOf(goal?.description ?: "") }
    var selectedColor by remember { mutableStateOf(goal?.color ?: "#4CAF50") }
    var selectedIconName by remember { mutableStateOf(goal?.iconName ?: "savings") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }
    
    val isEditing = goal != null
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "编辑储蓄目标" else "创建储蓄目标") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("目标名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { value ->
                        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            targetAmount = value
                        }
                    },
                    label = { Text("目标金额") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("¥") },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = targetDate?.toJavaLocalDate()?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) ?: "选择目标日期（可选）",
                    onValueChange = { },
                    label = { Text("目标日期") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "选择日期")
                        }
                    },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedCard(
                        onClick = { showColorPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = MaterialTheme.shapes.small,
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(selectedColor))
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择颜色")
                        }
                    }
                    
                    OutlinedCard(
                        onClick = { showIconPicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getIconForGoal(selectedIconName),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选择图标")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        onConfirm(
                            name,
                            amount,
                            targetDate,
                            description.ifBlank { null },
                            selectedColor,
                            selectedIconName
                        )
                    }
                },
                enabled = name.isNotBlank() && targetAmount.toDoubleOrNull() != null && targetAmount.toDouble() > 0
            ) {
                Text(if (isEditing) "保存" else "创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = targetDate,
            onDateSelected = { date ->
                targetDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    if (showColorPicker) {
        ColorPicker(
            selectedColor = selectedColor,
            onColorSelected = { color ->
                selectedColor = color
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
    
    if (showIconPicker) {
        IconPicker(
            selectedIcon = selectedIconName,
            onIconSelected = { iconName ->
                selectedIconName = iconName
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toEpochDays()?.times(24 * 60 * 60 * 1000L)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择目标日期") },
        text = {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val localDate = java.time.Instant.ofEpochMilli(selectedDateMillis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate.toKotlinLocalDate())
                    } else {
                        onDateSelected(null)
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
    )
}

private fun getIconForGoal(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "house" -> Icons.Default.Home
        "car" -> Icons.Default.DirectionsCar
        "vacation" -> Icons.Default.BeachAccess
        "education" -> Icons.Default.School
        "emergency" -> Icons.Default.LocalHospital
        "shopping" -> Icons.Default.ShoppingCart
        "gift" -> Icons.Default.CardGiftcard
        "phone" -> Icons.Default.PhoneAndroid
        "computer" -> Icons.Default.Computer
        "camera" -> Icons.Default.CameraAlt
        else -> Icons.Default.Savings
    }
}