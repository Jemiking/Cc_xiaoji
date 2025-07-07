package com.ccxiaoji.feature.plan.presentation.screen.template

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.ccxiaoji.feature.plan.presentation.components.DatePickerField
import com.ccxiaoji.feature.plan.presentation.viewmodel.ApplyTemplateViewModel
import com.ccxiaoji.ui.theme.DesignTokens
import kotlinx.datetime.*

/**
 * 应用模板页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyTemplateScreen(
    navController: NavController,
    templateId: String,
    viewModel: ApplyTemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }
    
    // 处理父计划选择结果
    navController.currentBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val parentIdObserver = Observer<String> { parentId ->
                viewModel.updateParentPlanId(parentId)
                savedStateHandle.remove<String>("selected_parent_id")
            }
            
            savedStateHandle.getLiveData<String>("selected_parent_id").observe(lifecycleOwner, parentIdObserver)
            
            onDispose {
                savedStateHandle.getLiveData<String>("selected_parent_id").removeObserver(parentIdObserver)
            }
        }
    }
    
    // 处理应用成功后的导航
    LaunchedEffect(uiState.appliedPlanId) {
        uiState.appliedPlanId?.let { planId ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("applied_plan_id", planId)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("应用模板") },
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
        uiState.template?.let { template ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(DesignTokens.Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
            ) {
                // 模板信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignTokens.Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.small)
                    ) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Spacing.medium)
                        ) {
                            Text(
                                text = "预计天数：${template.duration}天",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "包含计划：${template.structure.totalPlanCount}个",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // 计划标题
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("计划标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(template.title) },
                    enabled = !uiState.isLoading,
                    colors = OutlinedTextFieldDefaults.colors(),
                    shape = MaterialTheme.shapes.small
                )
                
                // 开始日期
                DatePickerField(
                    date = uiState.startDate,
                    onDateChange = viewModel::updateStartDate,
                    label = "开始日期",
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 父计划选择
                OutlinedCard(
                    onClick = { 
                        if (!uiState.isLoading) {
                            navController.navigate(
                                "parent_plan_selection?selectedParentId=${uiState.parentPlanId ?: ""}"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignTokens.Spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.parentPlanId != null) {
                                "已选择父计划"
                            } else {
                                "选择父计划（可选）"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "选择",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 预计结束日期
                val endDate = uiState.startDate.plus(DatePeriod(days = template.duration))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = "预计结束日期：$endDate",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(DesignTokens.Spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        onClick = { viewModel.applyTemplate() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.title.isNotBlank() && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("应用模板")
                        }
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