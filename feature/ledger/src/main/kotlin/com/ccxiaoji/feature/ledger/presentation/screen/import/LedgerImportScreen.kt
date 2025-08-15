package com.ccxiaoji.feature.ledger.presentation.screen.import

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ImportViewModel
import com.ccxiaoji.feature.ledger.presentation.viewmodel.ImportStep

/**
 * 记账数据导入屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importConfig by viewModel.importConfig.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导入") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.currentStep != ImportStep.SELECT_FILE) {
                        TextButton(
                            onClick = { viewModel.reset() }
                        ) {
                            Text("重新开始")
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
            when (uiState.currentStep) {
                ImportStep.SELECT_FILE -> {
                    FileSelectionStep(
                        isLoading = uiState.isLoading,
                        onFileSelected = { uri ->
                            viewModel.selectFile(uri)
                        }
                    )
                }
                
                ImportStep.PREVIEW -> {
                    uiState.importPreview?.let { preview ->
                        PreviewStep(
                            preview = preview,
                            onContinue = {
                                // 直接进入导入，使用默认配置
                                viewModel.startImport()
                            },
                            onConfigure = {
                                // 进入配置步骤
                                viewModel.goToConfigureStep()
                            }
                        )
                    }
                }
                
                ImportStep.CONFIGURE -> {
                    ConfigureStep(
                        config = importConfig,
                        onConfigChange = { /* 配置更新方法将在ConfigureStep中实现 */ },
                        onStartImport = {
                            viewModel.startImport()
                        },
                        viewModel = viewModel
                    )
                }
                
                ImportStep.IMPORTING -> {
                    ImportingStep()
                }
                
                ImportStep.RESULT -> {
                    uiState.importResult?.let { result ->
                        ResultStep(
                            result = result,
                            onDone = onNavigateBack
                        )
                    }
                }
            }
            
            // 错误提示
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // 显示错误后自动清除
                }
                
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
 * 文件选择步骤
 */
@Composable
fun FileSelectionStep(
    isLoading: Boolean,
    onFileSelected: (Uri) -> Unit
) {
    // 使用OpenDocument替代GetContent，提供更好的兼容性
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onFileSelected(it) }
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
                text = "正在分析文件...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                Icons.Default.UploadFile,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "选择CSV文件",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "支持从CC小记导出的CSV格式文件\n如果看不到文件，请确保文件扩展名为.csv",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    // OpenDocument需要传入MIME类型数组
                    // 接受CSV和所有文本文件
                    launcher.launch(arrayOf("text/csv", "text/plain", "text/*", "*/*"))
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择文件")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 备选方案提示
            OutlinedButton(
                onClick = { 
                    // 尝试使用通配符选择所有文件
                    launcher.launch(arrayOf("*/*"))
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("浏览所有文件")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 提示信息
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
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
                            text = "支持的数据类型",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• 账户信息\n• 分类设置\n• 交易记录\n• 预算设置\n• 储蓄目标\n• 定期交易\n• 信用卡账单",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}