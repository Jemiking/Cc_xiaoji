package com.ccxiaoji.feature.ledger.presentation.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.DialogProperties
import com.ccxiaoji.feature.ledger.api.BudgetItem
import com.ccxiaoji.feature.ledger.api.CategoryItem

/**
 * 添加/编辑预算对话框
 * 支持创建总预算和分类预算，以及编辑现有预算
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    editingBudget: BudgetItem?,
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSave: (budgetAmountCents: Int, categoryId: String?, alertThreshold: Float, note: String?) -> Unit
) {
    var selectedCategoryId by remember { 
        mutableStateOf(editingBudget?.categoryId)
    }
    var amountText by remember { 
        mutableStateOf(
            editingBudget?.let { 
                it.budgetAmountYuan.toString() 
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
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
                
                // 预算类型选择（仅在新建时显示）
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
                
                // 选中的分类显示
                if (!isTotalBudget && selectedCategoryId != null) {
                    val selectedCategory = categories.find { it.id == selectedCategoryId }
                    selectedCategory?.let { category ->
                        CategoryItemCard(
                            category = category,
                            onClick = { showCategoryPicker = true }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                // 预算金额输入
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { 
                        // 只允许数字和小数点
                        if (it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amountText = it
                        }
                    },
                    label = { Text("预算金额") },
                    placeholder = { Text("请输入预算金额") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("单位：元") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 预警阈值设置
                Text(
                    text = "预警阈值: ${(alertThreshold * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = alertThreshold,
                    onValueChange = { alertThreshold = it },
                    valueRange = 0.5f..1.0f,
                    steps = 9, // 50%, 55%, 60%, ..., 95%, 100%
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "当支出达到预算的${(alertThreshold * 100).toInt()}%时会收到提醒",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 备注输入
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    placeholder = { Text("请输入备注信息") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 操作按钮
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
                                val amountCents = (amount * 100).toInt()
                                onSave(
                                    amountCents,
                                    selectedCategoryId,
                                    alertThreshold,
                                    note.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = amountText.toDoubleOrNull() != null && 
                                 amountText.toDoubleOrNull()!! > 0 &&
                                 (isTotalBudget || selectedCategoryId != null)
                    ) {
                        Text(if (isEditMode) "保存" else "添加")
                    }
                }
            }
        }
    }
    
    // 分类选择对话框
    if (showCategoryPicker) {
        CategoryPickerDialog(
            categories = categories.filter { it.type == "EXPENSE" }, // 只显示支出分类
            onDismiss = { showCategoryPicker = false },
            onSelect = { categoryId ->
                selectedCategoryId = categoryId
                showCategoryPicker = false
            }
        )
    }
}

/**
 * 分类项卡片
 */
@Composable
private fun CategoryItemCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                    ),
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

/**
 * 分类选择对话框
 */
@Composable
private fun CategoryPickerDialog(
    categories: List<CategoryItem>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "选择分类",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分类列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(category.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 分类图标
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Color(android.graphics.Color.parseColor(category.color)).copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category.icon,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}