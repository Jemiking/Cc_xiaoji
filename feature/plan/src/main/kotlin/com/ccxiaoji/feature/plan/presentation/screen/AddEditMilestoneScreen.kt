package com.ccxiaoji.feature.plan.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ccxiaoji.feature.plan.presentation.components.DatePickerField
import com.ccxiaoji.feature.plan.presentation.viewmodel.AddEditMilestoneViewModel

/**
 * 添加/编辑里程碑页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMilestoneScreen(
    planId: String,
    milestoneId: String? = null,
    navController: NavController,
    viewModel: AddEditMilestoneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 处理保存成功
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("milestone_updated", true)
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (milestoneId == null) "添加里程碑" else "编辑里程碑") 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveMilestone() },
                        enabled = !uiState.isLoading && uiState.titleError == null && uiState.title.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 里程碑标题输入
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("里程碑标题 *") },
                placeholder = { Text("例如：完成第一阶段") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading
            )
            
            // 描述输入
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("描述（可选）") },
                placeholder = { Text("详细描述这个里程碑...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text("${uiState.description.length}/500")
                },
                enabled = !uiState.isLoading
            )
            
            // 目标日期选择
            DatePickerField(
                label = "目标日期",
                date = uiState.targetDate,
                onDateChange = viewModel::updateTargetDate
            )
            
            // 错误提示
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // 加载中指示器
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}