package com.ccxiaoji.app.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.viewmodel.DataImportViewModel
import com.ccxiaoji.app.presentation.viewmodel.ImportStep
import com.ccxiaoji.common.data.import.DataModule
import com.ccxiaoji.common.data.import.ImportError
import com.ccxiaoji.common.data.import.ModuleImportResult

/**
 * 数据导入界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    navController: NavController,
    viewModel: DataImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectAndValidateFile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导入") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = uiState.importStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                },
                label = "import_step_animation"
            ) { step ->
                when (step) {
                    ImportStep.SELECT_FILE -> {
                        SelectFileStep(
                            onSelectFile = { filePickerLauncher.launch("application/json") }
                        )
                    }
                    ImportStep.PREVIEW -> {
                        PreviewStep(
                            uiState = uiState,
                            onToggleModule = viewModel::toggleModuleSelection,
                            onToggleSelectAll = viewModel::toggleSelectAll,
                            onUpdateConfig = viewModel::updateImportConfig,
                            onStartImport = viewModel::startImport,
                            onCancel = viewModel::goBack
                        )
                    }
                    ImportStep.IMPORTING -> {
                        ImportingStep()
                    }
                    ImportStep.RESULT -> {
                        ResultStep(
                            result = uiState.importResult,
                            onFinish = {
                                viewModel.reset()
                                navController.navigateUp()
                            },
                            onRetry = viewModel::reset
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.reset() }) {
                            Text("重试")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

/**
 * 选择文件步骤
 */
@Composable
private fun SelectFileStep(
    onSelectFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FileUpload,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "选择要导入的数据文件",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "支持从本应用导出的JSON格式文件",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Documents目录提示卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "建议从Download目录选择文件",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "导出功能会建议保存到Download目录，方便查找和管理",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择文件")
        }
    }
}

/**
 * 预览步骤
 */
@Composable
private fun PreviewStep(
    uiState: com.ccxiaoji.app.presentation.viewmodel.DataImportUiState,
    onToggleModule: (DataModule) -> Unit,
    onToggleSelectAll: () -> Unit,
    onUpdateConfig: (skipExisting: Boolean?, createBackup: Boolean?) -> Unit,
    onStartImport: () -> Unit,
    onCancel: () -> Unit
) {
    val validation = uiState.validation ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 文件信息卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "文件验证成功",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "文件大小: ${String.format("%.2f KB", validation.fileSize / 1024.0)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "数据模块: ${validation.dataModules.size} 个",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 错误信息
        if (validation.errors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            validation.errors.forEach { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 模块选择
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "选择要导入的模块",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onToggleSelectAll) {
                Text(
                    if (uiState.selectedModules.size == validation.dataModules.size) "取消全选" else "全选"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        validation.dataModules.forEach { module ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = { onToggleModule(module) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        module.displayName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Checkbox(
                        checked = uiState.selectedModules.contains(module),
                        onCheckedChange = { onToggleModule(module) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 导入选项
        Text(
            "导入选项",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "跳过已存在的数据",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "如果数据ID已存在，将跳过该条数据",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.importConfig.skipExisting,
                        onCheckedChange = { onUpdateConfig(it, null) }
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "导入前创建备份",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "在导入前自动备份当前数据库",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.importConfig.createBackup,
                        onCheckedChange = { onUpdateConfig(null, it) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("取消")
            }
            
            Button(
                onClick = onStartImport,
                modifier = Modifier.weight(1f),
                enabled = uiState.selectedModules.isNotEmpty()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始导入")
            }
        }
    }
}

/**
 * 导入中步骤
 */
@Composable
private fun ImportingStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "正在导入数据...",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "请勿关闭应用或进行其他操作",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 结果步骤
 */
@Composable
private fun ResultStep(
    result: com.ccxiaoji.common.data.import.ImportResult?,
    onFinish: () -> Unit,
    onRetry: () -> Unit
) {
    if (result == null) return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 结果摘要
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (result.success) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    if (result.success) "导入成功" else "导入失败",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 统计信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        label = "总计",
                        value = result.totalItems.toString()
                    )
                    StatisticItem(
                        label = "成功",
                        value = result.importedItems.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatisticItem(
                        label = "跳过",
                        value = result.skippedItems.toString(),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    if (result.hasErrors) {
                        StatisticItem(
                            label = "错误",
                            value = result.errors.size.toString(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (result.successRate < 1f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { result.successRate },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 模块详情
        Text(
            "导入详情",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        result.moduleResults.forEach { (module, moduleResult) ->
            ModuleResultCard(module, moduleResult)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 错误列表
        if (result.hasErrors) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "错误详情",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            result.errors.forEach { error ->
                ErrorCard(error)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 操作按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!result.success) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("重试")
                }
            }
            
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("完成")
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModuleResultCard(
    module: DataModule,
    result: ModuleImportResult
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    module.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "导入: ${result.importedItems} / ${result.totalItems}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (result.skippedItems > 0) {
                    Text(
                        "跳过: ${result.skippedItems}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Icon(
                if (result.errors.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (result.errors.isEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun ErrorCard(error: ImportError) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                if (error.code.isNotEmpty()) {
                    Text(
                        "错误代码: ${error.code}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    error.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}