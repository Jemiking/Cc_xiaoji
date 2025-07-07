package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.feature.plan.presentation.screen.create.components.*
import com.ccxiaoji.feature.plan.presentation.screen.edit.components.*
import com.ccxiaoji.feature.plan.presentation.viewmodel.EditPlanViewModel
import com.ccxiaoji.ui.components.FlatButton
import com.ccxiaoji.ui.components.FlatDialog
import com.ccxiaoji.ui.components.ModernCard
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*

/**
 * 编辑计划页面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPlanScreen(
    planId: String,
    onBackClick: () -> Unit,
    onPlanUpdated: () -> Unit,
    viewModel: EditPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载计划数据
    LaunchedEffect(planId) {
        viewModel.loadPlan(planId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑计划") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    FlatButton(
                        onClick = { viewModel.updatePlan() },
                        enabled = uiState.isValid && !uiState.isLoading,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = DesignTokens.Spacing.small)
                    ) {
                        Text("保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 基本信息卡片
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
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
                            isError = uiState.titleError != null,
                            supportingText = uiState.titleError?.let { { Text(it) } },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                        )
                        
                        // 描述输入
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = viewModel::updateDescription,
                            label = { Text("计划描述") },
                            placeholder = { Text("输入计划描述（可选）") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(DesignTokens.BorderRadius.medium)
                        )
                    }
                }
                
                // 日期选择卡片
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        // 开始日期
                        FlatDatePickerField(
                            label = "开始日期 *",
                            date = uiState.startDate,
                            onDateSelected = viewModel::updateStartDate,
                            isError = uiState.startDateError != null,
                            errorMessage = uiState.startDateError,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 结束日期
                        FlatDatePickerField(
                            label = "结束日期 *",
                            date = uiState.endDate,
                            onDateSelected = viewModel::updateEndDate,
                            isError = uiState.endDateError != null,
                            errorMessage = uiState.endDateError,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // 状态和进度卡片
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        // 状态选择
                        StatusSelector(
                            status = uiState.status,
                            onStatusSelected = viewModel::updateStatus
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        
                        // 进度更新
                        ProgressSlider(
                            progress = uiState.progress,
                            onProgressChanged = viewModel::updateProgress,
                            enabled = !uiState.hasChildren // 有子计划时禁止手动修改进度
                        )
                    }
                }
                
                // 样式设置卡片
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                    ) {
                        // 颜色选择
                        ColorSelector(
                            selectedColor = uiState.color,
                            onColorSelected = viewModel::updateColor
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        
                        // 优先级选择
                        PrioritySelector(
                            priority = uiState.priority,
                            onPrioritySelected = viewModel::updatePriority
                        )
                    }
                }
                
                // 标签输入卡片
                ModernCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium)
                    ) {
                        TagInput(
                            tags = uiState.tags,
                            onTagsChanged = viewModel::updateTags,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // 处理更新成功
    LaunchedEffect(uiState.isUpdated) {
        if (uiState.isUpdated) {
            onPlanUpdated()
        }
    }
    
    // 显示错误对话框
    uiState.error?.let { error ->
        FlatDialog(
            onDismissRequest = { viewModel.clearError() },
            title = "错误",
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        ) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}