package com.ccxiaoji.app.presentation.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ccxiaoji.app.presentation.screen.import.components.*
import com.ccxiaoji.app.data.excel.ExcelImportOptions
import com.ccxiaoji.app.presentation.viewmodel.DataImportViewModel
import com.ccxiaoji.app.presentation.viewmodel.ImportStep
import com.ccxiaoji.ui.theme.DesignTokens

/**
 * 数据导入界面
 */
/**
 * 数据导入界面 - 扁平化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataImportScreen(
    navController: NavController,
    viewModel: DataImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // 主选择器：使用OpenDocument（更稳定）
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.handleFileSelection(it)
        } ?: run {
            // 处理null的情况
            viewModel.handleFileSelectionCancelled()
        }
    }
    
    // 备选方案1：GetContent（兼容性）
    val getContentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            viewModel.handleFileSelection(it)
        } ?: run {
            viewModel.handleFileSelectionCancelled()
        }
    }
    
    // 备选方案2：传统Intent方式
    val legacyFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    viewModel.handleFileSelection(uri)
                } ?: run {
                    viewModel.handleFileSelectionCancelled()
                }
            } else {
                viewModel.handleFileSelectionCancelled()
            }
        } catch (e: Exception) {
            // MIUI兼容性修复：处理Bundle为null的情况
            viewModel.showError("文件选择失败，请重试")
        }
    }
    
    // 文件选择处理函数
    fun selectFile() {
        try {
            // 尝试使用OpenDocument
            openDocumentLauncher.launch(
                arrayOf(
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/json",
                    "text/csv",
                    "*/*"
                )
            )
        } catch (e: ActivityNotFoundException) {
            // 降级到GetContent
            try {
                getContentLauncher.launch("*/*")
            } catch (e2: Exception) {
                // 最后的备选方案：传统Intent
                try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/json",
                            "text/csv"
                        ))
                    }
                    legacyFilePicker.launch(intent)
                } catch (e3: Exception) {
                    // MIUI兼容性修复：在MIUI设备上避免使用可能导致Bundle NPE的方法
                    viewModel.showError("无法打开文件选择器，请检查系统设置或使用其他方法导入文件")
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据导入") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "返回"
                        )
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
                            onSelectFile = { selectFile() }
                        )
                    }
                    ImportStep.FILE_TYPE_DETECTED -> {
                        // 文件类型检测完成，自动跳转到相应的预览
                        LaunchedEffect(Unit) {
                            viewModel.proceedWithFileType()
                        }
                        ImportingStep() // 显示加载状态
                    }
                    ImportStep.EXCEL_PREVIEW -> {
                        ExcelPreviewStep(
                            fileStructure = uiState.excelStructure,
                            onConfirmImport = { options ->
                                viewModel.startExcelImport(options)
                            },
                            onCancel = viewModel::goBack,
                            columnMappingDetector = viewModel.columnMappingDetector
                        )
                    }
                    ImportStep.JSON_PREVIEW, ImportStep.PREVIEW -> {
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(DesignTokens.Spacing.medium),
                    action = {
                        TextButton(
                            onClick = { viewModel.reset() },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.inversePrimary
                            )
                        ) {
                            Text("重试")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ) {
                    Text(error)
                }
            }
        }
    }
}

