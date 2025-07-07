package com.ccxiaoji.feature.ledger.presentation.screen.budget.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.ledger.data.local.dao.BudgetWithSpent
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.ui.theme.DesignTokens
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.FlatBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlatBudgetDialog(
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
    val selectedCategory = categories.find { it.id == selectedCategoryId }
    
    FlatDialog(
        onDismissRequest = onDismiss,
        title = if (isEditMode) "编辑预算" else "添加预算",
        confirmButton = {
            FlatButton(
                text = "保存",
                onClick = {
                    val amount = (amountText.toDoubleOrNull() ?: 0.0) * 100
                    onSave(amount.toInt(), selectedCategoryId, alertThreshold, note.ifBlank { null })
                },
                enabled = amountText.toDoubleOrNull() != null && amountText.toDouble() > 0,
                backgroundColor = DesignTokens.BrandColors.Ledger
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 分类选择
            if (selectedCategoryId == null) {
                Text(
                    text = "预算类型",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCategoryId = null },
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                        color = if (selectedCategoryId == null) {
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = if (selectedCategoryId == null) {
                            BorderStroke(1.dp, DesignTokens.BrandColors.Ledger.copy(alpha = 0.5f))
                        } else null
                    ) {
                        Text(
                            text = "总预算",
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.Spacing.medium,
                                vertical = DesignTokens.Spacing.small
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedCategoryId == null) FontWeight.Medium else null,
                            color = if (selectedCategoryId == null) {
                                DesignTokens.BrandColors.Ledger
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showCategoryPicker = true },
                        shape = RoundedCornerShape(DesignTokens.BorderRadius.medium),
                        color = if (selectedCategoryId != null) {
                            DesignTokens.BrandColors.Ledger.copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    ) {
                        Text(
                            text = selectedCategory?.name ?: "分类预算",
                            modifier = Modifier.padding(
                                horizontal = DesignTokens.Spacing.medium,
                                vertical = DesignTokens.Spacing.small
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (selectedCategoryId != null) FontWeight.Medium else null,
                            color = if (selectedCategoryId != null) {
                                DesignTokens.BrandColors.Ledger
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
            
            // 金额输入
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("预算金额") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("¥") },
                colors = OutlinedTextFieldDefaults.colors(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                singleLine = true
            )
            
            // 预警阈值
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "预警阈值",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(alertThreshold * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = DesignTokens.BrandColors.Warning
                    )
                }
                Slider(
                    value = alertThreshold,
                    onValueChange = { alertThreshold = it },
                    valueRange = 0.5f..0.95f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = DesignTokens.BrandColors.Warning,
                        activeTrackColor = DesignTokens.BrandColors.Warning,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                    )
                )
            }
            
            // 备注输入
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = RoundedCornerShape(DesignTokens.BorderRadius.small),
                maxLines = 2
            )
        }
    }
    
    // 分类选择底部抽屉
    if (showCategoryPicker) {
        FlatBottomSheet(
            onDismissRequest = { showCategoryPicker = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignTokens.Spacing.medium)
            ) {
                Text(
                    text = "选择分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                )
                
                categories.filter { it.type == "EXPENSE" }.forEach { category ->
                    val categoryColor = when (category.type) {
                        "INCOME" -> DesignTokens.BrandColors.Success
                        "EXPENSE" -> DesignTokens.BrandColors.Error
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategoryId = category.id
                                showCategoryPicker = false
                            }
                            .padding(
                                horizontal = DesignTokens.Spacing.medium,
                                vertical = DesignTokens.Spacing.small
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(categoryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.icon,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(DesignTokens.Spacing.medium))
                        
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (selectedCategoryId == category.id) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = DesignTokens.BrandColors.Ledger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}