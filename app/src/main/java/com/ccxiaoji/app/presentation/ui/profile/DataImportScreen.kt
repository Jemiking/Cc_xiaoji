package com.ccxiaoji.app.presentation.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ccxiaoji.app.presentation.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectFile(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导入") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 文件选择区域
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "选择文件",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 文件选择框
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { filePickerLauncher.launch("application/json") },
                            contentAlignment = Alignment.Center
                        ) {
                            val selectedFileName = uiState.selectedFileName
                            if (selectedFileName != null) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedFileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "点击重新选择",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "点击选择导入文件",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "支持 JSON 格式",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        // 清除文件按钮
                        if (uiState.selectedFileName != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { viewModel.clearFile() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("清除文件")
                            }
                        }
                    }
                }
            }
            
            // 验证结果
            val validationResult = uiState.validationResult
            if (validationResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (validationResult.isValid) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (validationResult.isValid) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = if (validationResult.isValid) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (validationResult.isValid) {
                                        "文件验证通过"
                                    } else {
                                        "文件验证失败"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // 显示错误信息
                            if (validationResult.errors.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                validationResult.errors.forEach { error ->
                                    Text(
                                        text = "• ${error.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            // 显示警告信息
                            if (validationResult.warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                validationResult.warnings.forEach { warning ->
                                    Text(
                                        text = "• ${warning.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 数据预览
                if (validationResult.isValid) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "数据预览",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val preview = validationResult.dataPreview
                                
                                // 记账数据
                                if (preview.transactionCount > 0 || preview.accountCount > 0 || preview.categoryCount > 0) {
                                    DataPreviewItem(
                                        icon = Icons.Default.AccountBalance,
                                        title = "记账数据",
                                        items = listOf(
                                            "交易记录" to "${preview.transactionCount}条",
                                            "账户" to "${preview.accountCount}个",
                                            "分类" to "${preview.categoryCount}个"
                                        )
                                    )
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                
                                // 待办数据
                                if (preview.taskCount > 0) {
                                    DataPreviewItem(
                                        icon = Icons.Default.Task,
                                        title = "待办任务",
                                        items = listOf("任务" to "${preview.taskCount}个")
                                    )
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                
                                // 习惯数据
                                if (preview.habitCount > 0) {
                                    DataPreviewItem(
                                        icon = Icons.Default.FitnessCenter,
                                        title = "习惯数据",
                                        items = listOf("习惯" to "${preview.habitCount}个")
                                    )
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                
                                // 其他数据
                                if (preview.budgetCount > 0 || preview.savingsGoalCount > 0 || preview.countdownCount > 0) {
                                    DataPreviewItem(
                                        icon = Icons.Default.MoreHoriz,
                                        title = "其他数据",
                                        items = listOfNotNull(
                                            if (preview.budgetCount > 0) "预算" to "${preview.budgetCount}个" else null,
                                            if (preview.savingsGoalCount > 0) "储蓄目标" to "${preview.savingsGoalCount}个" else null,
                                            if (preview.countdownCount > 0) "倒计时" to "${preview.countdownCount}个" else null
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 导入选项
            val currentValidationResult = uiState.validationResult
            if (currentValidationResult?.isValid == true) {
                // 模块选择
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "选择要导入的模块",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            val dataPreview = currentValidationResult.dataPreview
                            
                            ImportModuleItem(
                                icon = Icons.Default.AccountBalance,
                                title = "记账数据",
                                checked = ImportModule.LEDGER in uiState.selectedModules,
                                enabled = dataPreview.transactionCount > 0,
                                onCheckedChange = { viewModel.toggleModule(ImportModule.LEDGER) }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            ImportModuleItem(
                                icon = Icons.Default.Task,
                                title = "待办任务",
                                checked = ImportModule.TODO in uiState.selectedModules,
                                enabled = dataPreview.taskCount > 0,
                                onCheckedChange = { viewModel.toggleModule(ImportModule.TODO) }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            ImportModuleItem(
                                icon = Icons.Default.FitnessCenter,
                                title = "习惯数据",
                                checked = ImportModule.HABIT in uiState.selectedModules,
                                enabled = dataPreview.habitCount > 0,
                                onCheckedChange = { viewModel.toggleModule(ImportModule.HABIT) }
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            ImportModuleItem(
                                icon = Icons.Default.MoreHoriz,
                                title = "其他数据",
                                checked = ImportModule.OTHERS in uiState.selectedModules,
                                enabled = dataPreview.let {
                                    it.budgetCount > 0 || it.savingsGoalCount > 0 || it.countdownCount > 0
                                },
                                onCheckedChange = { viewModel.toggleModule(ImportModule.OTHERS) }
                            )
                        }
                    }
                }
                
                // 冲突处理策略
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "冲突处理",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.conflictResolution == ConflictResolution.SKIP,
                                    onClick = { viewModel.setConflictResolution(ConflictResolution.SKIP) },
                                    label = { Text("跳过") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = uiState.conflictResolution == ConflictResolution.REPLACE,
                                    onClick = { viewModel.setConflictResolution(ConflictResolution.REPLACE) },
                                    label = { Text("替换") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = uiState.conflictResolution == ConflictResolution.CREATE_NEW,
                                    onClick = { viewModel.setConflictResolution(ConflictResolution.CREATE_NEW) },
                                    label = { Text("新建") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (uiState.conflictResolution) {
                                    ConflictResolution.SKIP -> "遇到重复数据时跳过不导入"
                                    ConflictResolution.REPLACE -> "遇到重复数据时替换现有数据"
                                    ConflictResolution.CREATE_NEW -> "遇到重复数据时创建新记录"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 导入进度
            val importProgress = uiState.importProgress
            if (uiState.isImporting && importProgress != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "导入进度",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "正在导入：${importProgress.currentModule}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LinearProgressIndicator(
                                progress = importProgress.progress,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${importProgress.successCount}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "成功",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${importProgress.skipCount}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Text(
                                        text = "跳过",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${importProgress.errorCount}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "失败",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // 导入结果
            val importResult = uiState.importResult
            if (importResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (importResult.success) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (importResult.success) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.Error
                                    },
                                    contentDescription = null,
                                    tint = if (importResult.success) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = importResult.message,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (importResult.success) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${importResult.successCount}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "成功导入",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${importResult.skipCount}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "跳过",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${importResult.errorCount}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "失败",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // 导入按钮
            if (uiState.canImport && uiState.importResult == null) {
                item {
                    Button(
                        onClick = { viewModel.startImport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isImporting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始导入")
                    }
                }
            }
        }
    }
}

@Composable
private fun DataPreviewItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    items: List<Pair<String, String>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            items.forEach { (label, value) ->
                Text(
                    text = "$label: $value",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ImportModuleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                if (!enabled) {
                    Text(
                        text = "无可导入数据",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}