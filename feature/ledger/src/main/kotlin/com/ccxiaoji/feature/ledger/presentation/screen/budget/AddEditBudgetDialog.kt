package com.ccxiaoji.feature.ledger.presentation.screen.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    editingBudget: BudgetWithSpent?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (budgetAmountCents: Int, categoryId: String?, alertThreshold: Float, note: String?) -> Unit
) {
    var selectedCategoryId by remember { 
        mutableStateOf(editingBudget?.categoryId)
    }
    var amountText by remember { 
        mutableStateOf(
            editingBudget?.let { 
                (it.budgetAmountCents / 100.0).toString() 
            } ?: ""
        )
    }
    var alertThreshold by remember { 
        mutableStateOf(editingBudget?.alertThreshold ?: 0.8f)
    }
    var note by remember { 
        mutableStateOf(editingBudget?.note ?: "")
    }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    val isEditMode = editingBudget != null
    val isTotalBudget = selectedCategoryId == null && (editingBudget == null || editingBudget.categoryId == null)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "编辑预算" else "添加预算",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 预算类型选择
                if (!isEditMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isTotalBudget,
                            onClick = { selectedCategoryId = null },
                            label = { Text("总预算") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isTotalBudget,
                            onClick = { showCategoryPicker = true },
                            label = { Text("分类预算") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 显示选中的分类
                if (!isTotalBudget) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    if (selectedCategory != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { if (!isEditMode) showCategoryPicker = true },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(selectedCategory.color)).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = selectedCategory.icon,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = selectedCategory.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // 预算金额输入
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // 只允许输入数字和小数点
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            amountText = newValue
                        }
                    },
                    label = { Text("预算金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 预警阈值
                Column {
                    Text(
                        text = "预警阈值: ${(alertThreshold * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = alertThreshold,
                        onValueChange = { alertThreshold = it },
                        valueRange = 0.5f..1f,
                        steps = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "当支出达到预算的${(alertThreshold * 100).toInt()}%时发出提醒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 备注
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                onSave(
                                    (amount * 100).toInt(),
                                    selectedCategoryId,
                                    alertThreshold,
                                    note.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amountText.isNotBlank() && 
                                amountText.toDoubleOrNull() != null && 
                                amountText.toDouble() > 0 &&
                                (isTotalBudget || selectedCategoryId != null)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // 分类选择器
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = {
                selectedCategoryId = it
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "选择分类",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                categories.forEach { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCategorySelected(category.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (category.id == selectedCategoryId) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.icon,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}