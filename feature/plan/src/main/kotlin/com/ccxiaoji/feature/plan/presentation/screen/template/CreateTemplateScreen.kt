package com.ccxiaoji.feature.plan.presentation.screen.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.feature.plan.presentation.viewmodel.CreateTemplateViewModel
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 创建模板页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplateScreen(
    navController: NavController,
    planId: String,
    viewModel: CreateTemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isTemplateCreated) {
        if (uiState.isTemplateCreated) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("template_created", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("创建模板") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(DesignTokens.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
        ) {
            // 提示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "提示：创建的模板将包含当前计划的所有子计划和里程碑结构",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(DesignTokens.Spacing.medium)
                )
            }
            
            // 标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("模板标题") },
                placeholder = { Text("输入模板标题") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = MaterialTheme.shapes.small,
                isError = uiState.titleError != null,
                supportingText = if (uiState.titleError != null) {
                    { Text(uiState.titleError!!, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            // 描述输入
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("模板描述") },
                placeholder = { Text("输入模板描述，说明这个模板的用途") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = !uiState.isLoading,
                colors = OutlinedTextFieldDefaults.colors(),
                shape = MaterialTheme.shapes.small,
                isError = uiState.descriptionError != null,
                supportingText = if (uiState.descriptionError != null) {
                    { Text(uiState.descriptionError!!, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            // 分类选择
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { if (!uiState.isLoading) showCategoryMenu = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedCategory.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("分类") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !uiState.isLoading,
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = MaterialTheme.shapes.small
                )
                
                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    TemplateCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                viewModel.updateCategory(category)
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }
            
            // 标签输入
            OutlinedTextField(
                value = uiState.tagsText,
                onValueChange = viewModel::updateTagsText,
                label = { Text("标签") },
                placeholder = { Text("输入标签，用逗号分隔") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(),
                shape = MaterialTheme.shapes.small
            )
            
            // 公开选项
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DesignTokens.Spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "设为公开模板",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "公开后其他用户可以使用此模板",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isPublic,
                        onCheckedChange = viewModel::updateIsPublic,
                        enabled = !uiState.isLoading
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("取消")
                }
                Button(
                    onClick = { viewModel.createTemplate() },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isValid && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("创建")
                    }
                }
            }
        }
        
        // 错误提示
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(DesignTokens.Spacing.medium),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("确定")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}