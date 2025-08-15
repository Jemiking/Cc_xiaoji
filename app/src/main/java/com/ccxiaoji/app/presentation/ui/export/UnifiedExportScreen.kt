package com.ccxiaoji.app.presentation.ui.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.ui.export.components.ExportProgressDialog
import com.ccxiaoji.app.presentation.ui.export.components.ExportSettingsCard
import com.ccxiaoji.app.presentation.ui.export.components.ExportSuccessDialog
import com.ccxiaoji.app.presentation.ui.export.components.ModuleExportCard

/**
 * 统一数据导出界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedExportScreen(
    navController: NavController,
    viewModel: UnifiedExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导出") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        bottomBar = {
            // 导出按钮
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportData() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedModules.isNotEmpty() && !uiState.isExporting
                    ) {
                        Text(
                            text = if (uiState.selectedModules.isEmpty()) {
                                "请选择要导出的模块"
                            } else {
                                "导出选中的数据"
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 模块选择标题
            item {
                Text(
                    text = "选择要导出的数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 模块选择卡片
            items(ExportModule.values().toList()) { module ->
                ModuleExportCard(
                    module = module,
                    stats = uiState.moduleStats[module],
                    isSelected = module in uiState.selectedModules,
                    onSelectionChange = { viewModel.toggleModule(module) }
                )
            }
            
            // 导出设置
            item {
                ExportSettingsCard(
                    dateRange = uiState.dateRange,
                    onDateRangeChange = { viewModel.setDateRange(it) },
                    exportFormat = uiState.exportFormat,
                    onExportFormatChange = { viewModel.setExportFormat(it) }
                )
            }
            
            // 导出说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                            Text(
                                text = "导出说明",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• 记账数据包含所有账户、交易、预算等信息\n" +
                                      "• 其他模块功能正在开发中，敬请期待\n" +
                                      "• 导出的文件可通过系统分享功能发送",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 导出进度对话框
    if (uiState.isExporting) {
        ExportProgressDialog(
            progress = uiState.exportProgress,
            currentModule = uiState.currentExportingModule,
            onDismiss = { /* 不可关闭 */ }
        )
    }
    
    // 导出成功对话框
    uiState.exportResult?.let { result ->
        if (result.success) {
            ExportSuccessDialog(
                result = result,
                onDismiss = { viewModel.clearExportResult() }
            )
        }
    }
    
    // 错误提示
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("提示") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}