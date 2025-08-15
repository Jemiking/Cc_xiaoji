package com.ccxiaoji.feature.ledger.presentation.screen.import

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.viewmodel.QianjiImportViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * 钱迹数据导入页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QianjiImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: QianjiImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 使用LaunchedEffect避免重组循环
    LaunchedEffect(uiState.isImporting, uiState.importComplete) {
        android.util.Log.e("QIANJI_DEBUG", "========== 导入状态变化 ==========")
        android.util.Log.e("QIANJI_DEBUG", "isImporting=${uiState.isImporting}, importComplete=${uiState.importComplete}")
        android.util.Log.e("QIANJI_DEBUG", "selectedFile=${uiState.selectedFile}")
        if (uiState.importComplete) {
            android.util.Log.e("QIANJI_DEBUG", "导入完成消息: ${uiState.resultMessage}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入钱迹数据") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.selectedFile != null && !uiState.isImporting) {
                        TextButton(
                            onClick = { viewModel.reset() }
                        ) {
                            Text("重新选择")
                        }
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
            when {
                uiState.importComplete -> {
                    // 导入完成
                    ImportCompleteContent(
                        resultMessage = uiState.resultMessage,
                        onDone = onNavigateBack
                    )
                }
                
                uiState.isImporting -> {
                    // 导入中
                    ImportingContent(
                        progress = uiState.importProgress,
                        progressMessage = uiState.progressMessage
                    )
                }
                
                uiState.previewData.isNotEmpty() -> {
                    // 预览数据
                    PreviewContent(
                        previewData = uiState.previewData,
                        importOptions = uiState.importOptions,
                        onOptionsChange = viewModel::updateOptions,
                        onStartImport = viewModel::startImport,
                        onReset = viewModel::reset
                    )
                }
                
                else -> {
                    // 选择文件
                    FileSelectionContent(
                        isLoading = uiState.isLoading,
                        onFileSelected = viewModel::selectFile
                    )
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
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
}

/**
 * 文件选择界面
 */
@Composable
private fun FileSelectionContent(
    isLoading: Boolean,
    onFileSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        android.util.Log.e("QIANJI_DEBUG", "文件选择结果, uri: $uri")
        if (uri == null) {
            android.util.Log.e("QIANJI_DEBUG", "URI为空，用户可能取消了选择")
        } else {
            android.util.Log.e("QIANJI_DEBUG", "URI详情: scheme=${uri.scheme}, authority=${uri.authority}")
            android.util.Log.e("QIANJI_DEBUG", "URI路径=${uri.path}, lastPathSegment=${uri.lastPathSegment}")
            onFileSelected(uri)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在分析钱迹数据...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "导入钱迹数据",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "支持钱迹APP导出的CSV文件\n文件名格式：QianJi_*.csv",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    android.util.Log.e("QIANJI_DEBUG", "点击选择文件按钮")
                    val mimeTypes = arrayOf("text/csv", "text/plain", "text/*", "*/*")
                    android.util.Log.e("QIANJI_DEBUG", "启动文件选择器，MIME类型: ${mimeTypes.joinToString()}")
                    try {
                        launcher.launch(mimeTypes)
                        android.util.Log.e("QIANJI_DEBUG", "文件选择器启动成功")
                    } catch (e: Exception) {
                        android.util.Log.e("QIANJI_DEBUG", "文件选择器启动失败: ${e.message}", e)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择钱迹CSV文件")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 导入说明
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "导入说明",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• 将自动识别钱迹的分类和账户\n" +
                                      "• 智能映射到CC小记的分类体系\n" +
                                      "• 自动创建不存在的分类和账户\n" +
                                      "• 支持跳过重复数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 钱迹导出步骤
                    Text(
                        text = "如何从钱迹导出数据？",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. 打开钱迹APP\n" +
                              "2. 进入「设置」→「数据备份」\n" +
                              "3. 选择「导出数据」\n" +
                              "4. 选择CSV格式导出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * 数据预览界面
 */
@Composable
private fun PreviewContent(
    previewData: List<Any>,
    importOptions: QianjiImportViewModel.ImportOptions,
    onOptionsChange: (QianjiImportViewModel.ImportOptions) -> Unit,
    onStartImport: () -> Unit,
    onReset: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 预览数据
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "数据预览（前100条）",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(previewData.take(100)) { item ->
                        // 这里需要根据实际的数据结构来显示
                        // 暂时用Card展示
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "交易数据",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 导入选项
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "导入选项",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 跳过重复数据
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("跳过重复数据")
                    Switch(
                        checked = importOptions.skipDuplicates,
                        onCheckedChange = { 
                            onOptionsChange(importOptions.copy(skipDuplicates = it))
                        }
                    )
                }
                
                // 自动创建分类
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("自动创建分类")
                    Switch(
                        checked = importOptions.createCategories,
                        onCheckedChange = { 
                            onOptionsChange(importOptions.copy(createCategories = it))
                        }
                    )
                }
                
                // 自动创建账户
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("自动创建账户")
                    Switch(
                        checked = importOptions.createAccounts,
                        onCheckedChange = { 
                            onOptionsChange(importOptions.copy(createAccounts = it))
                        }
                    )
                }
                
                // 合并二级分类
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("合并二级分类")
                    Switch(
                        checked = importOptions.mergeSubCategories,
                        onCheckedChange = { 
                            onOptionsChange(importOptions.copy(mergeSubCategories = it))
                        }
                    )
                }
                
                // 处理退款
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("处理退款关联")
                    Switch(
                        checked = importOptions.handleRefunds,
                        onCheckedChange = { 
                            onOptionsChange(importOptions.copy(handleRefunds = it))
                        }
                    )
                }
            }
        }
        
        // 操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("重新选择")
            }
            
            Button(
                onClick = onStartImport,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始导入")
            }
        }
    }
}

/**
 * 导入中界面
 */
@Composable
private fun ImportingContent(
    progress: Float,
    progressMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "正在导入数据",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = progressMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 导入完成界面
 */
@Composable
private fun ImportCompleteContent(
    resultMessage: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "导入完成",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = resultMessage,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("完成")
        }
    }
}