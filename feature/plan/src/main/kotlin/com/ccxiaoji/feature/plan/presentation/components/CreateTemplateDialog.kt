package com.ccxiaoji.feature.plan.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory

/**
 * 从计划创建模板对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateDialog(
    planTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        category: TemplateCategory,
        tags: List<String>,
        isPublic: Boolean
    ) -> Unit
) {
    // 表单状态
    var title by remember { mutableStateOf(planTitle + " 模板") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TemplateCategory.OTHER) }
    var tagsText by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    // 验证状态
    val isValid = title.isNotBlank() && description.isNotBlank()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建模板") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("模板标题") },
                    placeholder = { Text("输入模板标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("模板描述") },
                    placeholder = { Text("输入模板描述，说明这个模板的用途") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // 分类选择
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("分类") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        TemplateCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
                
                // 标签输入
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("标签") },
                    placeholder = { Text("输入标签，用逗号分隔") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                
                // 公开选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "设为公开模板",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it }
                    )
                }
                
                Text(
                    text = "提示：创建的模板将包含当前计划的所有子计划和里程碑结构",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val tags = tagsText
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    
                    onConfirm(
                        title,
                        description,
                        selectedCategory,
                        tags,
                        isPublic
                    )
                },
                enabled = isValid
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}